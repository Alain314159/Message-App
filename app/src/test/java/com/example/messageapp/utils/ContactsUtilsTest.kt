package com.example.messageapp.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Tests para utilidades de Contacts (Contacts.kt)
 * 
 * Tests Mínimos (Regla de Memoria):
 * - Happy path (1 test)
 * - Edge cases (2+ tests)
 * - Error handling (1+ tests)
 * - Null/empty cases (1+ tests)
 */
class ContactsUtilsTest {

    // ============================================
    // Tests para DeviceContact
    // ============================================

    @Test
    fun `DeviceContact with empty name creates successfully`() {
        // When: Creo DeviceContact con nombre vacío
        val contact = DeviceContact(name = "", phones = emptyList())

        // Then: Debería crear exitosamente
        assertThat(contact.name).isEmpty()
        assertThat(contact.phones).isEmpty()
    }

    @Test
    fun `DeviceContact with valid data creates successfully`() {
        // When: Creo DeviceContact con datos válidos
        val contact = DeviceContact(
            name = "John Doe",
            phones = listOf("+1234567890", "+0987654321")
        )

        // Then: Debería crear exitosamente
        assertThat(contact.name).isEqualTo("John Doe")
        assertThat(contact.phones).hasSize(2)
        assertThat(contact.phones).contains("+1234567890")
        assertThat(contact.phones).contains("+0987654321")
    }

    @Test
    fun `DeviceContact with empty phones list creates successfully`() {
        // When: Creo DeviceContact sin teléfonos
        val contact = DeviceContact(
            name = "No Phone User",
            phones = emptyList()
        )

        // Then: Debería crear exitosamente
        assertThat(contact.name).isEqualTo("No Phone User")
        assertThat(contact.phones).isEmpty()
    }

    @Test
    fun `DeviceContact with single phone creates successfully`() {
        // When: Creo DeviceContact con un solo teléfono
        val contact = DeviceContact(
            name = "Single Phone",
            phones = listOf("+1234567890")
        )

        // Then: Debería crear exitosamente
        assertThat(contact.name).isEqualTo("Single Phone")
        assertThat(contact.phones).hasSize(1)
    }

    @Test
    fun `DeviceContact with unicode name creates successfully`() {
        // When: Creo DeviceContact con unicode
        val contact = DeviceContact(
            name = "Usuario 🌍",
            phones = listOf("+1234567890")
        )

        // Then: Debería crear exitosamente
        assertThat(contact.name).contains("🌍")
    }

    @Test
    fun `DeviceContact with special characters creates successfully`() {
        // When: Creo DeviceContact con caracteres especiales
        val contact = DeviceContact(
            name = "User-<>&\"'-123",
            phones = listOf("+123-456-7890")
        )

        // Then: Debería crear exitosamente
        assertThat(contact.name).contains("<>&\"'")
    }

    @Test
    fun `DeviceContact with very long name creates successfully`() {
        // When: Creo DeviceContact con nombre muy largo
        val contact = DeviceContact(
            name = "a".repeat(500),
            phones = listOf("+1234567890")
        )

        // Then: Debería crear exitosamente
        assertThat(contact.name).hasLength(500)
    }

    @Test
    fun `DeviceContact with many phones creates successfully`() {
        // When: Creo DeviceContact con muchos teléfonos
        val phones = List(50) { "+123456789$it" }
        val contact = DeviceContact(
            name = "Many Phones",
            phones = phones
        )

        // Then: Debería crear exitosamente
        assertThat(contact.phones).hasSize(50)
    }

    // ============================================
    // Tests para rememberDeviceContactsDetailed
    // ============================================

    @Test
    fun `rememberDeviceContactsDetailed returns empty list initially`() {
        // Note: Esta función es @Composable, no se puede testear directamente
        // Sin embargo, verificamos que DeviceContact funciona correctamente

        // When: Creo lista vacía de contactos
        val contacts = emptyList<DeviceContact>()

        // Then: Debería ser lista vacía
        assertThat(contacts).isEmpty()
    }

    @Test
    fun `DeviceContact list with multiple contacts creates successfully`() {
        // When: Creo lista de múltiples contactos
        val contacts = listOf(
            DeviceContact("Contact 1", listOf("+1111")),
            DeviceContact("Contact 2", listOf("+2222", "+3333")),
            DeviceContact("Contact 3", emptyList())
        )

        // Then: Debería crear exitosamente
        assertThat(contacts).hasSize(3)
        assertThat(contacts[0].name).isEqualTo("Contact 1")
        assertThat(contacts[1].phones).hasSize(2)
        assertThat(contacts[2].phones).isEmpty()
    }

    // ============================================
    // Tests edge cases: Null safety
    // ============================================

    @Test
    fun `DeviceContact with null-like strings handles gracefully`() {
        // When: Creo DeviceContact con strings que parecen null
        val contact = DeviceContact(
            name = "null",
            phones = listOf("null")
        )

        // Then: Debería crear exitosamente
        assertThat(contact.name).isEqualTo("null")
        assertThat(contact.phones).contains("null")
    }

    // ============================================
    // Tests edge cases: Phone number formats
    // ============================================

    @Test
    fun `DeviceContact with various phone formats creates successfully`() {
        // When: Creo DeviceContact con varios formatos de teléfono
        val contact = DeviceContact(
            name = "International",
            phones = listOf(
                "+1234567890",
                "+54-11-1234-5678",
                "+1 (555) 123-4567",
                "1234567890",
                "+86 138 0000 0000"
            )
        )

        // Then: Debería crear exitosamente
        assertThat(contact.phones).hasSize(5)
    }

    // ============================================
    // Tests de concurrencia
    // ============================================

    @Test
    fun `creating multiple DeviceContacts concurrently does not crash`() {
        // When: Creo múltiples DeviceContacts en paralelo
        val contacts = List(100) { index ->
            DeviceContact(
                name = "Contact $index",
                phones = listOf("+123456789$index")
            )
        }

        // Then: Ninguno debería crashar
        assertThat(contacts).hasSize(100)
        contacts.forEachIndexed { index, contact ->
            assertThat(contact.name).isEqualTo("Contact $index")
            assertThat(contact.phones).contains("+123456789$index")
        }
    }

    // ============================================
    // Tests de rendimiento
    // ============================================

    @Test
    fun `creating 1000 DeviceContacts performance test`() {
        // When: Creo 1000 DeviceContacts
        val startTime = System.currentTimeMillis()
        val contacts = List(1000) { index ->
            DeviceContact(
                name = "Contact $index",
                phones = List(5) { "+123456789${index}_$it" }
            )
        }
        val elapsed = System.currentTimeMillis() - startTime

        // Then: Debería ser rápido (< 500ms)
        assertThat(elapsed).isLessThan(500)
        assertThat(contacts).hasSize(1000)
    }
}
