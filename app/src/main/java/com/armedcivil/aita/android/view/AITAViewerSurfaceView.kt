package com.armedcivil.aita.android.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.vecmath.AxisAngle4d
import javax.vecmath.Matrix4d
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

class AITAViewerSurfaceView : SurfaceView, SurfaceHolder.Callback {
    private var running: Boolean = false
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
    private var floorBitmap: Bitmap? = null
    private var job: Job? = null
    private var centerX = 0.0
    private var centerY = 0.0

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle,
    ) {
        init(attrs, defStyle)
    }

    private fun init(
        attrs: AttributeSet?,
        defStyle: Int,
    ) {
        holder.addCallback(this)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        try {
            event ?: return false
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
                        scale = max(scale, 10f)
                        secondStartX = event.getX(event.findPointerIndex(1))
                        secondStartY = event.getY(event.findPointerIndex(1))
                    }
                }
            }
            return true
        } catch (e: Exception) {
            Log.d("ERROR", e.message!!)
            return true
        }
    }

    private fun run() {
        job =
            GlobalScope.launch {
                while (running) {
                    if (floorBitmap !== null) {
                        synchronized(holder) {
                            val canvas = holder.lockCanvas()
                            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                            val matrix = Matrix()
                            matrix.preScale(
                                scale / 250,
                                scale / 250,
                                0f,
                                0f,
                            )
                            matrix.postTranslate(
                                (offsetX + (width / 2) - (centerX * scale / 250).toFloat()),
                                (offsetY + (height / 2) - (centerY * scale / 250).toFloat()),
                            )
                            canvas.drawBitmap(
                                floorBitmap!!,
                                matrix,
                                null,
                            )
                            holder.unlockCanvasAndPost(canvas)
                        }
                    }
                }
            }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        running = true
        run()
    }

    override fun surfaceChanged(
        holder: SurfaceHolder,
        format: Int,
        width: Int,
        height: Int,
    ) {
        running = true
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        running = false
        job?.cancel()
        job = null
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
                captureFloor()
            }
        }
    }

    private fun captureFloor() {
        GlobalScope.launch {
            val points =
                floor!!.objects.flatMap { sceneObject ->
                    val bitmap = bitmapMap[sceneObject.topImagePath]
                    if (bitmap !== null) {
                        sceneObject.transformedPoints(bitmap.width, bitmap.height).toList()
                    } else {
                        listOf()
                    }
                }

            val minX = points.map { point -> point.x }.reduce { minX, x -> min(minX, x) }
            val minZ = points.map { point -> point.z }.reduce { minZ, z -> min(minZ, z) }
            val maxX = points.map { point -> point.x }.reduce { maxX, x -> max(maxX, x) }
            val maxZ = points.map { point -> point.z }.reduce { maxZ, z -> max(maxZ, z) }
            val width = (maxX - minX)
            val height = (maxZ - minZ)

            centerX = -minX
            centerY = -minZ

            val bitmap = Bitmap.createBitmap(width.toInt(), height.toInt(), Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.BLUE, PorterDuff.Mode.ADD)
            for (sceneObject in floor!!.objects) {
                val topImageBitmap = bitmapMap[sceneObject.topImagePath] ?: continue
                val matrix = Matrix()
                val axisAngle = AxisAngle4d()
                axisAngle.set(Matrix4d(sceneObject.matrix.elements.toDoubleArray()))
                matrix.preRotate(
                    (axisAngle.getAngle() * axisAngle.getY() * 180 / Math.PI).toFloat(),
                    (topImageBitmap.width / 2).toFloat(),
                    (topImageBitmap.height / 2).toFloat(),
                )
                matrix.postTranslate(
                    ((centerX) + (sceneObject.cx) - (topImageBitmap.width / 2)).toFloat(),
                    ((centerY) + (sceneObject.cz) - (topImageBitmap.height / 2)).toFloat(),
                )
                canvas.drawBitmap(
                    topImageBitmap.copy(Bitmap.Config.ARGB_8888, false),
                    matrix,
                    null,
                )
            }
            floorBitmap = bitmap
        }
    }
}

@Composable
fun AITAViewerSurface(floor: Floor) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context -> AITAViewerSurfaceView(context) },
        update = { view ->
            view.updateFloor(floor)
        },
    )
}
