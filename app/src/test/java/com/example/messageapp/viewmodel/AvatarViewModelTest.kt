package com.example.messageapp.viewmodel

import com.example.messageapp.data.AvatarRepository
import com.example.messageapp.model.AvatarType
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
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
 * Tests for AvatarViewModel state management.
 * Uses mocked AvatarRepository to isolate ViewModel logic.
 */
@RunWith(RobolectricTestRunner::class)
@Config(packageName = "com.example.messageapp", sdk = [33])
class AvatarViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockRepo = mockk<AvatarRepository>(relaxed = true)
    private lateinit var viewModel: AvatarViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { mockRepo.getAllAvatars() } returns AvatarType.getAll()
        coEvery { mockRepo.getUserAvatar(any()) } returns AvatarType.CERDITA
        coEvery { mockRepo.setUserAvatar(any(), any()) } returns Result.success(Unit)
        viewModel = AvatarViewModel(mockRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has default avatar`() {
        assertThat(viewModel.selectedAvatar.value).isEqualTo(AvatarType.CERDITA)
        assertThat(viewModel.uiState.value.isLoading).isFalse()
        assertThat(viewModel.uiState.value.isSaving).isFalse()
        assertThat(viewModel.uiState.value.saveSuccess).isNull()
        assertThat(viewModel.uiState.value.errorMessage).isNull()
    }

    @Test
    fun `selectAvatar updates selectedAvatar state`() {
        viewModel.selectAvatar(AvatarType.KOALA)

        assertThat(viewModel.selectedAvatar.value).isEqualTo(AvatarType.KOALA)
    }

    @Test
    fun `selectAvatar can switch back to CERDITA`() {
        viewModel.selectAvatar(AvatarType.KOALA)
        viewModel.selectAvatar(AvatarType.CERDITA)

        assertThat(viewModel.selectedAvatar.value).isEqualTo(AvatarType.CERDITA)
    }

    @Test
    fun `resetState clears saveSuccess and errorMessage`() {
        viewModel.selectAvatar(AvatarType.KOALA)

        viewModel.resetState()

        assertThat(viewModel.uiState.value.saveSuccess).isNull()
        assertThat(viewModel.uiState.value.errorMessage).isNull()
    }

    @Test
    fun `getAllAvatars returns all avatar types`() {
        val avatars = viewModel.getAllAvatars()

        assertThat(avatars).hasSize(2)
        assertThat(avatars).containsExactly(AvatarType.CERDITA, AvatarType.KOALA)
    }

    @Test
    fun `loadUserAvatar updates state with repo result`() = runTest {
        viewModel.loadUserAvatar("user-123")
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.selectedAvatar.value).isEqualTo(AvatarType.CERDITA)
        assertThat(viewModel.uiState.value.isLoading).isFalse()
    }

    @Test
    fun `saveAvatar calls repo and updates state`() = runTest {
        viewModel.selectAvatar(AvatarType.KOALA)
        viewModel.saveAvatar("user-123")
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.isSaving).isFalse()
        assertThat(viewModel.uiState.value.saveSuccess).isTrue()
    }
}
