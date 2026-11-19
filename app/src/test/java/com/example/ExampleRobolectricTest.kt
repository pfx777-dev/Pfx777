package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h915dp")
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appNameMatchesVardaanCreation() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("VARDAAN CREATION", appName)
    }

    @Test
    fun homeOpensDirectlyWithoutLoginAndNavigatesToImageStudio() {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("VARDAAN CREATION").assertIsDisplayed()
        composeTestRule.onNodeWithTag("home_image_studio_card").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("home_video_studio_card").performScrollTo().assertIsDisplayed()

        // Search tools right on Home screen
        composeTestRule.onNodeWithTag("home_search_tools_input").performScrollTo().performTextInput("Enhancer")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("search_result_prompt_enhancer")
            .performScrollTo()
            .assertIsDisplayed()

        // Open Image Studio directly via bottom nav
        composeTestRule.onNodeWithTag("bottom_nav_image_studio").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("AI Image Studio").assertIsDisplayed()
        composeTestRule.onNodeWithTag("image_studio_list")
            .performScrollToNode(hasTestTag("generate_image_button"))
        composeTestRule.onNodeWithTag("generate_image_button").assertIsDisplayed()
    }
}
