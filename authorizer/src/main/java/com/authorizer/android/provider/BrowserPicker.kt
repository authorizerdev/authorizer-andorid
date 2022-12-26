package com.authorizer.android.provider

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import androidx.browser.customtabs.CustomTabsService

/**
 * Class used to match any browser, preferring Custom Tabs compatible browsers
 * and browsers that are selected as the default browser application in the device settings.
 */

class BrowserPicker : Parcelable {

    companion object {
        private val CHROME_BROWSERS = listOf(
            "com.android.chrome",
            "com.google.android.apps.chrome",
            "com.android.chrome.beta",
            "com.android.chrome.dev"
        )

        @JvmField
        val CREATOR : Creator<BrowserPicker> = object :Creator<BrowserPicker> {
            override fun createFromParcel(parcel: Parcel): BrowserPicker {
                return BrowserPicker(parcel)
            }

            override fun newArray(size: Int): Array<BrowserPicker?> {
                return arrayOfNulls(size)
            }
        }

        /**
         * Starts building a new BrowserPicker that will match any browser, preferring Custom Tabs compatible browsers
         * and browsers that are selected as the default browser application in the device settings.
         *
         * @return a new BrowserPicker.Builder ready to customize.
         */
        fun newBuilder(): Builder {
            return Builder()
        }
    }

    private val allowedPackages: List<String>?

    private constructor(allowedPackages: List<String>?) {
        this.allowedPackages = allowedPackages
    }

    private constructor(parcel: Parcel) {
        allowedPackages = parcel.createStringArrayList()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeStringList(allowedPackages)
    }

    override fun describeContents(): Int {
        return 0
    }

    class Builder {
        private var allowedPackages: List<String>? = null

        /**
         * Filters from the available browsers those whose package name is contained in the given list.
         * The order of the list matters, as will be used as preference. Default browsers selected
         * explicitly by the end-user in the device settings, will be always preferred when they are
         * included in the allowed packages list.
         *
         * @param allowedPackages the list of browser package names to allow.
         * @return this builder instance.
         */
        fun withAllowedPackages(allowedPackages: List<String>): Builder {
            this.allowedPackages = allowedPackages
            return this
        }

        /**
         * Constructs a new BrowserPicker.
         *
         * @return a new BrowserPicker.
         * @see CustomTabsOptions
         */
        fun build(): BrowserPicker {
            return BrowserPicker(allowedPackages)
        }
    }

    fun getBestBrowserPackage(pm: PackageManager): String? {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.example.com"))
        val webHandler = pm.resolveActivity(browserIntent, PackageManager.MATCH_DEFAULT_ONLY)
        var defaultBrowser: String? = null
        if (webHandler != null) {
            defaultBrowser = webHandler.activityInfo.packageName
        }
        val availableBrowsers = pm.queryIntentActivities(browserIntent, PackageManager.MATCH_ALL)
        val regularBrowsers: MutableList<String> = ArrayList()
        val customTabsBrowsers: MutableList<String> = ArrayList()
        val isFilterEnabled = allowedPackages != null
        for (info in availableBrowsers) {
            val isAllowed = !isFilterEnabled || allowedPackages!!.contains(info.activityInfo.packageName)
            if (!isAllowed) {
                continue
            }
            val serviceIntent = Intent()
            serviceIntent.action = CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION
            serviceIntent.setPackage(info.activityInfo.packageName)
            if (pm.resolveService(serviceIntent, 0) != null) {
                customTabsBrowsers.add(info.activityInfo.packageName)
            } else {
                regularBrowsers.add(info.activityInfo.packageName)
            }
        }

        //If the browser packages were filtered, use the allowed packages list as order preference.
        //A user-selected default browser will always be picked up first.
        val preferenceList = getPreferenceOrder(allowedPackages, defaultBrowser)

        //If the list was filtered, the customTabsBrowsers and regularBrowsers Lists will contain only allowed packages.
        val customTabBrowser = getFirstMatch(customTabsBrowsers, preferenceList, defaultBrowser)
        return customTabBrowser ?: getFirstMatch(regularBrowsers, preferenceList, defaultBrowser)

        //Will return any browser or null
    }

    private fun getPreferenceOrder(allowedPackages: List<String>?, defaultBrowser: String?): List<String> {
        if (allowedPackages != null) {
            return allowedPackages
        }
        val preferenceList: MutableList<String> = ArrayList()
        if (defaultBrowser != null) {
            preferenceList.add(defaultBrowser)
        }
        preferenceList.addAll(CHROME_BROWSERS)
        return preferenceList
    }

    private fun getFirstMatch(baseList: List<String>, preferenceList: List<String>, bestChoice: String?): String? {
        bestChoice?.let {
            if (preferenceList.contains(bestChoice) && baseList.contains(bestChoice)) {
                return bestChoice
            }
        }

        //Walk the preferred items
        for (lstPreference in preferenceList) {
            if (baseList.contains(lstPreference)) {
                return lstPreference
            }
        }
        //Fallback to the first available item
        return if (baseList.isNotEmpty()) {
            baseList[0]
        } else null
    }


}