package com.huerteando.app.api;

import com.huerteando.app.clases.SupabaseSignUpRequest;
import com.huerteando.app.clases.SupabaseSignUpResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface SupabaseService {

    @POST("auth/v1/signup")
    Call<SupabaseSignUpResponse> signUp(@Body SupabaseSignUpRequest request);
}
