package com.example.corsa.data.repositories

import com.example.corsa.data.model.Run
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.ZonedDateTime

// ── Interface (what the ViewModel depends on) ──────────────────────────────
interface RunsRepository {
    fun getRunById(id: String): Flow<Run?>
    fun getRunsByUser(userId: String): Flow<List<Run>>
}

// ── Fake implementation (no real Supabase yet) ─────────────────────────────
class FakeRunsRepository : RunsRepository {

    // Realistic GeoJSON path around a park loop
    private val fakePath = """
        {
  "type": "Feature",
  "geometry": {
    "type": "Point",
    "coordinates": [125.6, 10.1]
  },
  "properties": {
    "name": "Dinagat Islands"
  }
}
    """.trimIndent()

    private val fakeRuns = listOf(
        Run(
            id = "run-001",
            userId = "user-abc",
            startTime = ZonedDateTime.now().minusHours(1).minusMinutes(12),
            endTime = ZonedDateTime.now(),
            path = fakePath,
            distanceMeters = 7_430f,
            meanPaceSeconds = 312,      // 5:12 /km
            temperature = 18.5f,
            elevationGain = 54f
        ),
        Run(
            id = "run-002",
            userId = "user-abc",
            startTime = ZonedDateTime.now().minusDays(1).minusMinutes(45),
            endTime = ZonedDateTime.now().minusDays(1),
            path = fakePath,
            distanceMeters = 5_100f,
            meanPaceSeconds = 335,
            temperature = null,
            elevationGain = 30f
        )
    )

    override fun getRunById(id: String): Flow<Run?> = flow {
        emit(fakeRuns.firstOrNull { it.id == id })
    }

    override fun getRunsByUser(userId: String): Flow<List<Run>> = flow {
        emit(fakeRuns.filter { it.userId == userId })
    }
}