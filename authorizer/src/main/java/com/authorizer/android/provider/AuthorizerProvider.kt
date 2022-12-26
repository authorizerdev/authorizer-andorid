package com.authorizer.android.provider

import android.app.Activity
import android.net.Uri
import com.authorizer.android.AuthorizerConfig
import com.authorizer.android.activity.DefaultUIActivity
import java.util.*

object AuthorizerProvider {
    fun launchLoginUI(context: Activity) {
        val authMapData = HashMap<String, Any>()
        val ctOptions: CustomTabsOptions = CustomTabsOptions
            .Builder()
            .build()

        val uri = Uri.parse(AuthorizerConfig.authorizerURL)
            .buildUpon()
            .scheme("https")
            .appendPath("android")
            .appendPath(AuthorizerConfig.packageName)
            .appendPath("callback")
            .build()
        authMapData["client_id"] = AuthorizerConfig.clientId
        authMapData["redirect_uri"] = uri
        authMapData["state"] = UUID.randomUUID().toString()
        authMapData["scope"] = "openid email profile offline"
        authMapData["response_mode"] = "query"


       /* ApiProvider.apiInstance().authorizeUser<String>(authMapData).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                response.body()?.let { Log.e("Auth Response", it) }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                t.message?.let { Log.e("Auth Failure", it) }
            }

        })*/
        DefaultUIActivity.loginWithBrowser(context,authMapData,ctOptions)
    }
}