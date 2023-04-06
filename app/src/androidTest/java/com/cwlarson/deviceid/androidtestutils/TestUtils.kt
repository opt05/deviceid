package com.cwlarson.deviceid.androidtestutils

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
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

/**
 * Returns whether the node's text contains the given [Role].
 *
 * Note that in merged semantics tree there can be a list of items that got merged from
 * the child nodes. Typically an accessibility tooling will decide based on its heuristics which
 * ones to use.
 *
 * @param role Value to match as one of the items in the list of role values.
 *
 * @see SemanticsProperties.Role
 */
fun hasRole(role: Role) = SemanticsMatcher("${SemanticsProperties.Role.name} contains '$role'") {
    val roleProperty = it.config.getOrNull(SemanticsProperties.Role) ?: false
    roleProperty == role
}

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