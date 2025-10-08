package textures

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MinecraftModel(
    val parent: String? = null,

    // Optional textures
    val textures: Map<String, String>? = null,

    // Optional display section
    val display: Display? = null,

    // Optional elements list
    val elements: List<Element>? = null
)

@Serializable
data class Display(
    val gui: Transform? = null,
    val head: Transform? = null,
    @SerialName("thirdperson_lefthand")
    val thirdPersonLeftHand: Transform? = null
)

@Serializable
data class Transform(
    val rotation: List<Float>? = null,
    val translation: List<Float>? = null,
    val scale: List<Float>? = null
)

@Serializable
data class Element(
    val from: List<Float>,
    val to: List<Float>,
    val faces: Faces? = null
)

@Serializable
data class Faces(
    val down: Face? = null,
    val up: Face? = null,
    val north: Face? = null,
    val south: Face? = null,
    val west: Face? = null,
    val east: Face? = null
)

@Serializable
data class Face(
    val uv: List<Float>? = null,
    val texture: String? = null,
    val cullface: String? = null,
    val tintindex: Int? = null
)
