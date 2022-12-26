package com.authorizer.android.provider

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.browser.customtabs.CustomTabsSession
import com.authorizer.android.utils.safeLet
import java.lang.ref.WeakReference
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

internal class AppTabController  : CustomTabsServiceConnection() {

    private val tag: String = AppTabController::class.java.simpleName
    private val waitTime: Long = 1

    private var context: WeakReference<Context>? = null
    private var session: AtomicReference<CustomTabsSession>? = null
    private var sessionLatch: CountDownLatch? = null
    private var preferredPackage: String? = null
    private var customTabsOptions: CustomTabsOptions? = null
    private var didTryToBind = false

    override fun onCustomTabsServiceConnected(componentName: ComponentName, customTabsClient: CustomTabsClient) {
        customTabsClient.warmup(0L)
        session?.set(customTabsClient.newSession(null))
        sessionLatch?.countDown()
    }

    override fun onServiceDisconnected(componentName: ComponentName?) {
        session?.set(null)
    }

    /**
     * Attempts to bind the Custom Tabs Service to the Context.
     */
    open fun bindService() {
        val context: Context? = this.context?.get()
        didTryToBind = false
        safeLet(context,preferredPackage){cont,preferredPackage ->
            didTryToBind = true
            CustomTabsClient.bindCustomTabsService(cont, preferredPackage, this)
        }
    }

    /**
     * Attempts to unbind the Custom Tabs Service from the Context.
     */
    open fun unbindService() {
        Log.v(tag, "Trying to unbind the service")
        val context: Context? = this.context?.get()
        if (didTryToBind && context != null) {
            context.unbindService(this)
            didTryToBind = false
        }
    }

    /**
     * Opens a Uri in a Custom Tab or Browser.
     * The Custom Tab service will be given up to [CustomTabsController.MAX_WAIT_TIME_SECONDS] to be connected.
     * If it fails to connect the Uri will be opened on a Browser.
     *
     *
     * In the exceptional case that no Browser app is installed on the device, this method will fail silently and do nothing.
     * Please, ensure the [Intent.ACTION_VIEW] action can be handled before calling this method.
     *
     * @param uri the uri to open in a Custom Tab or Browser.
     */
    open fun launchUri(uri: Uri) {
        val context: Context? = this.context?.get()
        if (context == null) {
            Log.v(tag, "Custom Tab Context was no longer valid.")
            return
        }
        Thread {
            var available = false
            try {
                available = sessionLatch?.await(if (preferredPackage == null) 0 else waitTime, TimeUnit.SECONDS) == true
            } catch (ignored: InterruptedException) {
            }
            val intent: Intent? = customTabsOptions?.toIntent(context, session?.get())
            intent?.data = uri
            try {
                context.startActivity(intent)
            } catch (ex: ActivityNotFoundException) {
                Log.e(tag, "Could not find any Browser application installed in this device to handle the intent.")
            }
        }.start()
    }
}