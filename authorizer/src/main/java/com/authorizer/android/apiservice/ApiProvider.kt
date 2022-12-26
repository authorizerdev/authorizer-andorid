package com.authorizer.android.apiservice

import com.authorizer.android.AuthorizerConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

internal object ApiProvider {
    private lateinit var okHttpClient: OkHttpClient
    private lateinit var retrofit: Retrofit

    init {
        initializeOkHttp()
        initializeRetrofit()
    }

    private fun initializeRetrofit() {
        retrofit = Retrofit.Builder()
            .baseUrl(AuthorizerConfig.authorizerURL!!)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun initializeOkHttp() {
        okHttpClient = OkHttpClient.Builder()
            .connectTimeout(1, TimeUnit.MINUTES)
            .readTimeout(1, TimeUnit.MINUTES)
            .addInterceptor(loggingInterceptor)
            .build()
    }

     fun apiInstance(): ApiInterface {
        return retrofit.create(ApiInterface::class.java)
    }

    private val loggingInterceptor: HttpLoggingInterceptor = HttpLoggingInterceptor { message: String? ->
        /* run {
             message?.let {
                 Log.i("response", it)
             }
         }*/
    }.setLevel(HttpLoggingInterceptor.Level.BODY)

}