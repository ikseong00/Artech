package org.ikseong.artech.ui.screen.bloglist

import org.ikseong.artech.data.model.BlogMeta

data class BlogListItem(
    val blogSource: String,
    val blogMeta: BlogMeta,
    val articleCount: Int,
)

data class BlogListUiState(
    val blogs: List<BlogListItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)
