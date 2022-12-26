package com.authorizer.android.apiservice

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap

public interface ApiInterface {

    @GET("/verify_email")
    fun <T> verifyEmail(@Query("token") token: String): Call<T>

    @GET("/userinfo")
    fun <T> getUserInfo(@Query("token") token: String): Call<T>

    @GET("/authorize")
    fun <T> authorizeUser(@QueryMap queryMap: HashMap<String, Any>): Call<T>

}