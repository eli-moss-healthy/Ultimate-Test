package com.healthy.io.multitouch

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Math utils to define matrices & vectors, multiplication and perform affine transformations.
 * also provides convenience functionality like converting radians and degrees.
 *
 * @author Eli Moss
 * @date 2022
 */

typealias Vector = FloatArray // rows of matrix
typealias Matrix = Array<Vector> // array of rows

/**
 * Builds and returns a Matrix representation
 * @param rows representing rows in a matrix
 * @return Matrix built from the (rows) vectors given as params
 */
fun matrix(vararg rows: Vector): Matrix {
    val array = Array(rows.size) { FloatArray(rows[0].size){ 0f } }
    for ((idx, row) in rows.withIndex()) {
        array[idx] = row
    }
    return array
}

/**
 * Implements matrix multiplication
 * @receiver left-side matrix in the multiplication
 * @param other right-side matrix in the multiplication
 * @return Matrix representing the result of the multiplication of @receiver and @param
 */
operator fun Matrix.times(other: Matrix): Matrix {
    val rows1 = this.size
    val cols1 = this[0].size
    val rows2 = other.size
    val cols2 = other[0].size

    require(cols1 == rows2)

    val result = Matrix(rows1) { Vector(cols2) }
    for (i in 0 until rows1) {
        for (j in 0 until cols2) {
            for (k in 0 until rows2) {
                result[i][j] += this[i][k] * other[k][j]
            }
        }
    }
    return result
}


/**
 * Converts degrees to radians
 * @receiver Float representing angle in degrees
 * @return angle in radians, converted from degrees
 */
fun Float.toRadians(): Float {
    return ((this / 180f) * PI).toFloat()
}

/**
 * Radians angle to degrees
 * @receiver Float representing angle in Radians
 * @return angle in degrees, converted from radians
 */
fun Float.toDegrees(): Float {
    return ((this * 180f) / PI).toFloat()
}

/**
 * Builds a rotation matrix with a given angle
 * @param degrees angle
 * @return matrix representing an angle transformation
 */
fun rotationMatrix(degrees: Float): Matrix {
    return arrayOf(
        floatArrayOf(cos(degrees.toRadians()), -sin(degrees.toRadians()), 0f),
        floatArrayOf(sin(degrees.toRadians()), cos(degrees.toRadians()), 0f),
        floatArrayOf(0f, 0f, 1f)
    )
}

/**
 * Builds a matrix of a transpose of a rotation matrix with a given angle
 * Since a rotation matrix is an orthonormal matrix, we can use the transpose to get
 * the inverse transformation of the rotation (computationally much cheaper than finding the inverse)
 * @param degrees angle
 * @return transpose of a rotation matrix
 */
fun rotationMatrixTranspose(degrees: Float): Matrix {
    return arrayOf(
        floatArrayOf(cos(degrees.toRadians()), sin(degrees.toRadians()), 0f),
        floatArrayOf(-sin(degrees.toRadians()), cos(degrees.toRadians()), 0f),
        floatArrayOf(0f, 0f, 1f)
    )
}

/**
   Since we rotate around a center point which is not a
   fixed original center point, we must apply affine transformation -
   We do not want to change our point, but rather the coordinate system.
   Hence, we need to translate to the desired pivot point, then apply rotation,
   then translate back to original coordinate system.

   (*) invertible matrix <=> A * A^-1 = I
   to rotate : T(R(T^-1)) * P0 = P1
   to revert rotation :
   (*) transpose rule : (AB)^t = (B^t)(A^t)
        P1 * (T(R(T^-1)))^t = P0
     => P1 * (T^-1)^t * R^t * T^t = P0

   Usage example :
      (matrix(floatArrayOf(0.58578646f, 0.58578646f, 1f)) // vector representing the touch point
          * rotationMatrixWithTranslationTranspose(45f, 200f, 300f)) // (degrees, pivot point + any additional existing translation)
      => x = result[0][0] , y = result[0][1] // vector representing the result, i.e touch point after rotation back to 0.

 * @sample getPointBeforeRotation
 */
fun rotationWithTranslationMatrixTranspose(degrees: Float, dx: Float, dy: Float): Matrix {

    // translate original point to pivot point around which the view rotates
    val translationMatrixTranspose = matrix(
        floatArrayOf(1f, 0f, 0f),
        floatArrayOf(0f, 1f, 0f),
        floatArrayOf(dx, dy, 1f))

    // translate back to original center point of rotation (0,0)
    val translationInverseMatrixTranspose = matrix(
        floatArrayOf(1f, 0f, 0f),
        floatArrayOf(0f, 1f, 0f),
        floatArrayOf(-dx, -dy, 1f))

    return (translationInverseMatrixTranspose
            * rotationMatrixTranspose(degrees)
                * translationMatrixTranspose)
}

/**
   Since we rotate around a center point which is not a
   fixed original center point, we must apply affine transformation -
   We do not want to change our point, but rather the coordinate system.
   Hence, we need to translate to the desired pivot point, then apply rotation,
   then translate back to original coordinate system.

   (*) invertible matrix <=> A * A^-1 = I
   To rotate : T(R(T^-1)) * P0 = P1

   Usage example :
      (rotationMatrixWithTranslation(45f, 2f, 2f) -> (rotationDegrees, (point around which to rotate))
          * matrix(floatArrayOf(0f), -> (x point)
                   floatArrayOf(2f), -> (y point)
                   floatArrayOf(1f)))
       => x = result[0][0] , y = result[1][0]

 * @sample getPointAfterRotation
 */
fun rotationWithTranslationMatrix(degrees: Float, dx: Float, dy: Float): Matrix {

    // translate original point to pivot point around which the view rotates
    val translationMatrix = matrix(
        floatArrayOf(1f, 0f, dx),
        floatArrayOf(0f, 1f, dy),
        floatArrayOf(0f, 0f, 1f))

    // translate back to original center point of rotation (0,0)
    val translationInverseMatrix = matrix(
        floatArrayOf(1f, 0f, -dx),
        floatArrayOf(0f, 1f, -dy),
        floatArrayOf(0f, 0f, 1f))

    return (translationMatrix
            * rotationMatrix(degrees)
                * translationInverseMatrix)
}

/**
 * Finds the touch point coordinates on a rotated view if we revert/cancel the rotation
 * @param x axis touch point coordinate
 * @param y axis touch point coordinate
 * @param dx x translation of the view
 * @param dy y translation of the view
 * @param rotationDegree the applied rotation on the view
 * @return Point representing coordinates after applying rotation transformation
 */
fun getVectorBeforeRotation(x: Float, y: Float,
                            dx: Float, dy: Float,
                            rotationDegree: Float): Matrix {
    return (matrix(floatArrayOf(x, -y, 1f))
            * rotationWithTranslationMatrixTranspose(rotationDegree, dx, -dy))
}


/**
 * Finds the point coordinates after applying a rotation transformation on a view
 * @param x axis touch point coordinate
 * @param y axis touch point coordinate
 * @param dx x translation of the view
 * @param dy y translation of the view
 * @param rotationDegree the applied rotation on the view
 * @return Point representing coordinates after applying rotation transformation
 */
fun getVectorAfterRotation(x: Float, y: Float,
                                  dx: Float, dy: Float,
                                  rotationDegree: Float): Matrix {
    return (rotationWithTranslationMatrix(-rotationDegree, dx, -dy)
            * matrix(floatArrayOf(x),
                     floatArrayOf(-y),
                     floatArrayOf(1f)))
}
