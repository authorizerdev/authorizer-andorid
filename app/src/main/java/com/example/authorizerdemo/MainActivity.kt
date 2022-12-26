package com.example.authorizerdemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.authorizer.android.AuthorizerConfig
import com.authorizer.android.provider.AuthorizerProvider

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeAuthorizer()
        findViewById<AppCompatButton>(R.id.btnUILogin).setOnClickListener{
            AuthorizerProvider.launchLoginUI(this)
        }

    }

    private fun initializeAuthorizer() {

        // https://authorizer-production-6455.up.railway.app = only mail login
        // ee3b1e66-947b-4646-999e-0e663ab47efe = Only mail Login

        // https://demo.authorizer.dev = all login option enabled
        // 96fed66c-9779-4694-a79a-260fc489ce33 = All Login types
        AuthorizerConfig.Builder()
            .authorizerURL("https://demo.authorizer.dev")
            .clientId("96fed66c-9779-4694-a79a-260fc489ce33")
            .packageName(BuildConfig.APPLICATION_ID)
            .build()
    }
}