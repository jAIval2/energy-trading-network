package org.energy.trading.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import java.time.Instant;

@DataType
public class EnergyCredit {
    
    @Property
    private String creditId;
    
    @Property
    private String prosumerId;
    
    @Property
    private double energyAmountKWh;
    
    @Property
    private String energySource; // "SOLAR", "WIND", etc.
    
    @Property
    private String generationTimestamp;
    
    @Property
    private String currentOwner;
    
    @Property
    private String status; // "AVAILABLE", "TRADING", "CONSUMED", "EXPIRED"
    
    @Property
    private double pricePerKWh;
    
    @Property
    private String location;
    
    @Property
    private boolean isCarbonNeutral;
    
    // Constructors
    public EnergyCredit() {}
    
    public EnergyCredit(String creditId, String prosumerId, double energyAmountKWh,
                       String energySource, String currentOwner, double pricePerKWh, 
                       String location, boolean isCarbonNeutral) {
        this.creditId = creditId;
        this.prosumerId = prosumerId;
        this.energyAmountKWh = energyAmountKWh;
        this.energySource = energySource;
        this.generationTimestamp = Instant.now().toString();
        this.currentOwner = currentOwner;
        this.status = "AVAILABLE";
        this.pricePerKWh = pricePerKWh;
        this.location = location;
        this.isCarbonNeutral = isCarbonNeutral;
    }
    
    // Getters and Setters
    @JsonProperty("creditId")
    public String getCreditId() { return creditId; }
    public void setCreditId(String creditId) { this.creditId = creditId; }
    
    @JsonProperty("prosumerId")
    public String getProsumerId() { return prosumerId; }
    public void setProsumerId(String prosumerId) { this.prosumerId = prosumerId; }
    
    @JsonProperty("energyAmountKWh")
    public double getEnergyAmountKWh() { return energyAmountKWh; }
    public void setEnergyAmountKWh(double energyAmountKWh) { this.energyAmountKWh = energyAmountKWh; }
    
    @JsonProperty("energySource")
    public String getEnergySource() { return energySource; }
    public void setEnergySource(String energySource) { this.energySource = energySource; }
    
    @JsonProperty("generationTimestamp")
    public String getGenerationTimestamp() { return generationTimestamp; }
    public void setGenerationTimestamp(String generationTimestamp) { this.generationTimestamp = generationTimestamp; }
    
    @JsonProperty("currentOwner")
    public String getCurrentOwner() { return currentOwner; }
    public void setCurrentOwner(String currentOwner) { this.currentOwner = currentOwner; }
    
    @JsonProperty("status")
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    @JsonProperty("pricePerKWh")
    public double getPricePerKWh() { return pricePerKWh; }
    public void setPricePerKWh(double pricePerKWh) { this.pricePerKWh = pricePerKWh; }
    
    @JsonProperty("location")
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    @JsonProperty("isCarbonNeutral")
    public boolean isCarbonNeutral() { return isCarbonNeutral; }
    public void setCarbonNeutral(boolean carbonNeutral) { isCarbonNeutral = carbonNeutral; }
}
