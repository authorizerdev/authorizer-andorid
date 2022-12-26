package com.authorizer.android.exception

internal class AuthorizerException : Exception() {

    fun throwAuthenticationError(error:String){
        throw Exception(error)
    }
}