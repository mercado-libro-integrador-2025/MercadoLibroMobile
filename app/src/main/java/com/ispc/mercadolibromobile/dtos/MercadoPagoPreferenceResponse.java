package com.ispc.mercadolibromobile.dtos;

import com.google.gson.annotations.SerializedName;

public class MercadoPagoPreferenceResponse {
    @SerializedName("id")
    private String preferenceId;
    @SerializedName("init_point")
    private String initPoint;

    public MercadoPagoPreferenceResponse(String preferenceId, String initPoint) {
        this.preferenceId = preferenceId;
        this.initPoint = initPoint;
    }

    public String getPreferenceId() {
        return preferenceId;
    }

    public String getInitPoint() {
        return initPoint;
    }

    public void setPreferenceId(String preferenceId) {
        this.preferenceId = preferenceId;
    }

    public void setInitPoint(String initPoint) {
        this.initPoint = initPoint;
    }
}