package com.authorizer.android.provider

import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.browser.customtabs.CustomTabsSession
import android.content.ComponentName
import androidx.browser.customtabs.CustomTabsClient
import android.content.Intent
import android.content.ActivityNotFoundException
import android.content.Context
import android.net.Uri
import android.util.Log
import java.lang.ref.WeakReference
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

internal class CustomTabsController constructor(context: Context, options: CustomTabsOptions) : CustomTabsServiceConnection() {
    private val context: WeakReference<Context>
    private val session: AtomicReference<CustomTabsSession?>
    private val sessionLatch: CountDownLatch
    private val preferredPackage: String?
    private val customTabsOptions: CustomTabsOptions
    private var didTryToBind = false
    override fun onCustomTabsServiceConnected(componentName: ComponentName, customTabsClient: CustomTabsClient) {
        customTabsClient.warmup(0L)
        session.set(customTabsClient.newSession(null))
        sessionLatch.countDown()
    }

    override fun onServiceDisconnected(componentName: ComponentName) {
        session.set(null)
    }

    /**
     * Attempts to bind the Custom Tabs Service to the Context.
     */
    fun bindService() {
        val context = context.get()
        didTryToBind = false
        if (context != null && preferredPackage != null) {
            didTryToBind = true
            CustomTabsClient.bindCustomTabsService(context, preferredPackage, this)
        }
    }

    /**
     * Attempts to unbind the Custom Tabs Service from the Context.
     */
    fun unbindService() {
        val context = context.get()
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
    fun launchUri(uri: Uri) {
        val context = context.get()
        if (context == null) {
            Log.v(TAG, "Custom Tab Context was no longer valid.")
            return
        }
        Thread {
            var available = false
            try {
                available = sessionLatch.await(if (preferredPackage == null) 0 else MAX_WAIT_TIME_SECONDS, TimeUnit.SECONDS)
            } catch (ignored: InterruptedException) {
            }
            val intent = customTabsOptions.toIntent(context, session.get())
            intent.data = uri
            try {
                context.startActivity(intent)
            } catch (ex: ActivityNotFoundException) {
            }
        }.start()
    }

    companion object {
        val TAG: String = CustomTabsController::class.java.simpleName
        private const val MAX_WAIT_TIME_SECONDS: Long = 1
    }

    init {
        this.context = WeakReference(context)
        session = AtomicReference()
        sessionLatch = CountDownLatch(1)
        customTabsOptions = options
        preferredPackage = options.getPreferredPackage(context.packageManager)
    }
}