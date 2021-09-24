package com.cwlarson.deviceid.androidtestutils

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.test.espresso.intent.Intents

/**
 * Asserts that the current semantics node has a long click action.
 *
 * Throws [AssertionError] if the node is doesn't have a long click action.
 */
fun SemanticsNodeInteraction.assertHasLongClickAction(): SemanticsNodeInteraction =
    assert(SemanticsMatcher.keyIsDefined(SemanticsActions.OnLongClick))

/**
 * Asserts that the current semantics node has no long click action.
 *
 * Throws [AssertionError] if the node is does have a long click action.
 */
fun SemanticsNodeInteraction.assertHasNoLongClickAction(): SemanticsNodeInteraction =
    assert(SemanticsMatcher.keyNotDefined(SemanticsActions.OnLongClick))

/**
 * Performs a long click action on the element represented by the given semantics node.
 */
fun SemanticsNodeInteraction.performLongClick(): SemanticsNodeInteraction =
    performGesture { longClick() }

inline fun intentsSafeRelease(block: () -> Unit) {
    try {
        Intents.init()
        block()
    } catch (e: Throwable) {
        // this would be simply `finally { Intents.release() }` but in that case we would hide the real cause
        try {
            Intents.release()
        } catch (e2: Throwable) {
            // Intents.release() failed, add cause and re-throw original
            generateSequence(e2) { it.cause }.last().initCause(e)
            throw e2
        }
        // Intents.release() terminated correctly, re-throw root cause
        throw e
    }
    // nothing failed yet, clean up (outside of try so we don't try to double-release)
    Intents.release()
}

/*
internal fun grantPermissionProgrammatically(
    permission: String,
    instrumentation: Instrumentation = InstrumentationRegistry.getInstrumentation()
) {
    if (Build.VERSION.SDK_INT < 28) {
        val fileDescriptor = instrumentation.uiAutomation.executeShellCommand(
            "pm grant ${instrumentation.targetContext.packageName} $permission"
        )
        fileDescriptor.checkError()
        fileDescriptor.close()
    } else {
        instrumentation.uiAutomation.grantRuntimePermission(
            instrumentation.targetContext.packageName, permission
        )
    }
}

*/
/*internal fun revokePermissionProgrammatically(
    permission: String,
    instrumentation: Instrumentation = InstrumentationRegistry.getInstrumentation()
) {
    if (Build.VERSION.SDK_INT < 28) {
        val fileDescriptor = instrumentation.uiAutomation.executeShellCommand(
            "pm revoke ${instrumentation.targetContext.packageName} $permission"
        )
        fileDescriptor.checkError()
        fileDescriptor.close()
    } else {
        instrumentation.uiAutomation.revokeRuntimePermission(
            instrumentation.targetContext.packageName, permission
        )
    }
}*//*


internal fun denyPermissionInDialog(
    instrumentation: Instrumentation = InstrumentationRegistry.getInstrumentation()
) {
    UiDevice.getInstance(instrumentation).findPermissionButton(
        when (Build.VERSION.SDK_INT) {
            in 24..28 -> "DENY"
            31 -> "Don"
            else -> "Deny"
        }
    ).clickForPermission(instrumentation)
}

private fun UiDevice.findPermissionButton(text: String): UiObject =
    findObject(
        UiSelector()
            .textStartsWith(text)
            .clickable(true)
            .className("android.widget.Button")
    )

private fun UiObject.clickForPermission(
    instrumentation: Instrumentation = InstrumentationRegistry.getInstrumentation()
): Boolean {
    waitUntil { exists() }
    if (!exists()) return false

    val clicked = waitUntil { exists() && click() }
    // Make sure that the tests waits for this click to be processed
    if (clicked) {
        instrumentation.waitForIdleSync()
    }
    return clicked
}

private fun waitUntil(timeoutMillis: Long = 2_000, condition: () -> Boolean): Boolean {
    val startTime = System.nanoTime()
    while (true) {
        if (condition()) return true
        // Let Android run measure, draw and in general any other async operations.
        Thread.sleep(10)
        if (System.nanoTime() - startTime > timeoutMillis * 1_000_000) {
            return false
        }
    }
}*/
