package org.ikseong.artech.data.model

object CategoryGroup {

    private val groups = listOf(
        listOf("QA", "Automation"),
        listOf("PM", "Business"),
        listOf("Infra", "DevOps"),
    )

    private val displayNameMap: Map<String, String> = buildMap {
        for (group in groups) {
            val displayName = group.joinToString("/")
            for (category in group) {
                put(category, displayName)
            }
        }
    }

    private val expandMap: Map<String, List<String>> = buildMap {
        for (group in groups) {
            val displayName = group.joinToString("/")
            put(displayName, group)
        }
    }

    fun toDisplayName(category: String): String = displayNameMap[category] ?: category

    fun expand(displayCategory: String): List<String> = expandMap[displayCategory] ?: listOf(displayCategory)

    fun mergeCategories(categories: List<String>): List<String> =
        categories.map { toDisplayName(it) }.distinct()
}
