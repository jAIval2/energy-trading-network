package org.energy.trading.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SimplifiedPPA {

    private String agreementId;
    private String prosumerId;
    private String buyerId;
    private double tariffPerKWh;
    private String startDate;
    private String endDate;

    private double totalEnergyGenerated;
    private double totalTokensIssued;
    private double totalInvoiceValue;

    @JsonCreator
    public SimplifiedPPA(
            @JsonProperty("agreementId") String agreementId,
            @JsonProperty("prosumerId") String prosumerId,
            @JsonProperty("buyerId") String buyerId,
            @JsonProperty("tariffPerKWh") double tariffPerKWh,
            @JsonProperty("startDate") String startDate,
            @JsonProperty("endDate") String endDate) {
        this.agreementId = agreementId;
        this.prosumerId = prosumerId;
        this.buyerId = buyerId;
        this.tariffPerKWh = tariffPerKWh;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalEnergyGenerated = 0.0;
        this.totalTokensIssued = 0.0;
        this.totalInvoiceValue = 0.0;
    }

    // --- Getters ---
    public String getAgreementId() {
        return agreementId;
    }

    public String getProsumerId() {
        return prosumerId;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public double getTariffPerKWh() {
        return tariffPerKWh;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public double getTotalEnergyGenerated() {
        return totalEnergyGenerated;
    }

    public double getTotalTokensIssued() {
        return totalTokensIssued;
    }

    public double getTotalInvoiceValue() {
        return totalInvoiceValue;
    }

    // --- Setters ---
    public void setAgreementId(String agreementId) {
        this.agreementId = agreementId;
    }

    public void setProsumerId(String prosumerId) {
        this.prosumerId = prosumerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public void setTariffPerKWh(double tariffPerKWh) {
        this.tariffPerKWh = tariffPerKWh;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public void setTotalEnergyGenerated(double totalEnergyGenerated) {
        this.totalEnergyGenerated = totalEnergyGenerated;
    }

    public void setTotalTokensIssued(double totalTokensIssued) {
        this.totalTokensIssued = totalTokensIssued;
    }

    public void setTotalInvoiceValue(double totalInvoiceValue) {
        this.totalInvoiceValue = totalInvoiceValue;
    }
}
