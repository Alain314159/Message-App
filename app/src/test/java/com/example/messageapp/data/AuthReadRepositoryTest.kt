package com.example.messageapp.data

import android.util.Patterns
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Tests for AuthReadRepository validation logic.
 * Tests isValidEmail directly using Android Patterns (no Supabase dependency).
 */
@RunWith(RobolectricTestRunner::class)
@Config(packageName = "com.example.messageapp", sdk = [33])
class AuthReadRepositoryTest {

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    @Test
    fun `isValidEmail returns true for valid emails`() {
        assertThat(isValidEmail("test@example.com")).isTrue()
        assertThat(isValidEmail("user.name@domain.org")).isTrue()
        assertThat(isValidEmail("a+b@gmail.com")).isTrue()
        assertThat(isValidEmail("user@sub.domain.com")).isTrue()
    }

    @Test
    fun `isValidEmail returns false for invalid emails`() {
        assertThat(isValidEmail("")).isFalse()
        assertThat(isValidEmail("not-an-email")).isFalse()
        assertThat(isValidEmail("@domain.com")).isFalse()
        assertThat(isValidEmail("user@")).isFalse()
        assertThat(isValidEmail("user@.com")).isFalse()
        assertThat(isValidEmail("user@domain")).isFalse()
    }

    @Test
    fun `isValidEmail returns false for emails with spaces`() {
        assertThat(isValidEmail("user @example.com")).isFalse()
        assertThat(isValidEmail(" user@example.com ")).isFalse()
    }
}
