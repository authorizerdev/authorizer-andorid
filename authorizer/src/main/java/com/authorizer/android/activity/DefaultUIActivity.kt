package com.authorizer.android.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.core.app.ActivityCompat.startActivityForResult
import com.authorizer.android.AuthorizerConfig
import com.authorizer.android.provider.AuthorizerEndPoints
import com.authorizer.android.provider.CustomTabsController
import com.authorizer.android.provider.CustomTabsOptions
import com.authorizer.android.utils.formatQueryParams

internal class DefaultUIActivity : Activity() {

    private var intentTriggered = false
    private var tabsController: CustomTabsController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.let {
            intentTriggered = it.getBoolean(EXTRA_INTENT_TRIGGERED, false)
        }
    }

    override fun onResume() {
        super.onResume()
        val authenticationIntent = intent
        if (!intentTriggered && authenticationIntent.extras == null) {
            finish()
            return
        } else if (!intentTriggered) {
            intentTriggered = true
            launchAuthenticationIntent()
            return
        }
        val resultMissing = authenticationIntent.data == null
        if (resultMissing) {
            setResult(RESULT_CANCELED)
        }
        finish()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(EXTRA_INTENT_TRIGGERED, intentTriggered)
    }


    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val resultData = if (resultCode == RESULT_CANCELED) Intent() else data
        managedResult(resultData)
        finish()
    }

    private fun managedResult(resultData: Intent?) {

    }

    override fun onDestroy() {
        super.onDestroy()
        tabsController?.let {
            it.unbindService()
            tabsController = null
        }
    }

    private fun launchAuthenticationIntent() {
        val extras = intent.extras
        extras?.let { extra ->
            val authorizeUri = extra.getParcelable<Uri>(EXTRA_URI)
            val customTabsOptions: CustomTabsOptions = extra.getParcelable(EXTRA_CT_OPTIONS)!!
            tabsController = createCustomTabsController(this, customTabsOptions)
            tabsController?.bindService()
            authorizeUri?.let { tabsController?.launchUri(it) }
        }

    }

    private fun createCustomTabsController(
        context: Context,
        options: CustomTabsOptions
    ): CustomTabsController {
        return CustomTabsController(context, options)
    }

    internal companion object {
        private const val EXTRA_URI = "requestedURI"
        private const val EXTRA_CT_OPTIONS = "EXTRA_CT_OPTIONS"
        private const val EXTRA_INTENT_TRIGGERED = "intentTriggred"

        @JvmStatic
        internal fun loginWithBrowser(context: Activity, authMapData: HashMap<String, Any>, options: CustomTabsOptions) {

            val intent = Intent(context, DefaultUIActivity::class.java)
            intent.putExtra(EXTRA_URI, Uri.parse(AuthorizerConfig.authorizerURL.plus(AuthorizerEndPoints.authorizeUI).plus(formatQueryParams(authMapData))))
            intent.putExtra(EXTRA_CT_OPTIONS, options)
            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivityForResult(context, intent, 100, null)
        }

    }
}