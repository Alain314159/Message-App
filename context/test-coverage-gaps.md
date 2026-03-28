# Test Coverage Gap Analysis - Message App

**Date:** 2026-03-26  
**Current Coverage:** 72%  
**Target Coverage:** 80%+  
**Test Framework:** JUnit 4, MockK, Turbine, Truth

---

## 📊 Current Test Inventory

### Existing Test Files (19 files)

| File | Lines | Tests | Coverage Estimate | Status |
|------|-------|-------|-------------------|--------|
| `AuthRepositoryTest.kt` | 280 | 24 | ~65% | ⚠️ Needs improvement |
| `ChatRepositoryTest.kt` | 140 | 11 | ~40% | ❌ Critical gaps |
| `ChatRepositoryAdditionalTest.kt` | 320 | 29 | ~70% | ✅ Good |
| `PresenceRepositoryTest.kt` | 340 | 26 | ~60% | ⚠️ Needs improvement |
| `ProfileRepositoryTest.kt` | - | - | ~50% | ⚠️ Needs improvement |
| `AvatarRepositoryTest.kt` | - | - | ~45% | ⚠️ Needs improvement |
| `ChatViewModelTest.kt` | 180 | 8 | ~55% | ❌ Critical gaps |
| `ChatViewModelAdditionalTest.kt` | 280 | 18 | ~70% | ✅ Good |
| `AuthViewModelTest.kt` | 340 | 22 | ~75% | ✅ Good |
| `ChatListViewModelTest.kt` | - | - | ~50% | ⚠️ Needs improvement |
| `E2ECipherTest.kt` | - | - | ~60% | ⚠️ Needs improvement |
| `MessageDaoTest.kt` | - | - | ~70% | ✅ Good |
| `ModelsTest.kt` | - | - | ~80% | ✅ Good |
| `ThemeModelsTest.kt` | - | - | ~85% | ✅ Good |
| `ContactsUtilsTest.kt` | - | - | ~65% | ⚠️ Needs improvement |
| `SignatureLoggerTest.kt` | - | - | ~60% | ⚠️ Needs improvement |
| `TimeUtilsTest.kt` | - | - | ~70% | ✅ Good |

---

## 🔴 Critical Test Gaps (Priority 1)

### 1. ChatRepository - Realtime Subscriptions

**Missing Tests:**
- `observeChats emits initial list then updates on realtime changes`
- `observeChats handles network errors gracefully`
- `observeChats filters chats by user membership`
- `observeMessages emits messages in chronological order`
- `observeMessages auto-marks messages as delivered for non-sender`
- `observeMessages handles realtime disconnect/reconnect`
- `observeChat returns null for non-existent chat`

**Risk Level:** HIGH - Core functionality untested

**Estimated Lines:** 250

---

### 2. ChatRepository - Message Operations

**Missing Tests:**
- `deleteMessageForUser adds user to deleted_for list`
- `deleteMessageForUser handles already deleted message`
- `deleteMessageForAll changes message type to deleted`
- `deleteMessageForAll updates chat last_message_enc`
- `pinMessage updates chat pinned_message_id and pinned_snippet`
- `unpinMessage sets pinned fields to null`
- `markAsRead updates read_at for all unread messages`
- `countUnreadMessages returns correct count`
- `countUnreadMessages excludes messages sent by user`

**Risk Level:** HIGH - Data integrity at risk

**Estimated Lines:** 200

---

### 3. ChatViewModel - sendText Method

**Missing Tests:**
- `sendText encrypts message before sending`
- `sendText updates error state when encryption fails`
- `sendText handles cipher initialization error`
- `sendText with valid message calls repository`
- `sendText shows error when repository throws exception`

**Risk Level:** HIGH - E2E encryption critical path

**Estimated Lines:** 150

---

### 4. ChatViewModel - deleteMessage Methods

**Missing Tests:**
- `deleteMessageForUser calls repository with correct parameters`
- `deleteMessageForAll calls repository with correct parameters`
- `deleteMessageForUser updates error state on failure`
- `deleteMessageForAll updates error state on failure`

**Risk Level:** MEDIUM - User-facing feature

**Estimated Lines:** 100

---

### 5. ChatViewModel - Typing Indicators

**Missing Tests:**
- `setTyping calls presence repository`
- `observePartnerTyping updates isPartnerTyping state`
- `observePartnerTyping debounces rapid changes`
- `typing indicator auto-clears after 5 seconds`

**Risk Level:** MEDIUM - UX feature

**Estimated Lines:** 120

---

### 6. PresenceRepository - Typing Timeout

**Missing Tests:**
- `setTyping auto-clears after 5 seconds when isTyping true`
- `setTyping does not auto-clear when isTyping false`
- `setTyping handles concurrent calls to same chat`
- `setTyping identifies correct user (user1 vs user2)`

**Risk Level:** HIGH - Timing-dependent behavior

**Estimated Lines:** 150

---

### 7. PresenceRepository - Online Status

**Missing Tests:**
- `observePartnerOnline emits initial state`
- `observePartnerOnline emits on status change`
- `observePartnerOnline handles user not found`
- `getPartnerLastSeen returns null for non-existent user`
- `getPartnerLastSeen returns timestamp for existing user`

**Risk Level:** MEDIUM - UX feature

**Estimated Lines:** 120

---

### 8. AuthRepository - Network Errors

**Missing Tests:**
- `signUpWithEmail handles network timeout`
- `signUpWithEmail handles server error 500`
- `signUpWithEmail handles duplicate email`
- `signInWithEmail handles invalid credentials`
- `signInWithEmail handles account not confirmed`
- `signInWithGoogle handles credential manager failure`
- `signInWithGoogle handles network error`
- `signOut handles network error gracefully`

**Risk Level:** HIGH - Authentication critical path

**Estimated Lines:** 200

---

## 🟡 Medium Priority Gaps (Priority 2)

### 9. AuthRepository - Edge Cases

**Missing Tests:**
- `signUpWithEmail with unicode in email`
- `signUpWithEmail with very long password`
- `signInWithEmail with locked account`
- `getCurrentUser returns null when offline`
- `getCurrentUser returns user when cached`

**Risk Level:** MEDIUM

**Estimated Lines:** 100

---

### 10. ChatRepository - Edge Cases

**Missing Tests:**
- `ensureDirectChat creates chat if not exists`
- `ensureDirectChat updates timestamp if exists`
- `observeChats handles empty chat list`
- `observeChats handles chat with null last_message`

**Risk Level:** MEDIUM

**Estimated Lines:** 100

---

### 11. ViewModel - Lifecycle

**Missing Tests:**
- `ChatViewModel onCleared sets offline status`
- `ChatViewModel onCleared clears typing indicator`
- `ChatViewModel stop clears current chat`
- `AuthViewModel init loads persisted session`

**Risk Level:** MEDIUM

**Estimated Lines:** 80

---

## 🟢 Low Priority Gaps (Priority 3)

### 12. Utils - Additional Coverage

**Missing Tests:**
- `TimeUtils.formatRelativeTime for future dates`
- `TimeUtils.formatRelativeTime for very old dates`
- `SignatureLogger logs signature correctly`
- `SignatureLogger handles empty message`

**Risk Level:** LOW

**Estimated Lines:** 60

---

### 13. Model - Edge Cases

**Missing Tests:**
- `Message with null optional fields`
- `Chat with null last_message`
- `User with null avatar`
- `Message.deletedFor contains multiple users`

**Risk Level:** LOW

**Estimated Lines:** 50

---

## 📈 Coverage Improvement Plan

### Phase 1: Critical Gaps (Week 1)

| File | Tests to Add | Lines | Priority |
|------|--------------|-------|----------|
| `ChatRepositoryRealtimeTest.kt` | 10 | 250 | HIGH |
| `ChatRepositoryMessageOpsTest.kt` | 12 | 200 | HIGH |
| `ChatViewModelSendTextTest.kt` | 8 | 150 | HIGH |
| `ChatViewModelDeleteTest.kt` | 6 | 100 | HIGH |
| `PresenceRepositoryTypingTest.kt` | 8 | 150 | HIGH |
| `PresenceRepositoryOnlineTest.kt` | 7 | 120 | MEDIUM |
| `AuthRepositoryNetworkTest.kt` | 10 | 200 | HIGH |

**Total Phase 1:** 61 tests, 1170 lines  
**Expected Coverage:** 72% → 78%

---

### Phase 2: Medium Priority (Week 2)

| File | Tests to Add | Lines | Priority |
|------|--------------|-------|----------|
| `AuthRepositoryEdgeCasesTest.kt` | 8 | 100 | MEDIUM |
| `ChatRepositoryEdgeCasesTest.kt` | 6 | 100 | MEDIUM |
| `ViewModelLifecycleTest.kt` | 6 | 80 | MEDIUM |

**Total Phase 2:** 20 tests, 280 lines  
**Expected Coverage:** 78% → 80%

---

### Phase 3: Low Priority (Week 3)

| File | Tests to Add | Lines | Priority |
|------|--------------|-------|----------|
| `UtilsAdditionalTest.kt` | 6 | 60 | LOW |
| `ModelsEdgeCasesTest.kt` | 8 | 50 | LOW |

**Total Phase 3:** 14 tests, 110 lines  
**Expected Coverage:** 80% → 82%

---

## 🎯 Test Generation Strategy

### TDD Approach (Strict)

For each test file:

1. **RED**: Write failing test first
   - Test should fail because implementation doesn't exist OR
   - Test should fail because edge case isn't handled

2. **GREEN**: Implement minimum code to pass
   - No extra features
   - No optimization

3. **REFACTOR**: Clean up while keeping tests green
   - Improve readability
   - Remove duplication
   - Extract methods

### Test Structure (AAA Pattern)

```kotlin
@Test
fun `should_do_X_when_Y`() = runTest {
    // Arrange
    val expected = ...
    val mock = mockk()
    every { mock.function() } returns expected
    
    // Act
    val result = subject.function()
    
    // Assert
    assertEquals(expected, result)
}
```

### Mocking Strategy

```kotlin
// Mock external dependencies
private val chatRepository: ChatRepository = mockk()
private val presenceRepository: PresenceRepository = mockk()
private val authRepository: AuthRepository = mockk()

// Mock flows for realtime
val messagesFlow = MutableStateFlow(listOf<Message>())
coEvery { chatRepository.observeMessages(any(), any()) } returns messagesFlow

// Verify interactions
coVerify { chatRepository.sendText(any(), any(), any(), any()) }
```

---

## 📋 Test Files to Generate

### Priority 1 (Critical)

1. `ChatRepositoryRealtimeTest.kt` - Realtime subscriptions
2. `ChatRepositoryMessageOperationsTest.kt` - Message CRUD operations
3. `ChatViewModelSendTextTest.kt` - sendText with encryption
4. `ChatViewModelDeleteMessageTest.kt` - deleteMessage methods
5. `ChatViewModelTypingTest.kt` - Typing indicators
6. `PresenceRepositoryTypingTimeoutTest.kt` - Auto-clear typing
7. `PresenceRepositoryOnlineStatusTest.kt` - Online/offline status
8. `AuthRepositoryNetworkErrorTest.kt` - Network error handling

### Priority 2 (Medium)

9. `AuthRepositoryEdgeCasesTest.kt` - Edge cases for auth
10. `ChatRepositoryEdgeCasesTest.kt` - Edge cases for chat
11. `ViewModelLifecycleTest.kt` - ViewModel lifecycle methods

### Priority 3 (Low)

12. `UtilsAdditionalTest.kt` - Additional utils coverage
13. `ModelsEdgeCasesTest.kt` - Model edge cases

---

## ✅ Success Criteria

### Coverage Metrics

| Metric | Current | Target |
|--------|---------|--------|
| Line Coverage | 72% | 80%+ |
| Branch Coverage | 65% | 75%+ |
| Test Count | ~200 | 300+ |
| Test Pass Rate | 95% | 100% |

### Quality Metrics

- [ ] All critical paths covered (auth, chat, encryption)
- [ ] All edge cases documented and tested
- [ ] All error scenarios covered
- [ ] Tests run in < 2 minutes
- [ ] No flaky tests
- [ ] Tests are independent
- [ ] Tests have descriptive names

---

## 🚀 Next Steps

1. **Generate Priority 1 tests** (8 files, 61 tests)
2. **Run all tests** after each file
3. **Fix any failing tests** (implementation bugs)
4. **Measure coverage** after Phase 1
5. **Continue to Phase 2** if coverage < 80%

---

**Last Updated:** 2026-03-26  
**Next Review:** After Phase 1 completion
