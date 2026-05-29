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
    suspend fun logout(): Result<Unit>
    val sessionStatus: Flow<SessionStatus>
}

class AuthRepositoryImpl(
    private val supabase: SupabaseClient
) : AuthRepository {

    private var cachedProfile: Profile? = null

    override val sessionStatus: Flow<SessionStatus>
        get() = supabase.auth.sessionStatus

    override suspend fun logout(): Result<Unit> = runCatching {
        supabase.auth.signOut()
        cachedProfile = null
    }
}