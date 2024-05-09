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
import com.armedcivil.aita.android.data.SceneObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.vecmath.AxisAngle4d
import javax.vecmath.Matrix4d
import javax.vecmath.Point3d
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sign
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
    private var floorWorldOriginX = 0.0
    private var floorWorldOriginY = 0.0
    private var onClickSheet: ((sceneObject: SceneObject) -> Unit)? = null

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
                    startDistance = distance(startX, startY, secondStartX, secondStartY)
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
                            distance(
                                event.getX(event.findPointerIndex(0)),
                                event.getY(event.findPointerIndex(0)),
                                event.getX(event.findPointerIndex(1)),
                                event.getY(event.findPointerIndex(1)),
                            )
                        val deltaDistance = distance - startDistance
                        scale += deltaDistance / startDistance
                        scale = max(scale, 10f)
                        secondStartX = event.getX(event.findPointerIndex(1))
                        secondStartY = event.getY(event.findPointerIndex(1))
                    }
                }

                MotionEvent.ACTION_UP -> {
                    val x = event.getX(event.findPointerIndex(0))
                    val y = event.getY(event.findPointerIndex(0))
                    if (floor !== null) {
                        floor!!.objects.forEach { sceneObject ->
                            val bitmap = bitmapMap[sceneObject.topImagePath]
                            if (bitmap !== null) {
                                val points =
                                    sceneObject.transformedPoints(bitmap.width, bitmap.height)
                                        .map { point ->
                                            point.apply {
                                                this.x =
                                                    offsetX + (this.x * scale / 250) + (width / 2)
                                                this.z =
                                                    offsetY + (this.z * scale / 250) + (height / 2)
                                            }
                                        }
                                if (sceneObject.isChair && isIntersect(x, y, points)) {
                                    Log.d("DEBUG", sceneObject.toString())
                                    onClickSheet?.invoke(sceneObject)
                                }
                            }
                        }
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
                Log.d("SURFACE", "描画ループを開始します")
                while (running) {
                    if (floorBitmap !== null) {
                        Log.d("SURFACE", "Canvas を lock します")
                        val canvas = holder.lockCanvas()
                        Log.d("SURFACE", "Canvas を lock しました")
                        canvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                        val matrix = Matrix()
                        matrix.preScale(
                            scale / 250,
                            scale / 250,
                            0f,
                            0f,
                        )
                        matrix.postTranslate(
                            (offsetX + (width / 2) - (floorWorldOriginX * scale / 250).toFloat()),
                            (offsetY + (height / 2) - (floorWorldOriginY * scale / 250).toFloat()),
                        )
                        canvas?.drawBitmap(
                            floorBitmap!!,
                            matrix,
                            null,
                        )
                        Log.d("SURFACE", "Canvas を unlock します")
                        if (canvas !== null) {
                            holder.unlockCanvasAndPost(canvas)
                        }

                        Log.d("SURFACE", "Canvas を unlock しました")
                    }
                }
                Log.d("SURFACE", "描画ループが終了しました")
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
        Log.d("SURFACE", "surfaceDestroyed が呼ばれました")
        running = false
        Log.d("SURFACE", "job を cancel します")
        job?.cancel()
        Log.d("SURFACE", "job を cancel しました")
        while (job !== null && !job!!.isCompleted) {
        } // 描画ループが終わるまで待つ
        Log.d("SURFACE", "描画ループが完了しました")
        job = null
    }

    fun setOnClickSheet(onClickSheet: ((sceneObject: SceneObject) -> Unit)) {
        this.onClickSheet = onClickSheet
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
        if (floor!!.objects.isEmpty()) {
            return
        }
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

            floorWorldOriginX = -minX
            floorWorldOriginY = -minZ

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
                    ((floorWorldOriginX) + (sceneObject.cx) - (topImageBitmap.width / 2)).toFloat(),
                    ((floorWorldOriginY) + (sceneObject.cz) - (topImageBitmap.height / 2)).toFloat(),
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

    private fun isIntersect(
        px: Float,
        pz: Float,
        pointArray: List<Point3d>,
    ): Boolean {
        var degree = 0.0
        pointArray.forEachIndexed { index, point ->
            val p2x = point.x
            val p2z = point.z
            var p3x = 0.0
            var p3z = 0.0
            if (index < pointArray.size - 1) {
                p3x = pointArray[index + 1].x
                p3z = pointArray[index + 1].z
            } else {
                p3x = pointArray[0].x
                p3z = pointArray[0].z
            }

            val ax = p2x - px
            val az = p2z - pz
            val bx = p3x - px
            val bz = p3z - pz
            val cos = (ax * bx + az * bz) / (sqrt(ax * ax + az * az) * sqrt(bx * bx + bz * bz))
            val sign = sign(ax * bz - az * bx)
            degree += sign * (acos(cos) * 180 / Math.PI)
        }
        return round(abs(degree)) == 360.0
    }

    private fun distance(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
    ): Float {
        return sqrt((x1 - x2).pow(2) + (y1 - y2).pow(2))
    }
}

@Composable
fun AITAViewerSurface(
    floor: Floor,
    onClickSheet: (sceneObject: SceneObject) -> Unit,
) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context -> AITAViewerSurfaceView(context).apply { setOnClickSheet(onClickSheet) } },
        update = { view ->
            view.updateFloor(floor)
        },
    )
}
