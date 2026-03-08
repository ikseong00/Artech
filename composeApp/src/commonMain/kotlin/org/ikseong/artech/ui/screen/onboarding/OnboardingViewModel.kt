package org.ikseong.artech.ui.screen.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.ikseong.artech.data.repository.AuthRepository

data class OnboardingUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
)

class OnboardingViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun onStartClicked() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.signInAnonymously()
                .onFailure {
                    _uiState.update { it.copy(isLoading = false, error = "시작하기에 실패했습니다. 다시 시도해 주세요.") }
                }
            // 성공 시 App.kt의 isLoggedIn Flow가 true로 바뀌어 자동 전환
        }
    }
}
