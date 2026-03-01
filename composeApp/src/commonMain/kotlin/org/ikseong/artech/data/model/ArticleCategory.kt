package org.ikseong.devnews.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ArticleCategory(val displayName: String) {
    @SerialName("AI")
    AI("AI"),

    @SerialName("Android")
    Android("Android"),

    @SerialName("Automation")
    Automation("Automation"),

    @SerialName("Cross-platform")
    CrossPlatform("Cross-platform"),

    @SerialName("Data")
    Data("Data"),

    @SerialName("DevOps")
    DevOps("DevOps"),

    @SerialName("Hiring")
    Hiring("Hiring"),

    @SerialName("Infra")
    Infra("Infra"),

    @SerialName("iOS")
    IOS("iOS"),

    @SerialName("PM")
    PM("PM"),

    @SerialName("QA")
    QA("QA"),

    @SerialName("Server")
    Server("Server"),

    @SerialName("Web")
    Web("Web"),
}
