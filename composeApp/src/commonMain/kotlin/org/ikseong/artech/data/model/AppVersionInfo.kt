package org.ikseong.artech.data.model

data class AppVersionInfo(
    val forceUpdateVersion: String,
    val optionalUpdateVersion: String,
    val storeUrl: String,
)
