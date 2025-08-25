package com.scpsync.plugin.utils

data class ScpResult(
    val isSuccess: Boolean,
    val message: String,
    val errorMessage: String? = null
) {
    companion object {
        fun success(message: String): ScpResult = ScpResult(true, message)
        fun failure(errorMessage: String): ScpResult = ScpResult(false, "", errorMessage)
    }
}