package org.energy.trading.contracts;

import java.util.ArrayList;
import java.util.List;

import org.energy.trading.models.EnergyCredit;
import org.energy.trading.models.SimplifiedPPA;
import org.energy.trading.models.GenerationEvent;
import org.energy.trading.models.Prosumer;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Contract(
        name = "EnergyTradingContract",
        info = @Info(
                title = "Simplified Energy Trading Smart Contract",
                description = "Smart contract for automated PPA management and token generation from electricity data",
                version = "1.0.0",
                license = @License(
                        name = "Apache License Version 2.0",
                        url = "http://www.apache.org/licenses/LICENSE-2.0.html")
        )
)
@Default
public class EnergyTradingContract implements ContractInterface {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Constants
    private static final double MIN_TARIFF = 0.0;
    private static final double MAX_TARIFF = 1000.0;
    private static final double MIN_ENERGY = 0.0;
    private static final double MAX_ENERGY = 1000000.0;
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final double TOKEN_TO_KWH_RATIO = 1.0; // 1 token = 1 kWh

    // Error messages
    private enum EnergyTradingErrors {
        PROSUMER_NOT_FOUND("Prosumer not found"),
        PROSUMER_ALREADY_EXISTS("Prosumer already exists"),
        PPA_NOT_FOUND("PPA not found"),
        PPA_ALREADY_EXISTS("PPA already exists"),
        GENERATION_EVENT_FAILED("Failed to process generation event"),
        JSON_PARSING_ERROR("Error parsing JSON"),
        INVALID_INPUT("Invalid input parameters"),
        INVALID_DATE_RANGE("End date must be after start date"),
        NEGATIVE_VALUE_NOT_ALLOWED("Negative values are not allowed"),
        VALUE_OUT_OF_RANGE("Value out of valid range");

        private final String message;

        EnergyTradingErrors(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    // Helper method to validate input parameters
    private void validateInput(String paramName, double value, double min, double max) {
        if (value < min) {
            throw new ChaincodeException(String.format("%s cannot be negative: %.2f", paramName, value),
                    EnergyTradingErrors.NEGATIVE_VALUE_NOT_ALLOWED.toString());
        }
        if (value > max) {
            throw new ChaincodeException(String.format("%s value %.2f exceeds maximum allowed value of %.2f",
                    paramName, value, max), EnergyTradingErrors.VALUE_OUT_OF_RANGE.toString());
        }
    }

    // Helper method to validate date format and range
    private void validateDateRange(String startDate, String endDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
            sdf.setLenient(false);
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);

            if (start.after(end)) {
                throw new ChaincodeException(EnergyTradingErrors.INVALID_DATE_RANGE.getMessage(),
                        EnergyTradingErrors.INVALID_DATE_RANGE.toString());
            }
        } catch (ParseException e) {
            throw new ChaincodeException("Invalid date format. Expected format: " + DATE_FORMAT,
                    EnergyTradingErrors.INVALID_INPUT.toString());
        }
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void initLedger(final Context context) {
        ChaincodeStub stub = context.getStub();

        // Create sample prosumers
        createSampleProsumer(context, "PROSUMER001", "Green Solar Farm", "Mumbai, Maharashtra", 100.0, "ProsumerMSP");
        createSampleProsumer(context, "PROSUMER002", "Eco Energy Solutions", "Pune, Maharashtra", 150.0, "ProsumerMSP");

        // Create sample PPAs
        createSamplePPA(context, "PPA001", "PROSUMER001", "UTILITY001", 4.5, "2025-01-01", "2030-12-31");
        createSamplePPA(context, "PPA002", "PROSUMER002", "CORPORATE001", 4.2, "2025-01-01", "2030-12-31");
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String processElectricityGeneration(final Context context,
                                               final String prosumerId,
                                               final double generatedKWh,
                                               final String meterId,
                                               final String timestamp,
                                               final String buyerId) {

        if (prosumerId == null || prosumerId.trim().isEmpty() ||
            meterId == null || meterId.trim().isEmpty() ||
            timestamp == null || timestamp.trim().isEmpty() ||
            buyerId == null || buyerId.trim().isEmpty()) {
            throw new ChaincodeException("All parameters are required and cannot be empty",
                    EnergyTradingErrors.INVALID_INPUT.toString());
        }

        validateInput("generatedKWh", generatedKWh, MIN_ENERGY, MAX_ENERGY);

        ChaincodeStub stub = context.getStub();

        try {
            Prosumer prosumer = getProsumer(context, prosumerId);
            String agreementId = findOrCreatePPA(context, prosumerId, buyerId);
            SimplifiedPPA ppa = getPPA(context, agreementId);

            double tokensToIssue = generatedKWh * TOKEN_TO_KWH_RATIO;
            double invoiceValue = generatedKWh * ppa.getTariffPerKWh();

            String eventId = prosumerId + "_" + System.currentTimeMillis();
            GenerationEvent event = new GenerationEvent(eventId, prosumerId, meterId,
                    generatedKWh, timestamp, agreementId,
                    tokensToIssue, invoiceValue);

            String tokenId = "TOKEN_" + eventId;
            EnergyCredit energyToken = new EnergyCredit(tokenId, prosumerId, generatedKWh,
                    "SOLAR", prosumerId, ppa.getTariffPerKWh(),
                    prosumer.getLocation(), true);

            ppa.setTotalEnergyGenerated(ppa.getTotalEnergyGenerated() + generatedKWh);
            ppa.setTotalTokensIssued(ppa.getTotalTokensIssued() + tokensToIssue);
            ppa.setTotalInvoiceValue(ppa.getTotalInvoiceValue() + invoiceValue);

            prosumer.setTotalEnergyGenerated(prosumer.getTotalEnergyGenerated() + generatedKWh);

            String eventJSON = objectMapper.writeValueAsString(event);
            stub.putStringState("EVENT_" + eventId, eventJSON);

            String tokenJSON = objectMapper.writeValueAsString(energyToken);
            stub.putStringState("CREDIT_" + tokenId, tokenJSON);

            String ppaJSON = objectMapper.writeValueAsString(ppa);
            stub.putStringState("PPA_" + agreementId, ppaJSON);

            String prosumerJSON = objectMapper.writeValueAsString(prosumer);
            stub.putStringState("PROSUMER_" + prosumerId, prosumerJSON);

            return String.format("{\"status\":\"SUCCESS\",\"eventId\":\"%s\",\"tokenId\":\"%s\",\"tokensIssued\":%.2f,\"invoiceValue\":%.2f,\"agreementId\":\"%s\"}",
                    eventId, tokenId, tokensToIssue, invoiceValue, agreementId);

        } catch (JsonProcessingException e) {
            throw new ChaincodeException("Failed to process electricity generation", EnergyTradingErrors.JSON_PARSING_ERROR.toString());
        }
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public SimplifiedPPA createPPA(final Context context, final String agreementId,
                                   final String prosumerId, final String buyerId,
                                   final double tariffPerKWh, final String startDate, final String endDate) {

        if (agreementId == null || agreementId.trim().isEmpty() ||
            prosumerId == null || prosumerId.trim().isEmpty() ||
            buyerId == null || buyerId.trim().isEmpty() ||
            startDate == null || startDate.trim().isEmpty() ||
            endDate == null || endDate.trim().isEmpty()) {
            throw new ChaincodeException("All parameters are required and cannot be empty",
                    EnergyTradingErrors.INVALID_INPUT.toString());
        }

        validateInput("tariffPerKWh", tariffPerKWh, MIN_TARIFF, MAX_TARIFF);
        validateDateRange(startDate, endDate);

        ChaincodeStub stub = context.getStub();

        String ppaKey = "PPA_" + agreementId;
        String ppaJSON = stub.getStringState(ppaKey);

        if (!ppaJSON.isEmpty()) {
            String errorMessage = String.format("PPA %s already exists", agreementId);
            throw new ChaincodeException(errorMessage, EnergyTradingErrors.PPA_ALREADY_EXISTS.toString());
        }

        SimplifiedPPA ppa = new SimplifiedPPA(agreementId, prosumerId, buyerId,
                tariffPerKWh, startDate, endDate);

        try {
            ppaJSON = objectMapper.writeValueAsString(ppa);
            stub.putStringState(ppaKey, ppaJSON);
            return ppa;
        } catch (JsonProcessingException e) {
            throw new ChaincodeException("Failed to serialize PPA", EnergyTradingErrors.JSON_PARSING_ERROR.toString());
        }
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public SimplifiedPPA getPPA(final Context context, final String agreementId) {
        ChaincodeStub stub = context.getStub();
        String ppaKey = "PPA_" + agreementId;
        String ppaJSON = stub.getStringState(ppaKey);

        if (ppaJSON.isEmpty()) {
            String errorMessage = String.format("PPA %s does not exist", agreementId);
            throw new ChaincodeException(errorMessage, EnergyTradingErrors.PPA_NOT_FOUND.toString());
        }

        try {
            return objectMapper.readValue(ppaJSON, SimplifiedPPA.class);
        } catch (JsonProcessingException e) {
            throw new ChaincodeException("Failed to deserialize PPA", EnergyTradingErrors.JSON_PARSING_ERROR.toString());
        }
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String getGenerationEvents(final Context context, final String prosumerId) {
        ChaincodeStub stub = context.getStub();
        List<GenerationEvent> events = new ArrayList<>();

        QueryResultsIterator<KeyValue> results = stub.getStateByRange("EVENT_", "EVENT_~");
        for (KeyValue result : results) {
            try {
                GenerationEvent event = objectMapper.readValue(result.getStringValue(), GenerationEvent.class);
                if (prosumerId.equals(event.getProsumerId())) {
                    events.add(event);
                }
            } catch (JsonProcessingException e) {
                // Skip invalid records
            }
        }

        try {
            return objectMapper.writeValueAsString(events);
        } catch (JsonProcessingException e) {
            throw new ChaincodeException("Failed to serialize events", EnergyTradingErrors.JSON_PARSING_ERROR.toString());
        }
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String getAvailableTokens(final Context context) {
        ChaincodeStub stub = context.getStub();
        List<EnergyCredit> availableTokens = new ArrayList<>();

        QueryResultsIterator<KeyValue> results = stub.getStateByRange("CREDIT_", "CREDIT_~");
        for (KeyValue result : results) {
            try {
                EnergyCredit token = objectMapper.readValue(result.getStringValue(), EnergyCredit.class);
                if (token.isAvailable()) { // ✅ fixed: check boolean flag
                    availableTokens.add(token);
                }
            } catch (JsonProcessingException e) {
                // Skip invalid records
            }
        }

        try {
            return objectMapper.writeValueAsString(availableTokens);
        } catch (JsonProcessingException e) {
            throw new ChaincodeException("Failed to serialize tokens", EnergyTradingErrors.JSON_PARSING_ERROR.toString());
        }
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Prosumer registerProsumer(final Context context, final String prosumerId,
                                     final String name, final String location,
                                     final double solarCapacityKW, final String organizationMSP) {
        ChaincodeStub stub = context.getStub();

        String prosumerKey = "PROSUMER_" + prosumerId;
        String prosumerJSON = stub.getStringState(prosumerKey);

        if (!prosumerJSON.isEmpty()) {
            String errorMessage = String.format("Prosumer %s already exists", prosumerId);
            throw new ChaincodeException(errorMessage, EnergyTradingErrors.PROSUMER_ALREADY_EXISTS.toString());
        }

        Prosumer prosumer = new Prosumer(prosumerId, name, location, solarCapacityKW, organizationMSP);

        try {
            prosumerJSON = objectMapper.writeValueAsString(prosumer);
            stub.putStringState(prosumerKey, prosumerJSON);
            return prosumer;
        } catch (JsonProcessingException e) {
            throw new ChaincodeException("Failed to serialize prosumer", EnergyTradingErrors.JSON_PARSING_ERROR.toString());
        }
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Prosumer getProsumer(final Context context, final String prosumerId) {
        ChaincodeStub stub = context.getStub();
        String prosumerKey = "PROSUMER_" + prosumerId;
        String prosumerJSON = stub.getStringState(prosumerKey);

        if (prosumerJSON.isEmpty()) {
            String errorMessage = String.format("Prosumer %s does not exist", prosumerId);
            throw new ChaincodeException(errorMessage, EnergyTradingErrors.PROSUMER_NOT_FOUND.toString());
        }

        try {
            return objectMapper.readValue(prosumerJSON, Prosumer.class);
        } catch (JsonProcessingException e) {
            throw new ChaincodeException("Failed to deserialize prosumer", EnergyTradingErrors.JSON_PARSING_ERROR.toString());
        }
    }

    private String findOrCreatePPA(Context context, String prosumerId, String buyerId) {
        ChaincodeStub stub = context.getStub();

        QueryResultsIterator<KeyValue> results = stub.getStateByRange("PPA_", "PPA_~");
        for (KeyValue result : results) {
            try {
                SimplifiedPPA ppa = objectMapper.readValue(result.getStringValue(), SimplifiedPPA.class);
                if (prosumerId.equals(ppa.getProsumerId()) && buyerId.equals(ppa.getBuyerId())) {
                    return ppa.getAgreementId();
                }
            } catch (JsonProcessingException e) {
                // Skip invalid records
            }
        }

        String newAgreementId = "PPA_" + prosumerId + "_" + buyerId + "_" + System.currentTimeMillis();
        createPPA(context, newAgreementId, prosumerId, buyerId, 4.5, "2025-01-01", "2030-12-31");
        return newAgreementId;
    }

    private void createSampleProsumer(Context context, String id, String name, String location,
                                      double capacity, String msp) {
        try {
            registerProsumer(context, id, name, location, capacity, msp);
        } catch (ChaincodeException e) {
            // Ignore if already exists
        }
    }

    private void createSamplePPA(Context context, String agreementId, String prosumerId,
                                 String buyerId, double tariff, String startDate, String endDate) {
        try {
            createPPA(context, agreementId, prosumerId, buyerId, tariff, startDate, endDate);
        } catch (ChaincodeException e) {
            // Ignore if already exists
        }
    }
}
