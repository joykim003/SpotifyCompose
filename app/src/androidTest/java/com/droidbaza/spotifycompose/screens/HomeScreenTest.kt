package com.droidbaza.spotifycompose.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalFoundationApi::class)
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun homeScreenDisplaysTopAppBar() {
        composeTestRule.setContent {
            HomeScreen()
        }

        // Verify top app bar is displayed
        composeTestRule.onNodeWithTag("TopAppBar").assertExists()
    }

    @Test
    fun homeScreenDisplaysCategories() {
        composeTestRule.setContent {
            HomeScreen()
        }

        // Verify categories section exists
        composeTestRule.onNodeWithTag("CategoriesGrid").assertExists()

        // Verify at least one category item is displayed
        val nodes = composeTestRule.onAllNodesWithTag("CategoryItem").fetchSemanticsNodes()
        assertTrue("Expected at least one category item", nodes.size >= 1)
    }
}
