package com.armedcivil.aita.android.data

import android.os.Parcelable
import com.armedcivil.aita.android.view.AITAViewerSurfaceView
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import javax.vecmath.AxisAngle4d
import javax.vecmath.Matrix4d
import javax.vecmath.Point3d
import kotlin.math.cos
import kotlin.math.sin

@Parcelize
data class Floor(val label: String, val objects: Array<SceneObject>) : Parcelable

@Parcelize
data class SceneObject(
    val modelPath: String,
    val matrix: Matrix4,
    val isChair: Boolean,
    val id: String,
    val topImagePath: String,
) : Parcelable {
    @IgnoredOnParcel
    val cx get() = matrix.elements[12] * AITAViewerSurfaceView.SCALE_IN_TOP_IMAGE

    @IgnoredOnParcel
    val cz get() = matrix.elements[14] * AITAViewerSurfaceView.SCALE_IN_TOP_IMAGE

    private fun points(
        width: Int,
        height: Int,
    ): ArrayList<Point3d> {
        return arrayListOf(
            Point3d(cx - width / 2, 0.0, cz - height / 2),
            Point3d(cx + width / 2, 0.0, cz - height / 2),
            Point3d(cx - width / 2, 0.0, cz + height / 2),
            Point3d(cx + width / 2, 0.0, cz + height / 2),
        )
    }

    private fun matrix4d(): Matrix4d {
        return Matrix4d(this.matrix.elements.toDoubleArray())
    }

    fun transformedPoints(
        width: Int,
        height: Int,
    ): List<Point3d> {
        return this.points(width, height).map { point ->
            val theta =
                AxisAngle4d().let {
                    it.set(matrix4d())
                    it.getY() * it.getAngle()
                }
            point.apply {
                val px = x
                val pz = z
                x = cx + (px - cx) * cos(theta) - (pz - cz) * sin(theta)
                z = cz + (px - cx) * sin(theta) + (pz - cz) * cos(theta)
            }
            point
        }
    }
}

@Parcelize
data class Matrix4(val elements: Array<Double>) : Parcelable
