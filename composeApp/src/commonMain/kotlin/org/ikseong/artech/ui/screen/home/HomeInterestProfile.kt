package org.ikseong.artech.ui.screen.home

data class HomeInterestProfile(
    val categoryScores: Map<String, Double>,
    val blogScores: Map<String, Double>,
) {
    val topCategories: List<String>
        get() = categoryScores.entries
            .sortedWith(compareByDescending<Map.Entry<String, Double>> { it.value }.thenBy { it.key })
            .map { it.key }

    fun scoreForCategory(category: String): Double = categoryScores[category] ?: 0.0

    fun scoreForBlog(blog: String): Double = blogScores[blog] ?: 0.0
}
