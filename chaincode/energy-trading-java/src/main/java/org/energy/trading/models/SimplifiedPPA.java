package org.energy.trading.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import java.time.Instant;

@DataType
public class SimplifiedPPA {

    @Property
    private String agreementId;

    @Property
    private String prosumerId;

    @Property
    private String buyerId;

    @Property
    private double tariffPerKWh;

    @Property
    private String agreementStartDate;

    @Property
    private String agreementEndDate;

    @Property
    private String status; // "ACTIVE", "EXPIRED", "TERMINATED"

    @Property
    private double totalEnergyGenerated;

    @Property
    private double totalTokensIssued;

    @Property
    private double totalInvoiceValue;

    @Property
    private String paymentStatus; // "PENDING", "PARTIAL", "PAID"

    @Property
    private String createdTimestamp;

    @Property
    private String lastUpdatedTimestamp;

    // Constructors
    public SimplifiedPPA() {}

    public SimplifiedPPA(String agreementId, String prosumerId, String buyerId,
                         double tariffPerKWh, String agreementStartDate, String agreementEndDate) {
        this.agreementId = agreementId;
        this.prosumerId = prosumerId;
        this.buyerId = buyerId;
        this.tariffPerKWh = tariffPerKWh;
        this.agreementStartDate = agreementStartDate;
        this.agreementEndDate = agreementEndDate;
        this.status = "ACTIVE";
        this.totalEnergyGenerated = 0.0;
        this.totalTokensIssued = 0.0;
        this.totalInvoiceValue = 0.0;
        this.paymentStatus = "PENDING";
        this.createdTimestamp = Instant.now().toString();
        this.lastUpdatedTimestamp = this.createdTimestamp;
    }

    // Getters and Setters
    @JsonProperty("agreementId")
    public String getAgreementId() { return agreementId; }
    public void setAgreementId(String agreementId) { this.agreementId = agreementId; }

    @JsonProperty("prosumerId")
    public String getProsumerId() { return prosumerId; }
    public void setProsumerId(String prosumerId) { this.prosumerId = prosumerId; }

    @JsonProperty("buyerId")
    public String getBuyerId() { return buyerId; }
    public void setBuyerId(String buyerId) { this.buyerId = buyerId; }

    @JsonProperty("tariffPerKWh")
    public double getTariffPerKWh() { return tariffPerKWh; }
    public void setTariffPerKWh(double tariffPerKWh) { this.tariffPerKWh = tariffPerKWh; }

    @JsonProperty("agreementStartDate")
    public String getAgreementStartDate() { return agreementStartDate; }
    public void setAgreementStartDate(String agreementStartDate) { this.agreementStartDate = agreementStartDate; }

    @JsonProperty("agreementEndDate")
    public String getAgreementEndDate() { return agreementEndDate; }
    public void setAgreementEndDate(String agreementEndDate) { this.agreementEndDate = agreementEndDate; }

    @JsonProperty("status")
    public String getStatus() { return status; }
    public void setStatus(String status) {
        this.status = status;
        this.lastUpdatedTimestamp = Instant.now().toString();
    }

    @JsonProperty("totalEnergyGenerated")
    public double getTotalEnergyGenerated() { return totalEnergyGenerated; }
    public void setTotalEnergyGenerated(double totalEnergyGenerated) { this.totalEnergyGenerated = totalEnergyGenerated; }

    @JsonProperty("totalTokensIssued")
    public double getTotalTokensIssued() { return totalTokensIssued; }
    public void setTotalTokensIssued(double totalTokensIssued) { this.totalTokensIssued = totalTokensIssued; }

    @JsonProperty("totalInvoiceValue")
    public double getTotalInvoiceValue() { return totalInvoiceValue; }
    public void setTotalInvoiceValue(double totalInvoiceValue) { this.totalInvoiceValue = totalInvoiceValue; }

    @JsonProperty("paymentStatus")
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
        this.lastUpdatedTimestamp = Instant.now().toString();
    }

    @JsonProperty("createdTimestamp")
    public String getCreatedTimestamp() { return createdTimestamp; }
    public void setCreatedTimestamp(String createdTimestamp) { this.createdTimestamp = createdTimestamp; }

    @JsonProperty("lastUpdatedTimestamp")
    public String getLastUpdatedTimestamp() { return lastUpdatedTimestamp; }
    public void setLastUpdatedTimestamp(String lastUpdatedTimestamp) { this.lastUpdatedTimestamp = lastUpdatedTimestamp; }
}
