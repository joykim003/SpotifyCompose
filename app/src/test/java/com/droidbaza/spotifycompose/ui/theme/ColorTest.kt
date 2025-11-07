package com.droidbaza.spotifycompose.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ColorTest {

    @Test
    fun `verify primary colors are different`() {
        assertNotEquals(
            "Primary colors should be distinct",
            Purple200.value,
            Purple500.value
        )
        assertNotEquals(
            "Primary colors should be distinct",
            Purple500.value,
            Purple700.value
        )
    }

    @Test
    fun `verify teal color is unique`() {
        val defaultTeal = Color(0xFF009688) // Material Design Teal color
        assertNotEquals(
            "Teal color should be distinct from default teal",
            Teal200.value,
            defaultTeal.value
        )
    }
}
