package com.armedcivil.aita.android.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.imageLoader
import coil.request.ImageRequest
import com.armedcivil.aita.android.data.Floor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.vecmath.AxisAngle4d
import javax.vecmath.Matrix4d
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Floor view
 */
class AITAViewer : View {
    private var floor: Floor? = null
    private val imageLoader: ImageLoader by lazy { context.imageLoader }
    private val bitmapMap: MutableMap<String, Bitmap> = mutableMapOf()
    private var offsetX = 0f
    private var offsetY = 0f
    private var startX = 0f
    private var startY = 0f
    private var secondStartX = 0f
    private var secondStartY = 0f
    private var startDistance = 0f
    private var scale = 10f

    constructor(context: Context) : super(context) {
        init(null, 0, context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0, context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle,
    ) {
        init(attrs, defStyle, context)
    }

    private fun init(
        attrs: AttributeSet?,
        defStyle: Int,
        context: Context,
    ) {
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        try {
            event ?: return true
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.getX(event.findPointerIndex(0))
                    startY = event.getY(event.findPointerIndex(0))
                }

                MotionEvent.ACTION_POINTER_DOWN -> {
                    startX = event.getX(event.findPointerIndex(0))
                    startY = event.getY(event.findPointerIndex(0))
                    secondStartX = event.getX(event.findPointerIndex(1))
                    secondStartY = event.getY(event.findPointerIndex(1))
                    startDistance =
                        sqrt((startX - secondStartX).pow(2) + (startY - secondStartY).pow(2))
                }

                MotionEvent.ACTION_MOVE -> {
                    val count = event.pointerCount
                    if (count < 1) {
                        return true
                    }
                    if (count == 1) {
                        val deltaX = event.getX(event.findPointerIndex(0)) - startX
                        val deltaY = event.getY(event.findPointerIndex(0)) - startY
                        offsetX += deltaX
                        offsetY += deltaY
                        startX = event.getX(event.findPointerIndex(0))
                        startY = event.getY(event.findPointerIndex(0))
                    } else {
                        val distance =
                            sqrt(
                                (
                                    event.getX(
                                        event.findPointerIndex(0),
                                    ) -
                                        event.getX(
                                            event.findPointerIndex(1),
                                        )
                                ).pow(2) +
                                    (
                                        event.getY(event.findPointerIndex(0)) -
                                            event.getY(
                                                event.findPointerIndex(1),
                                            )
                                    ).pow(2),
                            )
                        val deltaDistance = distance - startDistance
                        scale += deltaDistance / startDistance
                        Log.d("DEBUG", "scale : $scale")
                        secondStartX = event.getX(event.findPointerIndex(1))
                        secondStartY = event.getY(event.findPointerIndex(1))
                    }
                }
            }
            invalidate()
            return true
        } catch (e: Exception) {
            Log.d("ERROR", e.message!!)
            return true
        }
    }

    fun updateFloor(floorData: Floor) {
        this.floor = floorData
        GlobalScope.launch(Dispatchers.IO) {
            runBlocking {
                for (sceneObject in floorData.objects) {
                    if (bitmapMap.containsKey(sceneObject.topImagePath)) {
                        continue
                    }
                    val request =
                        ImageRequest.Builder(context)
                            .data("http://192.168.11.3:3001/${sceneObject.topImagePath}").build()
                    val response = async { imageLoader.execute(request) }
                    val drawable = response.await().drawable
                    if (drawable !== null) {
                        bitmapMap[sceneObject.topImagePath] = drawable.toBitmap()
                    }
                }
            }
            withContext(Dispatchers.Main) {
                invalidate()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (this.floor === null) {
            return
        }
        for (sceneObject in this.floor!!.objects) {
            val bitmap = bitmapMap[sceneObject.topImagePath] ?: continue
            val matrix = Matrix()
            val axisAngle = AxisAngle4d()
            axisAngle.set(Matrix4d(sceneObject.matrix.elements.toDoubleArray()))
            matrix.preRotate(
                (axisAngle.getAngle() * axisAngle.getY() * 180 / Math.PI).toFloat(),
                (bitmap.width / 2).toFloat(),
                (bitmap.height / 2).toFloat(),
            )
            matrix.preScale(
                scale / 250,
                scale / 250,
                (bitmap.width / 2).toFloat(),
                (bitmap.height / 2).toFloat(),
            )
            matrix.postTranslate(
                (offsetX + (width / 2) + (sceneObject.matrix.elements[12] * scale) - (bitmap.width / 2)).toFloat(),
                (offsetY + (height / 2) + (sceneObject.matrix.elements[14] * scale) - (bitmap.height / 2)).toFloat(),
            )
            canvas.drawBitmap(
                bitmap.copy(Bitmap.Config.ARGB_8888, false),
                matrix,
                null,
            )
        }
    }
}

@Composable
fun AITAViewer(floor: Floor) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context -> AITAViewer(context) },
        update = { view ->
            view.updateFloor(floor)
        },
    )
}
