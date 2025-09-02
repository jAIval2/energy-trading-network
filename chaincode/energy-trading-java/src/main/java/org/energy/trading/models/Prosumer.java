package org.energy.trading.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType
public class Prosumer {
    
    @Property
    private String prosumerId;
    
    @Property
    private String name;
    
    @Property
    private String location;
    
    @Property
    private double solarCapacityKW;
    
    @Property
    private String organizationMSP;
    
    @Property
    private boolean isActive;
    
    @Property
    private double totalEnergyGenerated;
    
    @Property
    private double totalEnergyTraded;

    // Constants
    private static final double MIN_CAPACITY = 0.0;
    private static final double MAX_CAPACITY = 100000.0; // 100 MW
    private static final double MIN_ENERGY = 0.0;
    private static final double MAX_ENERGY = 1_000_000_000.0; // 1 TWh

    // Constructors
    public Prosumer() {}

    public Prosumer(String prosumerId, String name, String location, 
                   double solarCapacityKW, String organizationMSP) {
        
        if (prosumerId == null || prosumerId.trim().isEmpty()) {
            throw new IllegalArgumentException("prosumerId cannot be null or empty");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("name cannot be null or empty");
        }
        if (location == null || location.trim().isEmpty()) {
            throw new IllegalArgumentException("location cannot be null or empty");
        }
        if (organizationMSP == null || organizationMSP.trim().isEmpty()) {
            throw new IllegalArgumentException("organizationMSP cannot be null or empty");
        }
        
        validateCapacity(solarCapacityKW);
        
        this.prosumerId = prosumerId;
        this.name = name;
        this.location = location;
        this.solarCapacityKW = solarCapacityKW;
        this.organizationMSP = organizationMSP;
        this.isActive = true;
        this.totalEnergyGenerated = 0.0;
        this.totalEnergyTraded = 0.0;
    }
    
    private void validateCapacity(double capacity) {
        if (capacity < MIN_CAPACITY) {
            throw new IllegalArgumentException("Capacity cannot be negative: " + capacity);
        }
        if (capacity > MAX_CAPACITY) {
            throw new IllegalArgumentException("Capacity " + capacity + " exceeds maximum allowed: " + MAX_CAPACITY);
        }
    }
    
    private void validateEnergy(double energy) {
        if (energy < MIN_ENERGY) {
            throw new IllegalArgumentException("Energy cannot be negative: " + energy);
        }
        if (energy > MAX_ENERGY) {
            throw new IllegalArgumentException("Energy " + energy + " exceeds maximum allowed: " + MAX_ENERGY);
        }
    }

    // Getters and Setters
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

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("name cannot be null or empty");
        }
        this.name = name;
    }

    @JsonProperty("location")
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        if (location == null || location.trim().isEmpty()) {
            throw new IllegalArgumentException("location cannot be null or empty");
        }
        this.location = location;
    }

    @JsonProperty("solarCapacityKW")
    public double getSolarCapacityKW() {
        return solarCapacityKW;
    }

    public void setSolarCapacityKW(double solarCapacityKW) {
        validateCapacity(solarCapacityKW);
        this.solarCapacityKW = solarCapacityKW;
    }

    @JsonProperty("organizationMSP")
    public String getOrganizationMSP() {
        return organizationMSP;
    }

    public void setOrganizationMSP(String organizationMSP) {
        if (organizationMSP == null || organizationMSP.trim().isEmpty()) {
            throw new IllegalArgumentException("organizationMSP cannot be null or empty");
        }
        this.organizationMSP = organizationMSP;
    }

    @JsonProperty("isActive")
    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @JsonProperty("totalEnergyGenerated")
    public double getTotalEnergyGenerated() {
        return totalEnergyGenerated;
    }

    public void setTotalEnergyGenerated(double totalEnergyGenerated) {
        validateEnergy(totalEnergyGenerated);
        this.totalEnergyGenerated = totalEnergyGenerated;
    }

    @JsonProperty("totalEnergyTraded")
    public double getTotalEnergyTraded() {
        return totalEnergyTraded;
    }

    public void setTotalEnergyTraded(double totalEnergyTraded) {
        validateEnergy(totalEnergyTraded);
        this.totalEnergyTraded = totalEnergyTraded;
    }
}
