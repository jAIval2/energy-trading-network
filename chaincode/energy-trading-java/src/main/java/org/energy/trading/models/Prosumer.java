package org.energy.trading.models;

import com.fasterxml.jackson.annotation.JsonCreator;
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
    private double totalEnergyGenerated;

    @JsonCreator
    public Prosumer(
            @JsonProperty("prosumerId") String prosumerId,
            @JsonProperty("name") String name,
            @JsonProperty("location") String location,
            @JsonProperty("solarCapacityKW") double solarCapacityKW,
            @JsonProperty("organizationMSP") String organizationMSP) {
        this.prosumerId = prosumerId;
        this.name = name;
        this.location = location;
        this.solarCapacityKW = solarCapacityKW;
        this.organizationMSP = organizationMSP;
        this.totalEnergyGenerated = 0.0;
    }

    // --- Getters ---
    public String getProsumerId() {
        return prosumerId;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public double getSolarCapacityKW() {
        return solarCapacityKW;
    }

    public String getOrganizationMSP() {
        return organizationMSP;
    }

    public double getTotalEnergyGenerated() {
        return totalEnergyGenerated;
    }

    // --- Setters ---
    public void setProsumerId(String prosumerId) {
        this.prosumerId = prosumerId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setSolarCapacityKW(double solarCapacityKW) {
        this.solarCapacityKW = solarCapacityKW;
    }

    public void setOrganizationMSP(String organizationMSP) {
        this.organizationMSP = organizationMSP;
    }

    public void setTotalEnergyGenerated(double totalEnergyGenerated) {
        this.totalEnergyGenerated = totalEnergyGenerated;
    }
}
