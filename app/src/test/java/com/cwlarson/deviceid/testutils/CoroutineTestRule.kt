package com.cwlarson.deviceid.testutils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Sets the main coroutines dispatcher to a [TestCoroutineDispatcher] for unit testing. A
 * [TestCoroutineDispatcher] provides control over the execution of coroutines.
 *
 * Declare it as a JUnit Rule:
 *
 * ```
 * @get:Rule
 * var coroutineRule = CoroutineTestRule()
 * ```
 *
 * Use the test dispatcher variable to modify the execution of coroutines
 *
 * ```
 * // This pauses the execution of coroutines
 * coroutineRule.testDispatcher.pauseDispatcher()
 * ...
 * // This resumes the execution of coroutines
 * coroutineRule.testDispatcher.resumeDispatcher()
 * ...
 * // This executes the coroutines running on testDispatcher synchronously
 * runBlockingTest { }
 * ```
 */
class CoroutineTestRule(
    private val testDispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()
) : TestWatcher() {

    override fun starting(description: Description?) {
        super.starting(description)
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description?) {
        super.finished(description)
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }
}