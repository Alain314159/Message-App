package com.example.messageapp.viewmodel

import com.example.messageapp.data.AuthRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Tests for AuthViewModel state management.
 * Uses mocked AuthRepository to isolate ViewModel logic.
 */
@RunWith(RobolectricTestRunner::class)
@Config(packageName = "com.example.messageapp", sdk = [33])
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockRepo = mockk<AuthRepository>(relaxed = true)
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { mockRepo.isUserLoggedIn() } returns false
        every { mockRepo.getCurrentUserId() } returns null
        viewModel = AuthViewModel(mockRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is logged out with no error`() {
        assertThat(viewModel.isLogged.value).isFalse()
        assertThat(viewModel.currentUserId.value).isNull()
        assertThat(viewModel.error.value).isNull()
        assertThat(viewModel.isLoading.value).isFalse()
    }

    @Test
    fun `init refreshes login state and clears errors`() {
        every { mockRepo.isUserLoggedIn() } returns true
        every { mockRepo.getCurrentUserId() } returns "user-123"

        viewModel.init()

        assertThat(viewModel.isLogged.value).isTrue()
        assertThat(viewModel.currentUserId.value).isEqualTo("user-123")
        assertThat(viewModel.error.value).isNull()
        assertThat(viewModel.isLoading.value).isFalse()
    }

    @Test
    fun `clearError clears error state`() {
        viewModel.clearError()

        assertThat(viewModel.error.value).isNull()
    }

    @Test
    fun `signInAnonymously success updates state`() = runTest {
        coEvery { mockRepo.signInAnonymously() } returns Result.success("anon-user-1")

        viewModel.signInAnonymously()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.isLogged.value).isTrue()
        assertThat(viewModel.currentUserId.value).isEqualTo("anon-user-1")
        assertThat(viewModel.isLoading.value).isFalse()
        assertThat(viewModel.error.value).isNull()
    }

    @Test
    fun `signInAnonymously failure sets error state`() = runTest {
        coEvery { mockRepo.signInAnonymously() } returns Result.failure(RuntimeException("Network error"))

        viewModel.signInAnonymously()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.isLogged.value).isFalse()
        assertThat(viewModel.isLoading.value).isFalse()
        assertThat(viewModel.error.value).contains("Network error")
    }

    @Test
    fun `signOut success logs out and clears state`() = runTest {
        coEvery { mockRepo.signOut() } returns Result.success(Unit)

        viewModel.signOut()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.isLogged.value).isFalse()
        assertThat(viewModel.currentUserId.value).isNull()
        assertThat(viewModel.error.value).isNull()
    }

    @Test
    fun `signOut failure sets error state`() = runTest {
        coEvery { mockRepo.signOut() } returns Result.failure(RuntimeException("Connection error"))

        viewModel.signOut()
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.isLogged.value).isFalse()
        assertThat(viewModel.error.value).contains("Connection error")
    }

    @Test
    fun `signInWithEmail success updates state and calls callback`() = runTest {
        var callbackCalled = false
        coEvery { mockRepo.signInWithEmail("a@b.com", "password123") } returns Result.success("user-456")

        viewModel.signInWithEmail("a@b.com", "password123") { callbackCalled = true }
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.isLogged.value).isTrue()
        assertThat(viewModel.currentUserId.value).isEqualTo("user-456")
        assertThat(callbackCalled).isTrue()
    }

    @Test
    fun `signUpWithEmail success updates state`() = runTest {
        coEvery { mockRepo.signUpWithEmail("new@test.com", "password123") } returns Result.success("new-user")

        viewModel.signUpWithEmail("new@test.com", "password123") {}
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.isLogged.value).isTrue()
        assertThat(viewModel.currentUserId.value).isEqualTo("new-user")
    }

    @Test
    fun `signUpWithEmail failure sets error state`() = runTest {
        coEvery { mockRepo.signUpWithEmail(any(), any()) } returns Result.failure(IllegalArgumentException("Email inválido"))

        viewModel.signUpWithEmail("bad", "pass") {}
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.isLogged.value).isFalse()
        assertThat(viewModel.error.value).contains("Email inválido")
    }

    @Test
    fun `updatePresence calls repo`() = runTest {
        viewModel.updatePresence(true)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockRepo.updatePresence(true) }
    }
}
