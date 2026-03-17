package org.ikseong.artech.ui.screen.bloglist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.ikseong.artech.data.model.BlogMeta
import org.ikseong.artech.data.model.faviconUrl
import org.ikseong.artech.data.repository.ArticleRepository

class BlogListViewModel(
    private val articleRepository: ArticleRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BlogListUiState())
    val uiState: StateFlow<BlogListUiState> = _uiState.asStateFlow()

    init {
        loadBlogs()
    }

    fun loadBlogs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val results = articleRepository.getAllBlogArticleCounts()
                val blogs = results.map { result ->
                    BlogListItem(
                        blogSource = result.blogSource,
                        blogMeta = BlogMeta(
                            name = result.blogSource,
                            url = result.url,
                            logoUrl = if (result.domain.isNotBlank()) faviconUrl(result.domain) else "",
                        ),
                        articleCount = result.count,
                    )
                }.sortedByDescending { it.articleCount }
                _uiState.update { it.copy(blogs = blogs, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "오류가 발생했습니다") }
            }
        }
    }
}
