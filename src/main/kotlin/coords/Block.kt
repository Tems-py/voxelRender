package org.example.coords

import org.example.coords.Geometry.FaceName
import org.example.raycasting.Raycasting
import org.example.textures.BlockColor
import org.example.textures.TexturesManager
import org.example.utils.ColorUtils.mul
import org.example.wrapTo01
import java.awt.Color
import java.awt.image.BufferedImage
import kotlin.math.abs
import kotlin.math.round

class Block(val name: String) { // val position: Vec3,
    //    val color = BlockColor.blockColors[name] ?: BlockColor.ViewColor(0.0, 0.0, 0.0, 0.0)
    var isAir: Boolean = name == "air"
    var isFull: Boolean = true

    data class Hit(
        val hit2d : Vec2,
        val hit3d: Vec3,
        val bouncedDirection:Vec3,
        val distance: Float,
    )

    data class returnInfo(
        val color: Color,
        val outPosition: Vec3
    )

    val basicGeometry = Geometry(
        Vec3Int(0, 0, 0),
        Vec3Int(16, 16, 16),
        mapOf(
            FaceName.DOWN to Geometry.Face(Pair(Vec2Int(0, 0), Vec2Int(16, 16)), "body"),
            FaceName.UP to Geometry.Face(Pair(Vec2Int(0, 0), Vec2Int(16, 16)), "body"),
            FaceName.NORTH to Geometry.Face(Pair(Vec2Int(0,0), Vec2Int(16,16)), "body"),
            FaceName.SOUTH to Geometry.Face(Pair(Vec2Int(0,0), Vec2Int(16,16)), "body"),
            FaceName.WEST to Geometry.Face(Pair(Vec2Int(0, 0), Vec2Int(16,16)),  "body"),
            FaceName.EAST to Geometry.Face(Pair(Vec2Int(0, 0), Vec2Int(16,16)),  "body")
        ))

    var geometries = listOf<Geometry>()

    fun getColor(uv: Vec2, ray: Raycasting.Ray): returnInfo {
        var clampedX = (((-uv.x) % 1f) + 1f) % 1f
        var clampedY = (((uv.y) % 1f) + 1f) % 1f

        var rayOutPosition = ray.origin


        fun geometryHit(startPosition:Vec3,direction:Vec3,geometry: Geometry) : List<Hit>{
            val hits = mutableListOf<Hit>()
            var depthToTravel :Float;
            var directionDivided : Vec3;
            var hitPosition : Vec3;

            // Y plane
            depthToTravel = if (direction.y > 0) geometry.from.y / 16f - startPosition.y else startPosition.y - geometry.to.y/16f

            directionDivided = Vec3(direction.x / abs(direction.y), direction.y / abs(direction.y), direction.z / abs(direction.y))
            if(depthToTravel != 0f) {
                hitPosition = startPosition.plus(directionDivided.mul(depthToTravel))
                if (geometry.checkIfInsideBlock(hitPosition)) hits.add(
                    Hit(
                        Vec2(hitPosition.z, hitPosition.x),
                        hitPosition,
                        Vec3(direction.x,-direction.y,direction.z),
                        directionDivided.length()
                    )
                ); }

            // X plane
            depthToTravel = if (direction.x > 0) geometry.from.x / 16f - startPosition.x else startPosition.x - geometry.to.x/16f

            directionDivided = Vec3(direction.x / abs(direction.x), direction.y/abs(direction.x), direction.z / abs(direction.x))

            if(depthToTravel != 0f) {
                hitPosition = startPosition.plus(directionDivided.mul(depthToTravel))
                if (geometry.checkIfInsideBlock(hitPosition)) hits.add(
                    Hit(
                        Vec2(hitPosition.z, hitPosition.y),
                        hitPosition,
                        Vec3(-direction.x,direction.y,direction.z),
                        directionDivided.length()
                    )
                ); }

            // Z plane
            depthToTravel = if (direction.z > 0) geometry.from.z / 16f - startPosition.z else startPosition.z - geometry.to.z/16f
            directionDivided = Vec3(direction.x / abs(direction.z), direction.y/abs(direction.z), direction.z / abs(direction.z))

            if(depthToTravel != 0f) {
                hitPosition = startPosition.plus(directionDivided.mul(depthToTravel))
                if (geometry.checkIfInsideBlock(hitPosition)) hits.add(
                    Hit(
                        Vec2(hitPosition.x, hitPosition.y),
                        hitPosition,
                        Vec3(direction.x,direction.y,-direction.z),
                        directionDivided.length()
                    )
                ); }
            return hits
        }


        fun planeHits(startPosition:Vec3,direction:Vec3,geometry: Geometry) : List<Hit>{
            val hits = mutableListOf<Hit>()
            var depthToTravel :Float;
            var directionDivided : Vec3;
            var hitPosition : Vec3;

            // Y plane
            depthToTravel = if (direction.y > 0) geometry.to.y / 16f - startPosition.y else startPosition.y - geometry.from.y/16f
            directionDivided = Vec3(direction.x / abs(direction.y), direction.y / abs(direction.y), direction.z / abs(direction.y))
            if(depthToTravel != 0f) {
                hitPosition = startPosition.plus(directionDivided.mul(depthToTravel))
                if (geometry.checkIfInsideBlock(hitPosition)) hits.add(
                    Hit(
                        Vec2(hitPosition.z, hitPosition.x),
                        hitPosition,
                        Vec3(direction.x,-direction.y,direction.z),
                        directionDivided.length()
                    )
                )}

            // X plane
            depthToTravel = if (direction.x > 0) geometry.to.x / 16f - startPosition.x else startPosition.x - geometry.from.x/16f

            directionDivided = Vec3(direction.x / abs(direction.x), direction.y/abs(direction.x), direction.z / abs(direction.x))

            if(depthToTravel != 0f) {
                hitPosition = startPosition.plus(directionDivided.mul(depthToTravel))
                if (geometry.checkIfInsideBlock(hitPosition)) hits.add(
                    Hit(
                        Vec2(hitPosition.z, hitPosition.y),
                        hitPosition,
                        Vec3(-direction.x,direction.y,direction.z),
                        directionDivided.length()
                    )
                )}

            // Z plane
            depthToTravel = if (direction.z > 0) geometry.to.z / 16f - startPosition.z else startPosition.z - geometry.from.z/16f

            directionDivided = Vec3(direction.x / abs(direction.z), direction.y/abs(direction.z), direction.z / abs(direction.z))
            if(depthToTravel != 0f) {
                hitPosition = startPosition.plus(directionDivided.mul(depthToTravel))
                if (geometry.checkIfInsideBlock(hitPosition)) hits.add(
                    Hit(
                        Vec2(hitPosition.x, hitPosition.y),
                        hitPosition,
                        Vec3(direction.x,direction.y,-direction.z),
                        directionDivided.length()
                    )
                ) }
            return hits
        }

//        val colors = listOf(
//            Color(255, 0, 0),
//            Color(255, 255, 0),
//            Color(0, 255, 0),
//            Color(0, 255, 255),
//            Color(0, 0, 255),
//            Color(255, 0, 255),
//            Color(128, 128, 255),
//            Color(255, 128, 0),
//            Color(0, 0, 128),
//            Color(128, 0, 0),
//        )


//        return Vec3(ray.origin.x, ray.origin.y, ray.origin.z).toColor()

        if (!isFull) {
            val startPosition = ray.origin

            val direction = ray.direction
            var foundGeometry : Geometry? = null;

            for (geometry in geometries) {
                if (geometry.checkIfInsideBlock(startPosition)) {
                    foundGeometry = geometry
                    break
                }
            }
            if(foundGeometry == null){
                val hits = mutableListOf<Hit>()
                for (geometry in geometries){
                    val hitsInGeometry = geometryHit(ray.origin, ray.direction,geometry)
                    for (hit in hitsInGeometry){
                        hits.add(hit)
                    }
                }

                if(hits.isEmpty()){
                    return returnInfo(Color(0,0,0,0), ray.origin)    // <= nic nie trafione
                }


                val sortedHits = hits.sortedBy { it.distance }
                val closestHit = sortedHits[0]
                rayOutPosition = planeHits(closestHit.hit3d,closestHit.bouncedDirection,basicGeometry).sortedBy { it.distance }[0].hit3d
                clampedX = closestHit.hit2d.x
                clampedY = closestHit.hit2d.y
            }




            //najblizszy do startPosition hit to prawdziwy hit



        }
//        return(Color(0,0,0,0)) //<= wyjebac


//        return Color(clampedY, 0f, clampedX)
        val image: BufferedImage =
            TexturesManager.getTexture(name) ?: return returnInfo((BlockColor.blockColors[name]?.getJavaColor() ?: Color(
                126,
                225,
                252
            )), rayOutPosition)

        val px = (clampedX * (image.width - 1)).toInt()
        val py = (clampedY * (image.height - 1)).toInt()

        // Get pixel color
        val rgb = image.getRGB(px, py)

        var color = Color(rgb, true)

        val mulColor = if (name.contains("grass")) Color(119, 171, 47)
        else if (name.contains("leaves")) Color(119, 171, 47)
        else null

        if (mulColor != null) color = color.mul(mulColor)

        return returnInfo(color,rayOutPosition);
    }


    companion object {
        val air = Block("air")
    }

    override fun toString(): String {
        return "<Block $name>"
    }
}