package com.authorizer.android.provider

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsSession

internal class CustomTabsOptions(browserPicker: BrowserPicker?) : Parcelable {
    private var browserPicker: BrowserPicker? = null

    constructor(parcel: Parcel) : this(parcel.readParcelable(BrowserPicker::class.java.classLoader)) {
        browserPicker = parcel.readParcelable(BrowserPicker::class.java.classLoader)
    }

    init {
        this.browserPicker = browserPicker
    }

    fun getPreferredPackage(pm: PackageManager): String? {
        return browserPicker?.getBestBrowserPackage(pm)
    }

    fun hasCompatibleBrowser(pm: PackageManager): Boolean {
        return getPreferredPackage(pm) != null
    }

    /**
     * Create a new CustomTabsOptions.Builder instance.
     *
     * @return a new CustomTabsOptions.Builder ready to customize.
     */
    fun newBuilder(): Builder {
        return Builder()
    }

    @SuppressLint("ResourceType")
    fun toIntent(context: Context, session: CustomTabsSession?): Intent {
        val builder = CustomTabsIntent.Builder(session)
            .setShareState(CustomTabsIntent.SHARE_STATE_OFF)
        return builder.build().intent
    }

    class Builder {
        private var browserPicker: BrowserPicker = BrowserPicker.newBuilder().build()
        /**
         * Create a new CustomTabsOptions instance with the customization settings.
         *
         * @return an instance of CustomTabsOptions with the customization settings.
         */
        fun build(): CustomTabsOptions {
            return CustomTabsOptions(browserPicker)
        }

    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(browserPicker, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Creator<CustomTabsOptions> {
        override fun createFromParcel(parcel: Parcel): CustomTabsOptions {
            return CustomTabsOptions(parcel)
        }

        override fun newArray(size: Int): Array<CustomTabsOptions?> {
            return arrayOfNulls(size)
        }
    }
}
