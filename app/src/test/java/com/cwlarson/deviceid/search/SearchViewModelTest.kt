package com.cwlarson.deviceid.search

import com.cwlarson.deviceid.data.AllRepository
import com.cwlarson.deviceid.settings.PreferenceManager
import com.cwlarson.deviceid.testutils.CoroutineTestRule
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

class SearchViewModelTest {
    @get:Rule
    val coroutineRule = CoroutineTestRule()

    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    @Mock
    lateinit var repository: AllRepository

    @Mock
    lateinit var preferenceManager: PreferenceManager

    @InjectMocks
    lateinit var testObject: SearchViewModel

    @Test
    fun `Verify calls search when creating allItems with internal search flow`() = runTest {
        val captor = argumentCaptor<StateFlow<String>>()
        verify(repository).search(captor.capture())
        assertEquals("", captor.lastValue.first())
    }

    @Test
    fun `Sets search text when called and calls search in repository`() = runTest {
        val captor = argumentCaptor<StateFlow<String>>()
        verify(repository).search(captor.capture())
        testObject.setSearchText("test")
        assertEquals("test", captor.lastValue.first())
    }

    @Test
    fun `Verify force refresh is called when method is called`() = runTest {
        testObject.forceRefresh()
        verify(preferenceManager, times(1)).forceRefresh()
    }
}