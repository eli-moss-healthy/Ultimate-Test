package com.healthy.io.multitouch.core

import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Matrix
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.core.animation.doOnEnd
import com.healthy.io.multitouch.R
import com.healthy.io.multitouch.api.Optionals
import com.healthy.io.multitouch.detectTouches
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min

// Defaults
private const val ANIM_ZOOM_DURATION: Long = 350
private const val MAX_SCALE_FACTOR = 4f
private const val MIN_SCALE_FACTOR = 1f
private const val BOUNDS_LIMIT_EFFECT_SCALE_FACTOR = 0.9f
private const val SPAN_SCALE_THRESHOLD = 3.8f

/**
 * Represents a scale/zoom action on the view.
 * Contains whether the zoom/scale change was done by pinch-to-zoom or double-tap gesture.
 * Contains whether the zoom/scale change is zoom-in or zoom-out.
 */
sealed class ScalingType {
    data class Pinch(val isScalingIn: Boolean): ScalingType()
    data class DoubleTap(val isScalingIn: Boolean): ScalingType()
}

/**
 * This class holds all the logic of multi-touch gestures.
 * It applies the multi-touch transformations on a child view
 * represented by the [MultiTouchBaseLayoutParent.currentTargetViewIndex] index.
 *
 * @author Eli Moss
 * @date 2022
 */
abstract class MultiTouchBaseLayoutParent @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), Listener, Optionals {

    // state representing a child view
    data class State(val contentRect: RectF, var rotationDegree: Float = 0f)

    // optionals
    override var minScale = MIN_SCALE_FACTOR
        set(value) {
            if (value <= 0f) return
            field = value
        }
    override var isZoomEnabled = true
    override var isDoubleTapEnabled = true
    override var maxScale = MAX_SCALE_FACTOR
    override var isBoundsLimitEffectEnabled = false
    override var boundsLimitEffectScaleFactor = BOUNDS_LIMIT_EFFECT_SCALE_FACTOR
    override var isOneFingerDraggingEnabled = true
    override var isMultiFingerDraggingEnabled = true
    override var isRotationEnabled = false

    // helpers
    protected val mappingMatrix = Matrix()
    private val animator = ValueAnimator().apply { duration = ANIM_ZOOM_DURATION }
    private var lastDegrees = 0f
    private var fingersDownCount = 0
    protected var isMultipleFingersDown: Boolean = false
        private set
        get() = (fingersDownCount > 1)

    private var lastScale = minScale

    // target view to apply on
    protected var currentTargetViewIndex = 0

    protected fun currentView(): View? = getChildAt(currentTargetViewIndex)

    protected fun View.contentRect(): RectF? = getContentState()?.contentRect

    private fun View.initContentRect() = getContentState() ?: run { setContentState(State(RectF(0f, 0f, width.toFloat(), height.toFloat()))) }

    class WrapperTag(var contentState: State, var tag: Any? = null)

    // public use

    override fun getTag(): Any? {
        return (super.getTag() as? WrapperTag)?.tag ?: super.getTag()
    }

    override fun setTag(tag: Any) {
        val wrapperTag = (getTag() as? WrapperTag)
        wrapperTag?.tag = tag
        super.setTag(wrapperTag)
    }

    // internal use

    protected fun getContentState(): State? {
        return (tag as? WrapperTag)?.contentState
    }

    private fun setContentState(state: State) {
        super.setTag(
            (tag as? WrapperTag)
            ?.let { it.contentState = state } // update existing
            ?: WrapperTag(contentState = state) // or create new
        )
    }

    override fun addView(child: View) {
        super.addView(child)
        if (child.tag == null) afterMeasured { child.initContentRect() }
    }

    init {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.MultiTouchBaseLayoutParent)
            parseAttributes(typedArray)
            typedArray.recycle()
        }

        detectTouches {
            onScroll { e1, e2, distanceX, distanceY ->
                if (isZoomEnabled) handleScroll(e1, e2, distanceX, distanceY)
            }
            onScale { scaleFactor, focusX, focusY, spanDelta ->
                if (isZoomEnabled) handleScale(scaleFactor, focusX, focusY, spanDelta)
            }
            onScaleBegin { _, _, _  ->
                lastScale = currentView()?.scaleX ?: lastScale
            }
            onScaleEnd { _, _, _ ->
                if (isZoomEnabled) {
                    correctScaleIfNeeded(true)
                    onScalingEnded(ScalingType.Pinch(currentView()?.scaleX!! > lastScale))
                }
            }
            onDoubleTap { _, focusX, focusY ->
                if (isZoomEnabled && isDoubleTapEnabled) animateZoomEntirely(focusX, focusY)
            }
            onTouch { e ->
                parseMotion(e)
                if (isRotationEnabled && isMultipleFingersDown) {
                    handleRotation(e)
                }
                onTouch(e)
            }
            onTap { e ->
                onTap(e)
            }
        }
    }

    /* scrolling & scaling */

    private fun handleScroll(e1: MotionEvent, e2: MotionEvent?, distanceX: Float, distanceY: Float) {

        onScroll(e1, e2, isMultipleFingersDown)

        if (!isMultipleFingersDown && !isOneFingerDraggingEnabled) return
        if (isMultipleFingersDown && !isMultiFingerDraggingEnabled) return

        currentView()?.translateBy(-distanceX, -distanceY)
    }

    private fun handleScale(scaleFactor: Float, focusX: Float, focusY: Float, spanDelta: Float) {
        // detect scroll intention to help reduce "trembling" on edges when scrolling w/ 2 fingers
        if (spanDelta < SPAN_SCALE_THRESHOLD && isMultipleFingersDown) return

        currentView()?.run {
            val previousRect = RectF(contentRect())

            /* scaling/zooming */
            scaleBy(scaleFactor)

            /* scrolling for adjusting pinch-to-zoom focus */

            // mapping/finding the focus point on the rect after applying scale
            val pts = mappingMatrix.getMappedTouchPoint(
                floatArrayOf(focusX, focusY),
                previousRect,
                contentRect()!!
            )

            // translate the desired point on scaled view to the point touched on screen
            val dx = (focusX - pts[0])
            val dy = (focusY - pts[1])
            translateBy(dx, dy)
        }
    }

    protected fun View.translateBy(dx: Float, dy: Float) {
        // clamping / panning
        val maxDx = abs((contentRect()!!.width() - width)) / 2f
        val maxDy = abs((contentRect()!!.height() - height)) / 2f

        val correctDx = max(min(dx, maxDx - translationX), (-maxDx - translationX))
        val correctDy = max(min(dy, maxDy - translationY), (-maxDy - translationY))

        translationX += correctDx
        translationY += correctDy

        // update view content rect
        contentRect()?.offset(correctDx, correctDy)

        // notify
        onTranslated(translationX, translationY)
    }

    protected fun View.scaleBy(scaleFactor: Float) {
        val previousScale = scaleX
        val scale: Float
        if (isBoundsLimitEffectEnabled) {
            scale = max(boundsLimitEffectScaleFactor * minScale, min(scaleX * scaleFactor, maxScale))
        } else {
            scale = max(minScale, min(scaleX * scaleFactor, maxScale))
        }
        scaleX = scale
        scaleY = scale

        // update view content rect
        contentRect()?.scaleBy(scaleX / previousScale)

        // notify
        onScaleChanged(scaleX)
    }

    private fun RectF.scaleBy(scaleFactor: Float) {
        val widthMargins = (width() * scaleFactor - width()) / 2f
        val heightMargins = (height() * scaleFactor - height()) / 2f

        left -= widthMargins
        top -= heightMargins
        right += widthMargins
        bottom += heightMargins
    }

    protected fun View.centralizeView() = translateBy(-translationX, -translationY)

    protected fun Matrix.getMappedTouchPoint(point: FloatArray, sourceRect: RectF, destRect: RectF): FloatArray {
        reset()
        setRectToRect(
            sourceRect,
            destRect,
            Matrix.ScaleToFit.CENTER
        )
        mapPoints(point)
        return point
    }

    private fun correctScaleIfNeeded(animate: Boolean) {
        currentView() ?: return
        if (currentView()!!.scaleX < minScale) {
            if (currentView()!!.scaleX < 1f && minScale < 1f) {
                animateScaleOut() // scale only (leaving the view at its place - NOT centralizing)
            }
            else if (animate) {
                animateZoomOut() // scale + translation (centralizing view in center)
            }
            else {
                currentView().apply {
                    val targetScale = if (minScale > 1f) minScale else 1f
                    scaleBy(targetScale / scaleX)
                    translateBy(-translationX, -translationY)
                }
            }
        }
    }

    /* animated scales (double tap zoom) */

    private fun animateZoomEntirely(focusX: Float, focusY: Float) {
        currentView()?.let {
            when {
                it.scaleX > 1f -> animateZoomOut()
                else -> animateZoomIn(focusX, focusY)
            }
        }
    }

    private fun animateZoomIn(focusX: Float, focusY: Float) {
        currentView()?.run {
            val targetScale = if (scaleX < 1f) 1f else maxScale
            val pts = mappingMatrix.getMappedTouchPoint(
                floatArrayOf(focusX, focusY),
                contentRect()!!,
                RectF(contentRect()).apply { scaleBy(targetScale/scaleX) }
            )

            val scaling = PropertyValuesHolder.ofFloat("scale", scaleX, targetScale)
            val translateX = PropertyValuesHolder.ofFloat("translateX", 0f, focusX - pts[0])
            val translateY = PropertyValuesHolder.ofFloat("translateY", 0f, focusY - pts[1])

            animator.run {
                removeAllListeners()
                setValues(scaling, translateX, translateY)
                addUpdateListener { animation ->
                    val scaleFactor = (animation.getAnimatedValue("scale") as Float / scaleX)
                    val dx = (animation.getAnimatedValue("translateX") as Float) - translationX
                    val dy = (animation.getAnimatedValue("translateY") as Float) - translationY

                    scaleBy(scaleFactor)
                    translateBy(dx, dy)
                }
                doOnEnd { onScalingEnded(ScalingType.DoubleTap(true)) }

                start()
            }
        }
    }

    // scale + translation => essentially centralizes the view inside the frame.
    protected fun animateZoomOut() {
        currentView()?.run {
            val targetScale = if (scaleX < 1f) { // => for the effect of bounds limit when in negative scale (isBoundsLimitEffectEnabled = true)
                minScale
            } else { // zoom out to 1f, unless minScale (lower end) is more than 1f
                max(minScale, 1f)
            }

            val scaling = PropertyValuesHolder.ofFloat("scale", scaleX, targetScale)
            val translateX = PropertyValuesHolder.ofFloat("translateX", translationX, 0f)
            val translateY = PropertyValuesHolder.ofFloat("translateY", translationY, 0f)

            animator.run {
                removeAllListeners()
                setValues(scaling, translateX, translateY)
                addUpdateListener { animation ->
                    val scaleFactor = (animation.getAnimatedValue("scale") as Float / scaleX)
                    val dx = (animation.getAnimatedValue("translateX") as Float) - translationX
                    val dy = (animation.getAnimatedValue("translateY") as Float) - translationY

                    scaleBy(scaleFactor)
                    translateBy(dx, dy)
                }
                doOnEnd { onScalingEnded(ScalingType.DoubleTap(false)) }

                start()
            }
        }
    }

    // scale only, used for negative scale + isBoundsLimitEffectEnabled enabled,
    // to show the animation effect without centralizing the view, but rather leaving it at its place
    private fun animateScaleOut() {
        currentView()?.run {
            val targetScale = if (scaleX < 1f) { // => for the effect of bounds limit when in negative scale (isBoundsLimitEffectEnabled = true)
                minScale
            } else { // zoom out to 1f, unless minScale (lower end) is more than 1f
                max(minScale, 1f)
            }

            val scaling = PropertyValuesHolder.ofFloat("scale", scaleX, targetScale)

            animator.run {
                removeAllListeners()
                setValues(scaling)
                addUpdateListener { animation ->
                    val scaleFactor = (animation.getAnimatedValue("scale") as Float / scaleX)
                    scaleBy(scaleFactor)
                }
                doOnEnd { onScalingEnded(ScalingType.DoubleTap(false)) }

                start()
            }
        }
    }

    /* rotation */

    private fun handleRotation(e: MotionEvent) {
        when (e.actionMasked) {
            MotionEvent.ACTION_POINTER_DOWN -> {
                if (e.pointerCount == 2) {
                    currentView()?.let {
                        lastDegrees = calculateDegree(e)
                        lastDegrees -= it.rotation
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (isMultipleFingersDown && e.pointerCount > 1) {
                    val degrees = -(lastDegrees - calculateDegree(e))
                    currentView()?.apply {
                        rotation = degrees
                        (tag as? State)?.rotationDegree = degrees
                    }
                }
            }
        }
    }

    private fun calculateDegree(event: MotionEvent): Float {
        val deltaX = (event.getX(0) - event.getX(1))
        val deltaY = (event.getY(0) - event.getY(1))
        val radians = atan2(deltaY, deltaX)

        return Math.toDegrees(radians.toDouble()).toFloat()
    }

    /* init & helpers */

    private fun parseAttributes(typedArray: TypedArray) {
        if (typedArray.hasValue(R.styleable.MultiTouchBaseLayoutParent_isZoomEnabled)) {
            isZoomEnabled = typedArray.getBoolean(R.styleable.MultiTouchBaseLayoutParent_isZoomEnabled, true)
        }
        if (typedArray.hasValue(R.styleable.MultiTouchBaseLayoutParent_isDoubleTapEnabled)) {
            isDoubleTapEnabled = typedArray.getBoolean(R.styleable.MultiTouchBaseLayoutParent_isDoubleTapEnabled, true)
        }
        if (typedArray.hasValue(R.styleable.MultiTouchBaseLayoutParent_minScale)) {
            minScale = typedArray.getFloat(R.styleable.MultiTouchBaseLayoutParent_minScale, MIN_SCALE_FACTOR)
        }
        if (typedArray.hasValue(R.styleable.MultiTouchBaseLayoutParent_maxScale)) {
            maxScale = typedArray.getFloat(R.styleable.MultiTouchBaseLayoutParent_maxScale, MAX_SCALE_FACTOR)
        }
        if (typedArray.hasValue(R.styleable.MultiTouchBaseLayoutParent_isBoundsLimitEffectEnabled)) {
            isBoundsLimitEffectEnabled = typedArray.getBoolean(R.styleable.MultiTouchBaseLayoutParent_isBoundsLimitEffectEnabled, true)
        }
        if (typedArray.hasValue(R.styleable.MultiTouchBaseLayoutParent_boundsLimitEffectFactor)) {
            boundsLimitEffectScaleFactor = typedArray.getFloat(R.styleable.MultiTouchBaseLayoutParent_boundsLimitEffectFactor, BOUNDS_LIMIT_EFFECT_SCALE_FACTOR)
        }
        if (typedArray.hasValue(R.styleable.MultiTouchBaseLayoutParent_isOneFingerDraggingEnabled)) {
            isOneFingerDraggingEnabled = typedArray.getBoolean(R.styleable.MultiTouchBaseLayoutParent_isOneFingerDraggingEnabled, true)
        }
        if (typedArray.hasValue(R.styleable.MultiTouchBaseLayoutParent_isMultiFingerDraggingEnabled)) {
            isMultiFingerDraggingEnabled = typedArray.getBoolean(R.styleable.MultiTouchBaseLayoutParent_isMultiFingerDraggingEnabled, true)
        }
        if (typedArray.hasValue(R.styleable.MultiTouchBaseLayoutParent_isRotationEnabled)) {
            isRotationEnabled = typedArray.getBoolean(R.styleable.MultiTouchBaseLayoutParent_isRotationEnabled, true)
        }
    }

    private fun parseMotion(e: MotionEvent) {
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> fingersDownCount = 1
            MotionEvent.ACTION_POINTER_DOWN -> fingersDownCount ++
            MotionEvent.ACTION_POINTER_UP -> fingersDownCount --
            MotionEvent.ACTION_UP -> fingersDownCount = 0
        }
    }

    private inline fun View.afterMeasured(crossinline f: () -> Unit) {
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                f()
            }
        })
    }
}

/* internal use interface */
internal interface Listener {
    fun onScaleChanged(scale: Float)
    fun onScalingEnded(type: ScalingType)
    fun onTranslated(dx: Float, dy: Float)
    fun onTap(e: MotionEvent)
    fun onTouch(e: MotionEvent)
    fun onScroll(e1: MotionEvent, e2: MotionEvent?, isCurrentlyInMultiTouch: Boolean)
}
