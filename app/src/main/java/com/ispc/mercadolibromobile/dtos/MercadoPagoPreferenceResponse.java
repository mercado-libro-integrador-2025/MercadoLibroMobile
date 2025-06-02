package com.ispc.mercadolibromobile.dtos;

import com.google.gson.annotations.SerializedName;

public class MercadoPagoPreferenceResponse {
    @SerializedName("id")
    private String preferenceId;
    @SerializedName("init_point")
    private String initPoint;
    @SerializedName("sandbox_init_point")
    private String sandboxInitPoint;

    public MercadoPagoPreferenceResponse(String preferenceId, String initPoint, String sandboxInitPoint) {
        this.preferenceId = preferenceId;
        this.initPoint = initPoint;
        this.sandboxInitPoint = sandboxInitPoint;
    }
    public String getPreferenceId() {
        return preferenceId;
    }
    public String getInitPoint() {
        return initPoint;
    }
    public String getSandboxInitPoint() {
        return sandboxInitPoint;
    }
    public void setPreferenceId(String preferenceId) {
        this.preferenceId = preferenceId;
    }
    public void setInitPoint(String initPoint) {
        this.initPoint = initPoint;
    }
    public void setSandboxInitPoint(String sandboxInitPoint) {
        this.sandboxInitPoint = sandboxInitPoint;
    }
}