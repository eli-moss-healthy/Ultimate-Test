# Ultimate Multi-Touch

<p align="center">
<img src="..." alt="logo" />

<br>
:zap: A powerful, easy to use, multifunctional & highly-customizable multi-touch library for Android :zap:
<br><br>
<a title="Release" target="_blank" href="..."><img src="..."></a>
<a title="API" target="_blank" href="https://android-arsenal.com/api?level=16"><img src="https://img.shields.io/badge/API-16%2B-green.svg?style=flat"></a>
</p>

<p align="center">
</p>

## Table of Contents

<!-- MarkdownTOC -->
1. [Features & Capabilities](#features--capabilities)
1. [Getting Started](#getting-started)
    - [Introduction](#introduction)
    - [Installation](#installation)
1. [Documentation](#documentation)
    - [Optionals](#optionals)
    - [Public API](#public-api)
    - [Callbacks](#callbacks)
    - [Special Classes](#special-classes)
    - [Implementing Touch Events & Gestures](#implementing-touch-events--gestures)
    - [Math Utils](#math-utils)
1. [Full Example](#full-example)
1. [Tests](#tests)
1. [License](#license)
<!-- /MarkdownTOC -->

## Features & Capabilities

✨ A highly-customizable multi-touch library for Android, based on FrameLayout.

* **All multi-touch gestures support**: Scaling(zoom), translation(dragging), rotation, double-tap and pinch-to-zoom gestures.
* **Highly-customizable**: Allows full control of your view's multi-touch capabilities. [(Optionals)](https://github.com/eli-moss-healthy/multitouch#optionals)
* **Touch point mappings & public api**: Get the coordinates of a touch point respective to the original view, i.e before multi touch manipulations [(Public API)](https://github.com/eli-moss-healthy/multitouch#public-api)
* **Seamlessly implement your own touch events & gestures**: Offers an extension function to easily and seamlessly implement touch events/gestures [(Detect Touches)](https://github.com/eli-moss-healthy/multitouch#implementing-touch-events--gestures)
* **Math**: Math utils to define and perform linear algebra calculations. Also, radians<>degrees conversions. [(Math Utils)](https://github.com/eli-moss-healthy/multitouch#math-utils)

## Getting Started
### Introduction
Ultimate MultiTouch library is basically a **FrameLayout** "on steroids".\
It detects touch gestures, and applies all the transformations (scale, translation, rotation) on the FrameLayout's child view at index indicated by ```currentTargetViewIndex```, which can be changed with the ```setTargetChildViewIndex``` function (see [Documentation](https://github.com/eli-moss-healthy/multitouch#public-api))

### Installation
```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com......:v1.0.0'
}
```

## Documentation
### Optionals
```kotlin
// zoom
var isZoomEnabled: Boolean
var isDoubleTapEnabled: Boolean
var minScale: Float
var maxScale: Float

// the effect when trying to scale-out more than the minimum scale ("bounce back animation")
var isBoundsLimitEffectEnabled: Boolean // enabling/disabling the effect
var boundsLimitEffectScaleFactor: Float // setting the effect/animation factor

// allowed dragging modes
var isOneFingerDraggingEnabled: Boolean
var isMultiFingerDraggingEnabled: Boolean

// rotation
var isRotationEnabled: Boolean
``` 

Usage Example :
1. Programmatically :
```kotlin
multiTouchFrameLayout.apply {
    isZoomEnabled = true
    isDoubleTapEnabled = true
    minScale = 1f
    maxScale = 4f
    isBoundsLimitEffectEnabled = true
    boundsLimitEffectFactor = 0.9f
    isOneFingerDraggingEnabled = true
    isMultiFingerDraggingEnabled = true
    isRotationEnabled = false
}
```    

2. XML :
```xml
<io.healthy.spot.app.view.multitouch.MultiTouchFrameLayout 
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:isZoomEnabled="true"
    app:isDoubleTapEnabled="true"
    app:minScale="1"
    app:maxScale="4"
    app:isBoundsLimitEffectEnabled="true"
    app:boundsLimitEffectFactor="0.9f"
    app:isOneFingerDraggingEnabled="true"
    app:isMultiFingerDraggingEnabled="true"
    app:isRotationEnabled="false"/>
```

### Public API
* ```kotlin
  var scale: Float
  ```
  Current scale(zoom) applied on child view at index currentTargetViewIndex (use setTargetChildViewIndex to change that index)
  
* ```kotlin
  var dx: Float
  ```
  Current x-axis translation applied
  
* ```kotlin
  var dy: Float
  ```
  Current y-axis translation applied

* ```kotlin
  fun setScale(newScale: Float)
  ```
  | Parameters | Description |
  | --- | --- |
  | `newScale` | **float**: New scale value to set |
  
* ```kotlin
  fun resetScale(animate: Boolean)
  ```
  | Parameters | Description |
  | --- | --- |
  | `animate` | **boolean**: Flag indicating whether to animate the scale change |
  
* ```kotlin
  fun isZoomed(): Boolean
  ```
  | Returns | description |
  | --- | --- |
  | **boolean** | true if any scale is applied on child view at index currentTargetViewIndex |
  
* ```kotlin
  fun isCurrentlyMultipleFingersDown(): Boolean
  ```
  | Returns | description |
  | --- | --- |
  | **boolean** | true if more than one finger are down on the screen |

* ```kotlin
  fun setTargetChildViewIndex(index: Int)
  ```
  | Parameters | Description |
  | --- | --- |
  | `index` | **Int**: index of child view for the transformations to be applied on|
* ```kotlin
  fun getTargetChildViewIndex()
  ```
  | Returns | Description |
  | --- | --- |
  | **int** | returns the index of child view on which the transformations are applied on|
* ```kotlin
  fun currentContentView(): View?
  ```
  | Returns | description |
  | --- | --- |
  | **View** | the child view at index currentTargetViewIndex |

* ```kotlin
  fun currentContentRectangle(): RectF?
  ```
  | Returns | description |
  | --- | --- |
  | **RectF** | the rectangle representing the transformed state of child view at index currentTargetViewIndex |

* ```kotlin
  fun getMappedPoint(touchPoint: PointF): PointF
  ```
  | Parameters | Description |
  | --- | --- |
  | `touchPoint` | **PointF**: touch point that needs to be mapped back to the original view's coordinates |
  
  | Returns | description |
  | --- | --- |
  | **PointF** | coordinates of the touch point corresponding to the original view |


* ```kotlin
  fun getPointBeforeRotation(x: Float, y: Float, rotationDegree: Float): PointF
  ```
  | Parameters | Description |
    | --- | --- |
  | `x` | **Float**: x-axis coordinate|
  | `y` | **Float**: y-axis coordinate|
  | `rotationDegree` | **Float**: rotation in degrees|

  | Returns | description |
  | --- | --- |
  | **PointF** | coordinates of the touch point corresponding to the view **before** the applying the given rotation degree |

* ```kotlin
  fun getPointAfterRotation(x: Float, y: Float, rotationDegree: Float): PointF
  ```
  | Parameters | Description |
  | --- | --- |
  | `x` | **Float**: x-axis coordinate|
  | `y` | **Float**: y-axis coordinate|
  | `rotationDegree` | **Float**: rotation in degrees|

  | Returns | description |
  | --- | --- |
  | **PointF** | coordinates of the touch point corresponding to the view **after** the applying the given rotation degree |


### Callbacks
* ```kotlin
  onScaleChanged(scale: Float)
  ```
* ```kotlin
  onScalingEnded(scalingType: ScalingType)
  ```
* ```kotlin
  onTranslated(dx: Float, dy: Float)
  ```
* ```kotlin
  onTap(e: MotionEvent, touchPointMappedToOriginal: PointF)
  ``` 
* ```kotlin
  onTouch(e: MotionEvent)
  ```
* ```kotlin
  onDrag(p1: TransformedPoint, p2: TransformedPoint?, isNowMultipleFingersDown: Boolean)
  ```

Usage Example :
  ```kotlin
  multiTouchFrameView.apply {
      onDrag { p1: TransformedPoint, p2: TransformedPoint?, isNowMultipleFingersDown: Boolean ->
          //
      }
      onScalingEnded { scale ->
          //
      }
  }
  ```

### Special Classes

* TransformedPoint - represents a touch point respective to its coordinates on the original view 
  ````kotlin 
  data class TransformedPoint(val originalPointF: PointF, val transformedPoint: PointF)
  ````
  - returned in onScroll callback
    
* ScalingType - represents the scaling operation performed.
  ````kotlin 
  sealed class ScalingType {
      data class Pinch(val isScalingIn: Boolean): ScalingType()
      data class DoubleTap(val isScalingIn: Boolean): ScalingType()
  }
  ````
  - contains two types :
    - ```data class Pinch``` - scaling/zoom was done by pinch-to-zoom gesture
    - ```data class DoubleTap``` - scaling/zoom was done by double tap gesture
  - ```isScalingIn``` - a boolean indicating whether the scaling operation is zooming in or out
  - returned in onScalingEnded callback

### Implementing Touch Events & Gestures
A convenient and simple extension function to implement touch events and gestures on any View.
````kotlin
View.detectTouches { ... }
```` 

Callbacks:
   ````kotlin
    onTouch: (e: MotionEvent)
    onScale: (scaleFactor: Float, focusX: Float, focusY: Float, spanDelta: Float)
    onScaleBegin: (scaleFactor: Float, focusX: Float, focusY: Float)
    onScaleEnd: (scaleFactor: Float, focusX: Float, focusY: Float)
    onTap: (e: MotionEvent)
    onScroll: (e1: MotionEvent, e2: MotionEvent?, distanceX: Float, distanceY: Float)
    onDoubleTap: (e: MotionEvent, focusX: Float, focusY: Float)
    onFling: (e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float)
    onLongPress: (e: MotionEvent)
   ````
Usage Example:
````kotlin
detectTouches {
    onScroll { e1, e2, distanceX, distanceY ->
        //
    }
    onScale { scaleFactor, focusX, focusY, spanDelta ->
        //
    }
    onDoubleTap { e, focusX, focusY ->
        //
    }
    onTouch { e ->
        //
    }
    onTap { e ->
        //
    }
    ....
}
````

### Math Utils
Math utils used in this library are open for general use.\
They mainly include functionality for linear algebra transformations and manipulations.
But also small utils such as conversion between radians and degrees. 

**Degrees<>Radians**
* Extension function that converts degrees to radians
   ````kotlin 
   Float.toRadians(): Float
  ````
* Extension function that converts radians to degrees
   ````kotlin 
   Float.toDegrees(): Float
   ````
**Linear Algebra (Matrices, Vectors, Multiplication operator)**

Entities :
````kotlin
typealias Vector = FloatArray // rows of matrix
typealias Matrix = Array<Vector> // array of rows
````

Helpful Methods :
* Builds and returns a matrix representation
   ````kotlin 
   fun matrix(vararg rows: Vector): Matrix
   ````
    Matrix multiplication example :
    ````kotlin
    val matrix1 = matrix(
                floatArrayOf(6f, -2f, 5f),
                floatArrayOf(-1f, 3f, -4f),
                floatArrayOf(7f, 0f, -3f))
    
    val matrix2 = matrix(
                floatArrayOf(1f, 6f, 5f),
                floatArrayOf(2f, 3f, 4f),
                floatArrayOf(-1f, 7f, 0f))
    
    val result = matrix1 * matrix2
    ````
* Build and returns a rotation matrix representation
   ````kotlin 
   fun rotationMatrix(degrees: Float): Matrix
   ````  
* Builds and returns a transpose of a rotation matrix representation
   ````kotlin 
   fun rotationMatrixTranspose(degrees: Float): Matrix
   ````  
  
## Full Example

````kotlin

val multiTouchFrame: MultiTouchFrameLayout

multiTouchFrame.apply {
    // Optionals 
    isDoubleTapEnabled = true
    isBoundsLimitEffectEnabled = false
    isMultiFingerDraggingEnabled = true
    // ... 
    
    // MultiTouchGestureListeners
    onDrag { p1: TransformedPoint, p2: TransformedPoint?, isNowMultipleFingersDown: Boolean ->
      //
    }
    onScalingEnded { scale ->
      //
    }
    // ...
}

val someLayout = FrameLayout(context).apply { addView(someView) }

multiTouchFrame.addView(someLayout)

// MultiTouchApi
if (multiTouchFrame.isZoomed()) {
    multiTouchFrame.resetScale(animate = true)
}
// ...

````

## Tests
Unit tests for math and point mapping functionality.

## License

Copyright 2022 Healthy.io, created by Eli Moss

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

> http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.