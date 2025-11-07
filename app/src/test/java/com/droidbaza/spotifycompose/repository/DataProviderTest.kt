package com.droidbaza.spotifycompose.repository

import org.junit.Assert.assertNotNull
import org.junit.Test

class DataProviderTest {

    @Test
    fun `test categoriesBy returns non-empty list`() {
        val categories = DataProvider.categoriesBy()
        assertNotNull(categories)
        assert(categories.isNotEmpty())
    }

    @Test
    fun `test category data has valid structure`() {
        val categories = DataProvider.categoriesBy()
        val firstCategory = categories.firstOrNull()

        assertNotNull(firstCategory)
        assertNotNull(firstCategory?.title)
        assertNotNull(firstCategory?.data)
        assert(firstCategory?.data?.isNotEmpty() == true)
    }
}
