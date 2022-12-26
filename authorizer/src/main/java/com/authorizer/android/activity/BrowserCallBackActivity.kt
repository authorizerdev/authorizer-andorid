package com.authorizer.android.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle

public class BrowserCallBackActivity : Activity() {
    public override fun onCreate(savedInstanceBundle: Bundle?) {
        super.onCreate(savedInstanceBundle)
        val intent = Intent(this, DefaultUIActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        intent.data = getIntent()?.data
        startActivity(intent)
        finish()
    }
}