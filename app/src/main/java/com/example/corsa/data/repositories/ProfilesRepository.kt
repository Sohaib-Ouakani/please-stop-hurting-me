package com.example.corsa.data.repositories

import com.example.corsa.data.model.Profile
import com.example.corsa.data.model.ProfileUpdate
import com.example.corsa.ui.composables.UserRankEntry
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest

// ── Interface ──────────────────────────────────────────────────────────────
interface ProfilesRepository {
    suspend fun getProfileByUserId(userId: String): Profile
    suspend fun getAllProfiles(): List<Profile>
    suspend fun getMyProfile(): Profile
    suspend fun updateProfile(update: ProfileUpdate): Profile
}

// ── Fake implementation ────────────────────────────────────────────────────
class ProfilesRepositoryImpl(
    private val supabase: SupabaseClient
) : ProfilesRepository {

    private val fakeProfiles = listOf(
        UserRankEntry(
            userId             = "1",
            displayName        = "J. Donahue",
            avatarUrl          = null,
            weekKm             = 69.0,
            level              = 7,
            completedChallenge = 8,
            totKn              = 48.3
        ),
        UserRankEntry(
            userId             = "user-abc",
            displayName        = "M. Rossi",
            avatarUrl          = null,
            weekKm             = 22.4,
            level              = 4,
            completedChallenge = 3,
            totKn              = 312.5
        )
    )

    override suspend fun getProfileByUserId(userId: String): Profile {
        return supabase.postgrest["profiles"]
                .select {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeSingle<Profile>()

    }

    override suspend fun getAllProfiles(): List<Profile>  {
        return supabase.postgrest["profiles"]
                .select()
                .decodeList<Profile>()
    }

    override suspend fun getMyProfile(): Profile {
        val myId = supabase.auth.currentUserOrNull()?.id
            ?: error("User not authenticated")

        return getProfileByUserId(myId)
    }

    override suspend fun updateProfile(update: ProfileUpdate): Profile {
        val myId = supabase.auth.currentUserOrNull()?.id
            ?: error("User not authenticated")

        return supabase.postgrest["profiles"]
            .update(update) {
                filter {
                    eq("auth_user_id", myId)
                }
                select()
            }
            .decodeSingle<Profile>()
    }
}