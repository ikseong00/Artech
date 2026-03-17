package org.ikseong.artech.navigation

import kotlinx.serialization.Serializable

sealed interface Route {

    @Serializable
    data object Onboarding : Route

    @Serializable
    data object Home : Route

    @Serializable
    data object Favorite : Route

    @Serializable
    data object History : Route

    @Serializable
    data object Settings : Route

    @Serializable
    data class Detail(val articleId: Long, val link: String) : Route

    @Serializable
    data class Blog(val blogSource: String) : Route

    @Serializable
    data object BlogList : Route
}
