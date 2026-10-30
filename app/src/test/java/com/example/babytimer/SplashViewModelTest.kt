package com.example.babytimer

import com.example.babytimer.data.BabyPreferencesRepository
import com.example.babytimer.ui.splash.SplashViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeBabyPreferencesRepository : BabyPreferencesRepository {
        private val _babyName = MutableStateFlow("Baby")
        private val _photoUri = MutableStateFlow<String?>(null)
        private val _normalTimerEndTime = MutableStateFlow(0L)
        private val _reusableTimerEndTime = MutableStateFlow(0L)

        override val babyName = _babyName.asStateFlow()
        override val photoUri = _photoUri.asStateFlow()
        override val normalTimerEndTime = _normalTimerEndTime.asStateFlow()
        override val reusableTimerEndTime = _reusableTimerEndTime.asStateFlow()

        override suspend fun updateBabyName(name: String) {
            _babyName.value = name
        }

        override suspend fun updatePhotoUri(uri: String?) {
            _photoUri.value = uri
        }

        override suspend fun updateNormalTimerEndTime(time: Long) {
            _normalTimerEndTime.value = time
        }

        override suspend fun updateReusableTimerEndTime(time: Long) {
            _reusableTimerEndTime.value = time
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_isDefault() = runTest {
        val repository = FakeBabyPreferencesRepository()
        val viewModel = SplashViewModel(repository)

        assertEquals("Baby", viewModel.babyName.value)
        assertEquals(null, viewModel.photoUri.value)
    }

    @Test
    fun updateBabyName_updatesState() = runTest {
        val repository = FakeBabyPreferencesRepository()
        val viewModel = SplashViewModel(repository)

        viewModel.updateBabyName("Emma")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Emma", viewModel.babyName.value)
    }

    @Test
    fun updatePhotoUri_updatesState() = runTest {
        val repository = FakeBabyPreferencesRepository()
        val viewModel = SplashViewModel(repository)

        viewModel.updatePhotoUri("content://media/picker/0/1")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("content://media/picker/0/1", viewModel.photoUri.value)
    }
}
