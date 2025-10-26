package org.example.replay

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.example.coords.Block
import org.example.coords.Vec3
import java.io.File
import kotlin.math.PI

object JsonReplay {
    @Serializable
    data class Meta(
        val builder: String,
        val measuredTime: Long,
        val mapId: Int,
        val cheatProbability: Double
    )

    @Serializable
    data class PacketEntry(
        val timestamp: Long,
        val packet: String
    )

    @Serializable
    data class Root(
        val timeStamp: Long,
        val duration: Long,
        val meta: Meta,
        val data: List<PacketEntry>
    )

    fun readJsonFile(filePath: String): Root {
        val jsonString = File(filePath).readText()
        return Json.decodeFromString(jsonString)
    }

    abstract class ReplayData()

    class ReplayPosition(val position: Vec3?, val rotation: Vec3?) : ReplayData() {
        override fun toString(): String {
            return "ReplayPosition[$position,  $rotation]"
        }
    }

    class ReplayWorldChange(val index: Int, val block: Block) : ReplayData() {

    }

    fun getReplayData(root: Root): List<ReplayData> {
        return root.data.mapNotNull {
            val packet = it.packet
            if (packet.startsWith("ClientPlayerPositionAndRotationPacket")) {
                val regex = Regex("""([xyz]|yaw|pitch)=([-+]?\d*\.?\d+)""")
                val matches = regex.findAll(packet)
                val coords = matches.associate { it.groupValues[1] to it.groupValues[2].toFloat() }

                val yaw = (coords["yaw"]!!) * -Math.PI.toFloat() / 180
                val pitch = coords["pitch"]!! * Math.PI.toFloat() / 180f

                val pos = ReplayPosition(
                    Vec3(coords["x"]!!, coords["y"]!! - 6, coords["z"]!!),
                    Vec3(yaw, 0f, pitch)
                )

                return@mapNotNull pos
//                println(pos)
            }
            if (packet.startsWith("ClientPlayerPositionPacket")) {
                val regex = Regex("""([xyz]|yaw|pitch)=([-+]?\d*\.?\d+)""")
                val matches = regex.findAll(packet)
                val coords = matches.associate { it.groupValues[1] to it.groupValues[2].toFloat() }

                val pos = ReplayPosition(
                    Vec3(coords["x"]!!, coords["y"]!! - 6, coords["z"]!!),
                    null
                )
                return@mapNotNull pos
            }
            if (packet.startsWith("ClientPlayerRotationPacket")) {
                val regex = Regex("""([xyz]|yaw|pitch)=([-+]?\d*\.?\d+)""")
                val matches = regex.findAll(packet)
                val coords = matches.associate { it.groupValues[1] to it.groupValues[2].toFloat() }

                val yaw = (coords["yaw"]!!) * -Math.PI.toFloat() / 180
                val pitch = coords["pitch"]!! * Math.PI.toFloat() / 180f

                val pos = ReplayPosition(
                    null,
                    Vec3(yaw, 0f, pitch)
                )
                return@mapNotNull pos
            }
            if (packet.startsWith("ClientPlayerBlockPlacementPacket")) {
                val regex =
                    Regex("""blockPosition=Vec\[x=([-+]?\d*\.?\d+),\s*y=([-+]?\d*\.?\d+),\s*z=([-+]?\d*\.?\d+)]""")

                val match = regex.find(packet)
                if (match != null) {
                    val (x, y, z) = match.destructured.toList().map { it.toFloat().toInt() }
                    val index = x * 20 * 20 + (y - 7) * 20 + z
                    println(index)
                    return@mapNotNull ReplayWorldChange(index, Block("light_blue_wool"))
                }
            }
            if (packet.startsWith("BlockChangePacket")) {
                val regex =
                    Regex("""blockPosition=Vec\[x=([-+]?\d*\.?\d+),\s*y=([-+]?\d*\.?\d+),\s*z=([-+]?\d*\.?\d+)]""")

                val match = regex.find(packet)
                if (match != null) {
                    val (x, y, z) = match.destructured.toList().map { it.toFloat().toInt() }
                    val index = x * 20 * 20 + (y - 7) * 20 + z
                    println(index)
                    return@mapNotNull ReplayWorldChange(index, Block("light_blue_wool"))
                }
            }

            return@mapNotNull null
        }
    }
}