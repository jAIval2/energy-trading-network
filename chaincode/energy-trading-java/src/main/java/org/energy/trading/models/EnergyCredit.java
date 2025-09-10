package org.energy.trading.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType
public class EnergyCredit {

    @Property
    private String tokenId;

    @Property
    private String prosumerId;

    @Property
    private double energyAmount; // in kWh

    @Property
    private String energyType;   // e.g. SOLAR, WIND, HYDRO

    @Property
    private String ownerId;      // who owns the token

    @Property
    private double tariffPerKWh; // price of energy per kWh

    @Property
    private String location;

    @Property
    private boolean available;   // ✅ boolean availability flag

    @JsonCreator
    public EnergyCredit(
            @JsonProperty("tokenId") String tokenId,
            @JsonProperty("prosumerId") String prosumerId,
            @JsonProperty("energyAmount") double energyAmount,
            @JsonProperty("energyType") String energyType,
            @JsonProperty("ownerId") String ownerId,
            @JsonProperty("tariffPerKWh") double tariffPerKWh,
            @JsonProperty("location") String location,
            @JsonProperty("available") boolean available) {
        this.tokenId = tokenId;
        this.prosumerId = prosumerId;
        this.energyAmount = energyAmount;
        this.energyType = energyType;
        this.ownerId = ownerId;
        this.tariffPerKWh = tariffPerKWh;
        this.location = location;
        this.available = available;
    }

    // --- Getters ---
    public String getTokenId() {
        return tokenId;
    }

    public String getProsumerId() {
        return prosumerId;
    }

    public double getEnergyAmount() {
        return energyAmount;
    }

    public String getEnergyType() {
        return energyType;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public double getTariffPerKWh() {
        return tariffPerKWh;
    }

    public String getLocation() {
        return location;
    }

    public boolean isAvailable() {
        return available;
    }

    // --- Setters ---
    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public void setProsumerId(String prosumerId) {
        this.prosumerId = prosumerId;
    }

    public void setEnergyAmount(double energyAmount) {
        this.energyAmount = energyAmount;
    }

    public void setEnergyType(String energyType) {
        this.energyType = energyType;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public void setTariffPerKWh(double tariffPerKWh) {
        this.tariffPerKWh = tariffPerKWh;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
