package com.authorizer.android.apiservice

import retrofit2.Response

interface ApiCallBack<T> {
        /**
         * Invoked for a received HTTP response.
         *
         *
         * Note: An HTTP response may still indicate an application-level failure such as a 404 or 500.
         * Call [Response.isSuccessful] to determine if the response indicates success.
         */
        fun onResponse(apiResponse: T)

        /**
         * Invoked when a network exception occurred talking to the server or when an unexpected exception
         * occurred creating the request or processing the response.
         */
        fun onFailure(t: Throwable?)

        /**
         * Invoked when tried to call API function while internet is not connected with device.
         */
        fun noNetworkAvailable()

        /**
         * Invoked if there is any validation error in api url or data
         */
        fun validationError(message:String)

        /**
         * Invoked if there is any validation error in api url or data
         */
        fun configurableError(message: String)
}