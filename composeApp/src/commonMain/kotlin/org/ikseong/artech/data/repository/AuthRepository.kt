package org.ikseong.artech.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.signInAnonymously
import kotlinx.coroutines.flow.Flow

class AuthRepository(
    private val supabase: SupabaseClient,
    private val sessionManager: SessionManager,
) {
    val isLoggedIn: Flow<Boolean> = sessionManager.isLoggedIn

    suspend fun signInAnonymously(): Result<Unit> {
        return runCatching {
            supabase.auth.signInAnonymously()
            val session = supabase.auth.currentSessionOrNull()!!
            sessionManager.saveSession(
                userId = session.user!!.id,
                accessToken = session.accessToken,
                refreshToken = session.refreshToken,
            )
        }
    }
}
