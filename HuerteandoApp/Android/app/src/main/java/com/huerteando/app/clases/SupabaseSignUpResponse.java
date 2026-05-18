package com.huerteando.app.clases;

import com.google.gson.annotations.SerializedName;

public class SupabaseSignUpResponse {

    @SerializedName("id")
    private String id;

    @SerializedName("email")
    private String email;

    public String getId() { return id; }
    public String getEmail() { return email; }
}
