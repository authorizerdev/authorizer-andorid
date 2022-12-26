package com.authorizer.android

import com.authorizer.android.exception.AuthorizerException
import com.authorizer.android.utils.validatePackageName


open class AuthorizerConfig private constructor(builder: Builder) {
    companion object {
        // Your server connection url
        internal var authorizerURL: String = ""

        //User client id which can be provide from authorizer dashboard
        internal var clientId: String = ""

        internal var packageName: String = ""
    }

    init {
        authorizerURL = builder.baseUrl
        clientId = builder.clientId
        packageName = builder.packageName
        validateAuthenticateSata()
    }

    private fun validateAuthenticateSata() {
        if (authorizerURL.isEmpty()) {
            AuthorizerException().throwAuthenticationError("Authorizer Url must not be null or empty")
            return
        }
        if (clientId.isEmpty()) {
            AuthorizerException().throwAuthenticationError("Client Id must not be null or empty")
            return
        }

        if (!validatePackageName(packageName)) {
            AuthorizerException().throwAuthenticationError("Invalid Package name ")
            return
        }

    }

    class Builder {
        internal var baseUrl: String = ""
        internal var clientId: String = ""
        internal var packageName: String = ""
        fun authorizerURL(baseUrl: String) = apply { this.baseUrl = baseUrl }
        fun clientId(clientId: String) = apply { this.clientId = clientId }
        fun packageName(packageName: String) = apply { this.packageName = packageName }
        fun build() = AuthorizerConfig(this)
    }
}