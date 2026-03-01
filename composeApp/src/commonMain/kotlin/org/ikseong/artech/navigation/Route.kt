package org.ikseong.devnews.navigation

import kotlinx.serialization.Serializable

sealed interface Route {

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
}
