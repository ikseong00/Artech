package org.ikseong.artech.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.first
import org.ikseong.artech.BuildKonfig
import org.ikseong.artech.data.model.AppVersionDto
import org.ikseong.artech.data.model.AppVersionInfo
import org.ikseong.artech.data.model.UpdateType

class AppUpdateRepository(
    private val client: SupabaseClient,
    private val settingsRepository: SettingsRepository,
) {

    suspend fun checkForUpdate(): Pair<UpdateType, AppVersionInfo?> {
        return try {
            val dto = client.from(TABLE_NAME)
                .select {
                    filter {
                        eq("platform", BuildKonfig.APP_PLATFORM)
                    }
                    limit(1)
                }
                .decodeList<AppVersionDto>()
                .firstOrNull() ?: return Pair(UpdateType.NONE, null)

            val versionInfo = AppVersionInfo(
                forceUpdateVersion = dto.forceUpdateVersion,
                optionalUpdateVersion = dto.optionalUpdateVersion,
                storeUrl = dto.storeUrl,
            )

            val currentVersion = BuildKonfig.APP_VERSION
            val updateType = when {
                compareVersions(currentVersion, dto.forceUpdateVersion) < 0 -> UpdateType.FORCE
                compareVersions(currentVersion, dto.optionalUpdateVersion) < 0 -> {
                    val skippedVersion = settingsRepository.skippedOptionalVersion.first()
                    if (skippedVersion == dto.optionalUpdateVersion) UpdateType.NONE else UpdateType.OPTIONAL
                }
                else -> UpdateType.NONE
            }

            Pair(updateType, if (updateType != UpdateType.NONE) versionInfo else null)
        } catch (_: Exception) {
            Pair(UpdateType.NONE, null)
        }
    }

    private fun compareVersions(current: String, target: String): Int {
        val currentParts = current.split(".").map { it.toIntOrNull() ?: 0 }
        val targetParts = target.split(".").map { it.toIntOrNull() ?: 0 }
        val maxLength = maxOf(currentParts.size, targetParts.size)

        for (i in 0 until maxLength) {
            val currentPart = currentParts.getOrElse(i) { 0 }
            val targetPart = targetParts.getOrElse(i) { 0 }
            if (currentPart != targetPart) {
                return currentPart.compareTo(targetPart)
            }
        }
        return 0
    }

    companion object {
        private const val TABLE_NAME = "app_versions"
    }
}
