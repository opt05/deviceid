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
    performTouchInput { longClick() }

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