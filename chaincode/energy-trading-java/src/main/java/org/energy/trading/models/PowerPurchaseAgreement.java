package org.energy.trading.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType
public class PowerPurchaseAgreement {
    
    @Property
    private String ppaId;
    
    @Property
    private String sellerId;
    
    @Property
    private String buyerId;
    
    @Property
    private double energyAmountKWh;
    
    @Property
    private double agreedPricePerKWh;
    
    @Property
    private double totalAmount;
    
    @Property
    private String contractStartDate;
    
    @Property
    private String contractEndDate;
    
    @Property
    private String status; // "PENDING", "ACTIVE", "COMPLETED", "CANCELLED"
    
    @Property
    private String termsAndConditions;
    
    @Property
    private String createdTimestamp;
    
    @Property
    private String lastUpdatedTimestamp;
    
    // Constructors
    public PowerPurchaseAgreement() {}
    
    public PowerPurchaseAgreement(String ppaId, String sellerId, String buyerId, 
                                 double energyAmountKWh, double agreedPricePerKWh,
                                 String contractStartDate, String contractEndDate,
                                 String termsAndConditions) {
        this.ppaId = ppaId;
        this.sellerId = sellerId;
        this.buyerId = buyerId;
        this.energyAmountKWh = energyAmountKWh;
        this.agreedPricePerKWh = agreedPricePerKWh;
        this.totalAmount = energyAmountKWh * agreedPricePerKWh;
        this.contractStartDate = contractStartDate;
        this.contractEndDate = contractEndDate;
        this.status = "PENDING";
        this.termsAndConditions = termsAndConditions;
        this.createdTimestamp = java.time.Instant.now().toString();
        this.lastUpdatedTimestamp = this.createdTimestamp;
    }
    
    // Getters and Setters
    @JsonProperty("ppaId")
    public String getPpaId() { return ppaId; }
    public void setPpaId(String ppaId) { this.ppaId = ppaId; }
    
    @JsonProperty("sellerId")
    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }
    
    @JsonProperty("buyerId")
    public String getBuyerId() { return buyerId; }
    public void setBuyerId(String buyerId) { this.buyerId = buyerId; }
    
    @JsonProperty("energyAmountKWh")
    public double getEnergyAmountKWh() { return energyAmountKWh; }
    public void setEnergyAmountKWh(double energyAmountKWh) { this.energyAmountKWh = energyAmountKWh; }
    
    @JsonProperty("agreedPricePerKWh")
    public double getAgreedPricePerKWh() { return agreedPricePerKWh; }
    public void setAgreedPricePerKWh(double agreedPricePerKWh) { this.agreedPricePerKWh = agreedPricePerKWh; }
    
    @JsonProperty("totalAmount")
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    
    @JsonProperty("contractStartDate")
    public String getContractStartDate() { return contractStartDate; }
    public void setContractStartDate(String contractStartDate) { this.contractStartDate = contractStartDate; }
    
    @JsonProperty("contractEndDate")
    public String getContractEndDate() { return contractEndDate; }
    public void setContractEndDate(String contractEndDate) { this.contractEndDate = contractEndDate; }
    
    @JsonProperty("status")
    public String getStatus() { return status; }
    public void setStatus(String status) { 
        this.status = status;
        this.lastUpdatedTimestamp = java.time.Instant.now().toString();
    }
    
    @JsonProperty("termsAndConditions")
    public String getTermsAndConditions() { return termsAndConditions; }
    public void setTermsAndConditions(String termsAndConditions) { this.termsAndConditions = termsAndConditions; }
    
    @JsonProperty("createdTimestamp")
    public String getCreatedTimestamp() { return createdTimestamp; }
    public void setCreatedTimestamp(String createdTimestamp) { this.createdTimestamp = createdTimestamp; }
    
    @JsonProperty("lastUpdatedTimestamp")
    public String getLastUpdatedTimestamp() { return lastUpdatedTimestamp; }
    public void setLastUpdatedTimestamp(String lastUpdatedTimestamp) { this.lastUpdatedTimestamp = lastUpdatedTimestamp; }
}
