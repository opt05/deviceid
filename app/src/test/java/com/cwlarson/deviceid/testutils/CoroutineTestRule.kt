package com.cwlarson.deviceid.testutils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.*
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * CoroutineTestRule installs a [UnconfinedTestDispatcher] for [Dispatchers.Main].
 *
 * Since it is run under TestScope, you can directly launch coroutines on the CoroutineTestRule
 * as a [CoroutineScope]:
 *
 * ```
 * testScope.launch { aTestCoroutine() }
 * ```
 *
 * All coroutines started on CoroutineTestRule must complete (including timeouts) before the test
 * finishes, or it will throw an exception.
 *
 * When using CoroutineTestRule you should always invoke [runTest] on it to avoid creating two
 * instances of [TestCoroutineScheduler] or [TestScope] in your test:
 *
 * ```
 * @Test
 * fun usingRunTest() = runTest {
 *     aTestCoroutine()
 * }
 * ```
 *
 * You may call [StandardTestDispatcher] methods on CoroutineTestRule and they will control the
 * virtual-clock.
 *
 * ```
 * withContext(StandardTestDispatcher(testScheduler)) { // do some coroutines }
 * advanceUntilIdle() // run all pending coroutines until the dispatcher is idle
 * ```
 *
 * By default, CoroutineTestRule will be in a *unpaused* state.
 *
 * @param dispatcher if provided, or else [UnconfinedTestDispatcher] will be used.
 */
class CoroutineTestRule(
    val dispatcher: TestDispatcher = UnconfinedTestDispatcher(TestCoroutineScheduler())
) : TestWatcher() {

    override fun starting(description: Description) {
        super.starting(description)
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        super.finished(description)
        dispatcher.scheduler.advanceUntilIdle()
        Dispatchers.resetMain()
    }
}