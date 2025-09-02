package org.energy.trading.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

@DataType
public class GenerationEvent {

    // Constants
    private static final double MIN_ENERGY = 0.0;
    private static final double MAX_ENERGY = 1_000_000.0; // 1 GWh per event
    private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    private static final String[] ALLOWED_STATUSES = {"PROCESSED", "PENDING", "FAILED"};

    @Property
    private String eventId;

    @Property
    private String prosumerId;

    @Property
    private String meterId;

    @Property
    private double generatedKWh;

    @Property
    private String timestamp;

    @Property
    private String agreementId;

    @Property
    private double tokensIssued;

    @Property
    private double invoiceValue;

    @Property
    private String status; // "PROCESSED", "PENDING", "FAILED"

    // Constructors
    public GenerationEvent() {}

    public GenerationEvent(String eventId, String prosumerId, String meterId,
                          double generatedKWh, String timestamp, String agreementId,
                          double tokensIssued, double invoiceValue) {
        
        setEventId(eventId);
        setProsumerId(prosumerId);
        setMeterId(meterId);
        setGeneratedKWh(generatedKWh);
        setTimestamp(timestamp);
        setAgreementId(agreementId);
        setTokensIssued(tokensIssued);
        setInvoiceValue(invoiceValue);
        this.status = "PENDING"; // Default status
    }

    // Validation methods
    private void validateEnergy(double energy) {
        if (energy < MIN_ENERGY) {
            throw new IllegalArgumentException("Energy cannot be negative: " + energy);
        }
        if (energy > MAX_ENERGY) {
            throw new IllegalArgumentException("Energy " + energy + " exceeds maximum allowed: " + MAX_ENERGY);
        }
    }

    private void validateTimestamp(String timestamp) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(TIMESTAMP_FORMAT);
            sdf.setLenient(false);
            sdf.parse(timestamp);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid timestamp format. Expected format: " + TIMESTAMP_FORMAT, e);
        }
    }

    private void validateStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }
        for (String allowedStatus : ALLOWED_STATUSES) {
            if (allowedStatus.equals(status)) {
                return;
            }
        }
        throw new IllegalArgumentException("Invalid status: " + status + ". Must be one of: " + String.join(", ", ALLOWED_STATUSES));
    }

    // Getters and Setters with validation
    @JsonProperty("eventId")
    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        if (eventId == null || eventId.trim().isEmpty()) {
            throw new IllegalArgumentException("eventId cannot be null or empty");
        }
        this.eventId = eventId;
    }

    @JsonProperty("prosumerId")
    public String getProsumerId() {
        return prosumerId;
    }

    public void setProsumerId(String prosumerId) {
        if (prosumerId == null || prosumerId.trim().isEmpty()) {
            throw new IllegalArgumentException("prosumerId cannot be null or empty");
        }
        this.prosumerId = prosumerId;
    }

    @JsonProperty("meterId")
    public String getMeterId() {
        return meterId;
    }

    public void setMeterId(String meterId) {
        if (meterId == null || meterId.trim().isEmpty()) {
            throw new IllegalArgumentException("meterId cannot be null or empty");
        }
        this.meterId = meterId;
    }

    @JsonProperty("generatedKWh")
    public double getGeneratedKWh() {
        return generatedKWh;
    }

    public void setGeneratedKWh(double generatedKWh) {
        validateEnergy(generatedKWh);
        this.generatedKWh = generatedKWh;
    }

    @JsonProperty("timestamp")
    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        validateTimestamp(timestamp);
        this.timestamp = timestamp;
    }

    @JsonProperty("agreementId")
    public String getAgreementId() {
        return agreementId;
    }

    public void setAgreementId(String agreementId) {
        if (agreementId == null || agreementId.trim().isEmpty()) {
            throw new IllegalArgumentException("agreementId cannot be null or empty");
        }
        this.agreementId = agreementId;
    }

    @JsonProperty("tokensIssued")
    public double getTokensIssued() {
        return tokensIssued;
    }

    public void setTokensIssued(double tokensIssued) {
        if (tokensIssued < 0) {
            throw new IllegalArgumentException("Tokens issued cannot be negative: " + tokensIssued);
        }
        this.tokensIssued = tokensIssued;
    }

    @JsonProperty("invoiceValue")
    public double getInvoiceValue() {
        return invoiceValue;
    }

    public void setInvoiceValue(double invoiceValue) {
        if (invoiceValue < 0) {
            throw new IllegalArgumentException("Invoice value cannot be negative: " + invoiceValue);
        }
        this.invoiceValue = invoiceValue;
    }

    @JsonProperty("status")
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        validateStatus(status);
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GenerationEvent that = (GenerationEvent) o;
        return Double.compare(that.generatedKWh, generatedKWh) == 0 &&
               Double.compare(that.tokensIssued, tokensIssued) == 0 &&
               Double.compare(that.invoiceValue, invoiceValue) == 0 &&
               Objects.equals(eventId, that.eventId) &&
               Objects.equals(prosumerId, that.prosumerId) &&
               Objects.equals(meterId, that.meterId) &&
               Objects.equals(timestamp, that.timestamp) &&
               Objects.equals(agreementId, that.agreementId) &&
               Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, prosumerId, meterId, generatedKWh, timestamp, 
                          agreementId, tokensIssued, invoiceValue, status);
    }

    @Override
    public String toString() {
        return "GenerationEvent{" +
                "eventId='" + eventId + '\'' +
                ", prosumerId='" + prosumerId + '\'' +
                ", meterId='" + meterId + '\'' +
                ", generatedKWh=" + generatedKWh +
                ", timestamp='" + timestamp + '\'' +
                ", agreementId='" + agreementId + '\'' +
                ", tokensIssued=" + tokensIssued +
                ", invoiceValue=" + invoiceValue +
                ", status='" + status + '\'' +
                '}';
    }
}
