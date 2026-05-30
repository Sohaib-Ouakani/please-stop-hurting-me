package com.example.corsa.data.repositories

import com.example.corsa.data.model.Run
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

interface RunsRepository {
    suspend fun getRunById(id: String): Run
    suspend fun getRunsByUserId(userId: String): List<Run>
}

class RunsRepositoryImpl(
    private val supabase: SupabaseClient
) : RunsRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getRunById(id: String): Run {
        return supabase
            .from("runs")
            .select(Columns.raw("*, ST_AsGeoJSON(path) as path")) {
                filter { eq("id", id) }
            }
            .decodeSingle<Run>()
            .wrapPathAsFeatureCollection(json)
    }

    override suspend fun getRunsByUserId(userId: String): List<Run> {
        return supabase
            .from("runs")
            .select(Columns.raw("*, ST_AsGeoJSON(path) as path")) {
                filter { eq("user_id", userId) }
                order("start_time", Order.DESCENDING)
            }
            .decodeList<Run>()
            .map { it.wrapPathAsFeatureCollection(json) }
    }

    private fun Run.wrapPathAsFeatureCollection(json: Json): Run {
        val geometry = json.parseToJsonElement(path)
        val featureCollection = buildJsonObject {
            put("type", "FeatureCollection")
            putJsonArray("features") {
                addJsonObject {
                    put("type", "Feature")
                    putJsonObject("properties") {}
                    put("geometry", geometry)
                }
            }
        }.toString()
        return copy(path = featureCollection)
    }
}