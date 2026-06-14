package com.example.antigravityfinance.ui.main

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Before
import org.junit.Rule
import org.junit.Test
 
class MainScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Before
  fun setup() {
    composeTestRule.setContent { 
      MainScreen(onItemClick = {}) 
    }
  }

  @Test
  fun appStarts_andDisplaysSecurityPinGate() {
    // Verify that the startup security lock is shown on start
    composeTestRule.onNodeWithText("ANTIGRAVITY").assertExists()
    composeTestRule.onNodeWithText("Create Security PIN").assertExists()
  }
}
