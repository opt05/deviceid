package com.cwlarson.deviceid.search

import com.cwlarson.deviceid.data.AllRepository
import com.cwlarson.deviceid.settings.PreferenceManager
import com.cwlarson.deviceid.testutils.CoroutineTestRule
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SearchViewModelTest {
    @get:Rule
    val coroutineRule = CoroutineTestRule()

    @get:Rule
    val mockkRule = MockKRule(this)

    @MockK(relaxed = true)
    lateinit var repository: AllRepository

    @MockK(relaxUnitFun = true)
    lateinit var preferenceManager: PreferenceManager

    @InjectMockKs
    lateinit var testObject: SearchViewModel

    @Test
    fun `Verify calls search when creating allItems with internal search flow`() = runTest {
        val argument = slot<StateFlow<String>>()
        verify(exactly = 1) { repository.search(capture(argument)) }
        assertEquals("", argument.captured.first())
    }

    @Test
    fun `Sets search text when called and calls search in repository`() = runTest {
        val argument = slot<StateFlow<String>>()
        verify(exactly = 1) { repository.search(capture(argument)) }
        testObject.setSearchText("test")
        assertEquals("test", argument.captured.first())
    }

    @Test
    fun `Verify force refresh is called when method is called`() = runTest {
        testObject.forceRefresh()
        verify(exactly = 1) { preferenceManager.forceRefresh() }
    }
}