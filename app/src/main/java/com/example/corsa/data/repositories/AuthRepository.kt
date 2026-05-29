package com.example.corsa.data.repositories

import com.example.corsa.data.model.Profile
import com.example.corsa.data.model.ProfileUpdate
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.filter.PostgrestFilterBuilder
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun getCurrentProfile(): Result<Profile?>
    suspend fun updateProfile(update: ProfileUpdate): Result<Unit>
    suspend fun logout(): Result<Unit>
    val sessionStatus: Flow<SessionStatus>
}

class AuthRepositoryImpl(
    private val supabase: SupabaseClient
) : AuthRepository {

    private var cachedProfile: Profile? = null

    override val sessionStatus: Flow<SessionStatus>
        get() = supabase.auth.sessionStatus

    override suspend fun getCurrentProfile(): Result<Profile?> = runCatching {
        cachedProfile?.let { return Result.success(it) }

        val authUserId = supabase.auth.currentUserOrNull()?.id
            ?: error("No authenticated user")

        supabase.from("profiles")
            .select {
                filter {
                    eq("auth_user_id", authUserId)
                }
            }
            .decodeSingleOrNull<Profile>()
            .also { cachedProfile = it }
    }

    override suspend fun updateProfile(update: ProfileUpdate): Result<Unit> = runCatching {
        val authUserId = supabase.auth.currentUserOrNull()?.id
            ?: error("No authenticated user")

        supabase.from("profiles")
            .update(update) {
                filter {
                    eq("auth_user_id", authUserId)
                }
            }

        cachedProfile = null
    }

    override suspend fun logout(): Result<Unit> = runCatching {
        supabase.auth.signOut()
        cachedProfile = null
    }
}