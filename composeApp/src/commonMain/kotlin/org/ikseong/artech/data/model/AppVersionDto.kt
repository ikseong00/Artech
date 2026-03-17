package org.ikseong.artech.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppVersionDto(
    val id: Long,
    val platform: String,
    @SerialName("force_update_version")
    val forceUpdateVersion: String,
    @SerialName("optional_update_version")
    val optionalUpdateVersion: String,
    @SerialName("store_url")
    val storeUrl: String,
)
