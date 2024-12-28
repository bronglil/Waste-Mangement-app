package com.example.wms.api


import com.example.wms.model.LoginRequest
import com.example.wms.model.SignUpRequest
import com.example.wms.viewmodel.BinDetails
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path


interface ApiService {

    // Coroutine-based method for signing up a user
    @POST("api/auth/signup")
    suspend fun signUpAuthSuspend(@Body request: SignUpRequest): SignUpResponse

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    // Callback-based method for fetching bin details
    @GET("api/bins/{id}")
    fun getBinDetails(@Path("id") binId: Int): Call<BinDetails>

    // Coroutine-based method for fetching bin details
    @GET("api/bins/{id}")
    suspend fun getBinDetailsSuspend(@Path("id") binId: Int): BinDetails
}
