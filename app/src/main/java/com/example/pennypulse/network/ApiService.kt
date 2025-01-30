package com.example.pennypulse.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.Call

interface ApiService {
    @POST("login") // This should match your server's login endpoint
    suspend fun loginUser(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @POST("signup") // Adjust the endpoint as per your server setup
    suspend fun signupUser(@Body request: SignupRequest): Response<SignupResponse>

    @POST("reg") // Adjust the endpoint as per your server setup
    suspend fun registerUser(@Body request: SignupRequest): Response<RegisterResponse>

    @POST("sms") // Update with your actual endpoint
    suspend fun sendSms(@Body smsBody: SmsData): Response<Any>

    @GET("expense") // Adjust the endpoint according to your API
    suspend fun getUserDetails(@Header("Authorization") token: String): Response<UserDetailsResponse>

    @POST("statements") // Update with the correct endpoint
    suspend fun submitStatement(
        @Header("Authorization") token: String,
        @Body request: StatementRequest
    ): Response<StatementResponse> // Use the new response model here

    @GET("charts") // Replace with your actual endpoint
    fun getWeeklyExpenses(@Header("Authorization") token: String): Call<List<ExpenseDataPoint>>

    @GET("transactions")
    fun getTransactions(
        @Header("Authorization") token: String
    ): Call<List<Transaction>>
}
