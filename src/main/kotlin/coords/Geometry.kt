package me.tems.coords

import me.tems.utils.FloatUtils.fixFloatingPointError
import kotlin.math.max
import kotlin.math.min

class Geometry(val from: FloatArray, val to: FloatArray, val faces: Map<FaceName, Face>, val textures: Map<String, String>, var rotation: FloatArray) {
    enum class FaceName {
        NORTH, SOUTH, DOWN, UP, WEST, EAST,
    }

    override fun toString(): String {
        return "<Geometry $from, $to, ${faces.size} faces, $rotation rotation>"
    }

    data class Face(
        val uv: Pair<Vec2, Vec2>,
        var texture: String
    )

    // ── pre-computed at init time ──────────────────────────────────────────────
    val hasRotation: Boolean = rotation[0] != 0f || rotation[1] != 0f || rotation[2] != 0f

    // Negative rotation and pivot used by checkIfInsideBlock — stored once to
    // avoid allocating them on every call in the hot path.
    private val negRotation: FloatArray = floatArrayOf(-rotation[0], -rotation[1], -rotation[2])
    private val pivot8: FloatArray = floatArrayOf(8f, 8f, 8f)

    // Bounds in 0-1 world space (from/to divided by 16) for the fast no-rotation
    // path of checkIfInsideBlock — avoids the vec.mul(16f) allocation.
    private val sfx = from[0] / 16f; private val sfย = from[1] / 16f; private val sfz = from[2] / 16f
    private val stx = to[0]   / 16f; private val stY = to[1]   / 16f; private val stz = to[2]   / 16f

    // Rotated AABB (in 0-16 block space) used by InBlockRaycast.geometryHit so
    // bounds don't have to be recomputed on every ray.
    val realFrom: FloatArray
    val realTo: FloatArray

    init {
        if (hasRotation) {
            val rotFrom = from.rotateAroundPivot(rotation, pivot8)
            val rotTo   = to.rotateAroundPivot(rotation, pivot8)
            realFrom = floatArrayOf(min(rotFrom[0], rotTo[0]), min(rotFrom[1], rotTo[1]), min(rotFrom[2], rotTo[2]))
            realTo   = floatArrayOf(max(rotFrom[0], rotTo[0]), max(rotFrom[1], rotTo[1]), max(rotFrom[2], rotTo[2]))
        } else {
            realFrom = floatArrayOf(min(from[0], to[0]), min(from[1], to[1]), min(from[2], to[2]))
            realTo   = floatArrayOf(max(from[0], to[0]), max(from[1], to[1]), max(from[2], to[2]))
        }
    }

    fun checkIfInsideBlock(vec: FloatArray): Boolean {
        if (!hasRotation) {
            // Fast path: compare directly in 0-1 space — no allocation.
            return vec[0] >= sfx && vec[1] >= sfย && vec[2] >= sfz &&
                   vec[0] <= stx && vec[1] <= stY && vec[2] <= stz
        }
        // Rotate position back into geometry's unrotated 0-16 space.
        val pos = checkScratch.get()
        pos[0] = vec[0] * 16f; pos[1] = vec[1] * 16f; pos[2] = vec[2] * 16f
        pos.rotateAroundPivotReversedInto(pos, negRotation, pivot8)
        val px = pos[0].fixFloatingPointError()
        val py = pos[1].fixFloatingPointError()
        val pz = pos[2].fixFloatingPointError()
        return px >= from[0] && py >= from[1] && pz >= from[2] &&
               px <= to[0]   && py <= to[1]   && pz <= to[2]
    }

    fun clone(): Geometry {
        val clonedFaces = faces.mapValues { (_, face) -> face.copy() }
        return Geometry(from.add(Vec3.ZERO), to.add(Vec3.ZERO), clonedFaces, textures.toMap(), rotation.add(Vec3.ZERO))
    }

    companion object {
        private val checkScratch = ThreadLocal.withInitial { FloatArray(3) }
    }
}
