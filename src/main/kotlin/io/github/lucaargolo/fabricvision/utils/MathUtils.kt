package io.github.lucaargolo.fabricvision.utils

object MathUtils {

    fun lerpColor(delta: Double, startColor: Int, endColor: Int): Int {
        // Ensure that delta is within the range [0.0, 1.0]
        val clampedDelta = delta.coerceIn(0.0, 1.0)

        // Extract the individual color channels
        val startAlpha = (startColor shr 24) and 0xFF
        val startRed = (startColor shr 16) and 0xFF
        val startGreen = (startColor shr 8) and 0xFF
        val startBlue = startColor and 0xFF

        val endAlpha = (endColor shr 24) and 0xFF
        val endRed = (endColor shr 16) and 0xFF
        val endGreen = (endColor shr 8) and 0xFF
        val endBlue = endColor and 0xFF

        // Interpolate the red, green, blue, and alpha components
        val interpolatedAlpha = (startAlpha + (endAlpha - startAlpha) * clampedDelta).toInt()
        val interpolatedRed = (startRed + (endRed - startRed) * clampedDelta).toInt()
        val interpolatedGreen = (startGreen + (endGreen - startGreen) * clampedDelta).toInt()
        val interpolatedBlue = (startBlue + (endBlue - startBlue) * clampedDelta).toInt()

        // Combine the color channels into the final color integer
        val interpolatedColor = (interpolatedAlpha shl 24) or
                (interpolatedRed shl 16) or
                (interpolatedGreen shl 8) or
                interpolatedBlue

        return interpolatedColor
    }


}