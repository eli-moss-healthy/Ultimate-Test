package com.healthy.io.multitouch

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Math local unit tests, to validate linear algebra transformations and calculations
 * and basic functionality like converting degrees to radians and vice-versa.
 */
class MathTest {

    /*
     * Checks conversion from degrees to radians
    */
    @Test
    fun `degrees to radians validation`() {
        val degrees = 30f
        val result = degrees.toRadians()
        assertEquals(0.5235988f, result)
    }

    /*
     * Checks conversion from radians to degrees
     */
    @Test
    fun `radians to degrees validation`() {
        val radians = 0.5235988f
        val result = radians.toDegrees()
        assertEquals(30f, result)
    }

    /**
     * with given rotation degrees, it returns a valid and correct matrix
     */
    @Test
    fun `rotation matrix validation`() {
        val expected = matrix(
            floatArrayOf(0.8660254f, -0.5f, 0f),
            floatArrayOf(0.5f, 0.8660254f, 0f),
            floatArrayOf(0f, 0f, 1f))

        assertArrayEquals(expected, rotationMatrix(30f))
    }

    /**
     * transpose rotation matrix -> when given rotation degrees, it returns correct matrix
     */
    @Test
    fun `rotation transpose matrix validation`() {
        val expected = matrix(
            floatArrayOf(0.8660254f, 0.5f, 0f),
            floatArrayOf(-0.5f, 0.8660254f, 0f),
            floatArrayOf(0f, 0f, 1f))

        assertArrayEquals(expected, rotationMatrixTranspose(30f))
    }

    /**
     * times operator - matrix multiplication -> with 2 given matrices, it returns valid matrix
     */
    @Test
    fun `matrix by matrix multiplication validation`() {
        val expected = matrix(
            floatArrayOf(-3f, 65f, 22f),
            floatArrayOf(9f, -25f, 7f),
            floatArrayOf(10f, 21f, 35f))

        val matrix1 = matrix(
            floatArrayOf(6f, -2f, 5f),
            floatArrayOf(-1f, 3f, -4f),
            floatArrayOf(7f, 0f, -3f))

        val matrix2 = matrix(
            floatArrayOf(1f, 6f, 5f),
            floatArrayOf(2f, 3f, 4f),
            floatArrayOf(-1f, 7f, 0f))

        assertArrayEquals(expected, matrix1 * matrix2)
    }

    /**
     * times operator - matrix multiplication -> with 2 given matrices, it returns valid matrix
     */
    @Test
    fun `vector by vector multiplication validation`() {
        val expected = matrix(floatArrayOf(32f))

        val vector1 = matrix(floatArrayOf(1f, 2f, 3f))

        val vector2 = matrix(floatArrayOf(4f),
                             floatArrayOf(5f),
                             floatArrayOf(6f))

        assertArrayEquals(expected, vector1 * vector2)
    }

    /**
     * times operator - matrix multiplication -> with 2 given matrices, it returns valid matrix
     */
    @Test
    fun `vector by matrix multiplication validation`() {
        val expected = matrix(floatArrayOf(83f, 63f, 37f, 75f))

        val vector = matrix(floatArrayOf(3f, 4f, 2f))
        val matrix = matrix(
            floatArrayOf(13f, 9f, 7f, 15f),
            floatArrayOf(8f, 7f, 4f, 6f),
            floatArrayOf(6f, 4f, 0f, 3f))

        assertArrayEquals(expected, vector * matrix)
    }

    /**
     * rotation with translation transpose -> checks that with given params, it returns valid matrix
     * checks [getPointBeforeRotation] function in MultiTouchFrameLayout
     */
    @Test
    fun `vector before rotation calculation`() {
        val expected = matrix(floatArrayOf(649.41956f, -451.0616f, 1f)) // 'minus' for android compatibility

        val rawTouchPointX = 808.9678f
        val rawTouchPointY = 508.89062f
        val degreesRotated = 33.42648f
        val dx = 632.89746f // fake pivot point + some translation applied
        val dy = 745.65393f // fake pivot point + some translation applied

        val result = getVectorBeforeRotation(
            x = rawTouchPointX,
            y = rawTouchPointY,
            dx = dx,
            dy = dy,
            rotationDegree = degreesRotated)

        assertArrayEquals(expected, result)
    }

    /**
     * rotation with translation transpose -> checks that with given params, it returns valid matrix
     * checks [getPointAfterRotation] function in MultiTouchFrameLayout
     */
    @Test
    fun `vector after rotation calculation`() {
        val expected = matrix(floatArrayOf(808.9677f),
                              floatArrayOf(-508.89062f), // 'minus' for android compatibility
                              floatArrayOf(1f))

        val beforeMappingPointX = 649.41956f
        val beforeMappingPointY = 451.0616f
        val rotationInDegrees = 33.42648f
        val dx = 632.89746f // fake pivot point + some translation applied
        val dy = 745.65393f // fake pivot point + some translation applied

        val result = getVectorAfterRotation(
            x = beforeMappingPointX,
            y = beforeMappingPointY,
            dx = dx,
            dy = dy,
            rotationDegree = rotationInDegrees)

        assertArrayEquals(expected, result)
    }
}
