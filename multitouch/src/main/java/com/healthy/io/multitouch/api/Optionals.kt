package com.healthy.io.multitouch.api


/**
 * An interface representing all the
 * core behavior customizations/configurations, that can be controlled by the user.
 *
 * @author Eli Moss
 * @date 2022
 */

interface Optionals {
    // zoom
    var isZoomEnabled: Boolean
    var isDoubleTapEnabled: Boolean
    var minScale: Float
    var maxScale: Float

    // return to bounds effect/anim when scale below min.
    var isBoundsLimitEffectEnabled: Boolean
    var boundsLimitEffectScaleFactor: Float

    // allowed dragging modes
    var isOneFingerDraggingEnabled: Boolean
    var isMultiFingerDraggingEnabled: Boolean

    // rotation
    var isRotationEnabled: Boolean
}
