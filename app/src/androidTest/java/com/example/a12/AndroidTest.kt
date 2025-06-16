package com.example.a12.pages

import android.app.Instrumentation
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents.*
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.a12.R
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ActivityTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun setup() {
        init()
    }

    @After
    fun tearDown() {
        release()
    }

    @Test
    fun launchTestButton_startsTestActivityWithExtras() {
        val intent = Intent(context, InfoTestActivity::class.java).apply {
            putExtra("TEST_ID", 1L)
        }

        ActivityScenario.launch<InfoTestActivity>(intent)

        intending(hasComponent(TestActivity::class.java.name))
            .respondWith(Instrumentation.ActivityResult(0, null))

        onView(withId(R.id.startTestContainer)).perform(click())

        intended(allOf(
            hasComponent(TestActivity::class.java.name),
            hasExtra("TEST_ID", 1L),
            hasExtra("REVIEW_MODE", false)
        ))
    }

    @Test
    fun testSwitchTabs_All_InProgress_Completed() {
        ActivityScenario.launch(LearningActivity::class.java)

        onView(withId(R.id.tabAll)).perform(click())
        onView(withId(R.id.tabAll)).check { view, _ ->
            assert(view.isSelected) { "Tab ALL should be selected" }
        }

        onView(withId(R.id.tabInProgress)).perform(click())
        onView(withId(R.id.tabInProgress)).check { view, _ ->
            assert(view.isSelected) { "Tab IN_PROGRESS should be selected" }
        }

        onView(withId(R.id.tabCompleted)).perform(click())
        onView(withId(R.id.tabCompleted)).check { view, _ ->
            assert(view.isSelected) { "Tab COMPLETED should be selected" }
        }
    }
}