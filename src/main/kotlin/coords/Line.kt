package org.example.coords

class Line(val origin: Vec3,val direction: Vec3) {
    //lane = direction * t + origin
    //lane: x = directionx * t + originx
    //      y = directiony * t + originy
    //      z = directionZ * t + originz
}