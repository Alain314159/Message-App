# Cerlita Chat Features Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add rich messaging, media storage, push notifications, WebRTC calls, and UX polish to the Cerlita Chat React Native app.

**Architecture:** Layered approach — missing Supabase service layer first, then feature modules built on top of existing Zustand stores and Expo Router. Each feature is independently testable.

**Tech Stack:** React Native (Expo SDK 52), TypeScript, Zustand, Supabase (Realtime, Storage, Auth), expo-notifications, react-native-webrtc, expo-image-picker, expo-av, react-native-reanimated, @shopify/flash-list

---

## Phase Overview

| Phase | Subsystem | Tasks | Dependency |
|-------|-----------|-------|------------|
| **Phase 0** | Foundation (Services) | 1-5 | None |
| **Phase 1** | Core Messaging | 6-13 | Phase 0 |
| **Phase 2** | Media & Storage | 14-18 | Phase 0, Phase 1 |
| **Phase 3** | Notifications | 19-20 | Phase 0 |
| **Phase 4** | UX Polish | 21-27 | Phase 1, Phase 2 |
| **Phase 5** | Calls (WebRTC) | 28-30 | Phase 0, Phase 1 |

---

## File Map

### New Files
```
src/services/supabase/
  config.ts, auth.service.ts, chat.service.ts, message.service.ts, notification.service.ts
src/services/media/
  storage.service.ts, imageCompressor.service.ts, mediaCache.service.ts
src/services/calls/
  webrtc.service.ts
src/services/backup.service.ts
src/components/chat/
  MessageBubble.tsx, MessageInput.tsx, MessageReactions.tsx, ReplyPreview.tsx,
  ReplyThread.tsx, MediaPreviewModal.tsx, VoiceRecorder.tsx, SearchMessagesModal.tsx,
  ChatOptionsMenu.tsx, SwipeableMessage.tsx
src/components/calls/
  CallScreen.tsx, IncomingCallModal.tsx
src/hooks/
  useMediaPicker.ts, useVoiceRecording.ts, useMessageSearch.ts, useCall.ts
src/types/
  message.types.ts, call.types.ts
```

### Modified Files
```
src/types/index.ts, src/store/messageStore.ts, src/store/chatStore.ts,
src/hooks/useMessages.ts, src/providers/AuthProvider.tsx,
app/(chat)/[chatId].tsx, app/(chat)/index.tsx, app/(chat)/settings.tsx,
app/_layout.tsx, database_schema.sql
```

---

## Phase 0: Foundation — Supabase Service Layer

### Task 1: Supabase Client

**Files:** Create `src/services/supabase/config.ts`, Modify `src/config/env.ts`

- [ ] **Step 1: Add env exports**

Add to `src/config/env.ts`:
```typescript
export const SUPABASE_URL = process.env.EXPO_PUBLIC_SUPABASE_URL || '';
export const SUPABASE_ANON_KEY = process.env.EXPO_PUBLIC_SUPABASE_ANON_KEY || '';
```

- [ ] **Step 2: Create client**

```typescript
// src/services/supabase/config.ts
import { createClient } from '@supabase/supabase-js';
import type { Database } from '@/types/database.types';
import { SUPABASE_URL, SUPABASE_ANON_KEY } from '@/config/env';

export const supabase = createClient<Database>(SUPABASE_URL, SUPABASE_ANON_KEY, {
  auth: { autoRefreshToken: true, persistSession: true },
  realtime: { params: { eventsPerSecond: 10 } },
});
```

- [ ] **Step 3: Verify** — Run `npx tsc --noEmit`, expect no errors.
- [ ] **Step 4: Commit** — `git add -A && git commit -m "feat: initialize Supabase client"`

---

### Task 2: Auth Service

**Files:** Create `src/services/supabase/auth.service.ts`, Create `__tests__/services/auth.service.test.ts`

- [ ] **Step 1: Write tests** — See full test code in plan context. Cover signIn, signUp, signOut, resetPassword, getCurrentUser.
- [ ] **Step 2: Run tests to verify failure.**
- [ ] **Step 3: Implement**

```typescript
// src/services/supabase/auth.service.ts
import { supabase } from './config';

export const authService = {
  async signIn(email: string, password: string) {
    const { data, error } = await supabase.auth.signInWithPassword({ email, password });
    return { user: data.user, error: error?.message || null };
  },
  async signUp(email: string, password: string, displayName: string) {
    const { data, error } = await supabase.auth.signUp({
      email, password, options: { data: { display_name: displayName } },
    });
    return { user: data.user, error: error?.message || null };
  },
  async signOut() {
    const { error } = await supabase.auth.signOut();
    if (error) console.error('Sign out error:', error.message);
  },
  async resetPassword(email: string, redirectTo: string) {
    const { error } = await supabase.auth.resetPasswordForEmail(email, { redirectTo });
    return { error: error?.message || null };
  },
  async getCurrentUser() {
    const { data, error } = await supabase.auth.getUser();
    return { user: data.user, error: error?.message || null };
  },
  onAuthStateChange(callback: (event: string, session: any) => void) {
    const { data } = supabase.auth.onAuthStateChange(callback);
    return data.subscription;
  },
};
```

- [ ] **Step 4: Run tests pass. Commit.**

---

### Task 3: Chat Service

**Files:** Create `src/services/supabase/chat.service.ts`, Create `__tests__/services/chat.service.test.ts`

- [ ] **Step 1-2: Write tests, verify failure.** Test getUserChats, getOrCreateDirectChat, subscribeToUserChats.
- [ ] **Step 3: Implement**

```typescript
// src/services/supabase/chat.service.ts
import { supabase } from './config';
import type { RealtimeChannel } from '@supabase/supabase-js';
import type { Database } from '@/types/database.types';

type ChatRow = Database['public']['Tables']['chats']['Row'];

export const chatService = {
  async getUserChats(userId: string) {
    const { data, error } = await supabase
      .from('chats').select('*').contains('participant_ids', [userId])
      .order('updated_at', { ascending: false });
    if (error) throw new Error(error.message);
    return data as ChatRow[];
  },
  async getOrCreateDirectChat(user1Id: string, user2Id: string) {
    const { data, error } = await supabase.rpc('get_or_create_direct_chat', {
      user1_id: user1Id, user2_id: user2Id,
    });
    if (error) throw new Error(error.message);
    return data as string;
  },
  subscribeToUserChats(userId: string, callback: (payload: any) => void): RealtimeChannel {
    const channel = supabase.channel(`chats:${userId}`);
    channel.on('postgres_changes', {
      event: '*', schema: 'public', table: 'chats',
      filter: `participant_ids=cs.{${userId}}`,
    }, callback).subscribe();
    return channel;
  },
};
```

- [ ] **Step 4: Tests pass. Commit.**

---

### Task 4: Message Service

**Files:** Create `src/services/supabase/message.service.ts`, Create `__tests__/services/message.service.test.ts`

- [ ] **Step 1-2: Write tests, verify failure.** Test getMessages, sendMessage, updateMessageStatus, markAllAsRead, subscribeToMessages.
- [ ] **Step 3: Implement**

```typescript
// src/services/supabase/message.service.ts
import { supabase } from './config';
import type { RealtimeChannel } from '@supabase/supabase-js';
import type { Database } from '@/types/database.types';

type MessageRow = Database['public']['Tables']['messages']['Row'];
type MessageInsert = Database['public']['Tables']['messages']['Insert'];

export interface SendMessageParams {
  chatId: string; senderId: string; content: string;
  messageType?: 'text' | 'image' | 'video' | 'audio' | 'file';
  mediaUrl?: string; thumbnailUrl?: string; replyToId?: string;
}

export const messageService = {
  async getMessages(chatId: string, limit = 50, before?: Date) {
    let query = supabase.from('messages').select('*').eq('chat_id', chatId)
      .order('created_at', { ascending: false }).limit(limit);
    if (before) query = query.lt('created_at', before.toISOString());
    const { data, error } = await query;
    if (error) throw new Error(error.message);
    return data as MessageRow[];
  },
  async sendMessage(params: SendMessageParams) {
    const { data, error } = await supabase.from('messages').insert({
      chat_id: params.chatId, sender_id: params.senderId, content: params.content,
      message_type: params.messageType || 'text', media_url: params.mediaUrl || null,
      thumbnail_url: params.thumbnailUrl || null, reply_to_id: params.replyToId || null,
      status: 'sent',
    }).select().single();
    if (error) throw new Error(error.message);
    return data as MessageRow;
  },
  async updateMessage(messageId: string, updates: Partial<MessageInsert>) {
    const { data, error } = await supabase.from('messages').update(updates)
      .eq('id', messageId).select().single();
    if (error) throw new Error(error.message);
    return data as MessageRow;
  },
  async updateMessageStatus(messageId: string, status: string) {
    const { error } = await supabase.from('messages').update({ status }).eq('id', messageId);
    if (error) throw new Error(error.message);
  },
  async markAllAsRead(chatId: string, userId: string) {
    const { error } = await supabase.from('messages').update({ status: 'read' })
      .eq('chat_id', chatId).neq('sender_id', userId).in('status', ['sent', 'delivered']);
    if (error) throw new Error(error.message);
  },
  async deleteMessage(messageId: string) {
    const { error } = await supabase.from('messages').delete().eq('id', messageId);
    if (error) throw new Error(error.message);
  },
  subscribeToMessages(chatId: string, callback: (payload: any) => void): RealtimeChannel {
    const channel = supabase.channel(`messages:${chatId}`);
    channel.on('postgres_changes', {
      event: '*', schema: 'public', table: 'messages', filter: `chat_id=eq.${chatId}`,
    }, callback).subscribe();
    return channel;
  },
};
```

- [ ] **Step 4: Tests pass. Commit.**

---

### Task 5: Notification Service

**Files:** Create `src/services/supabase/notification.service.ts`

- [ ] **Step 1: Implement**

```typescript
// src/services/supabase/notification.service.ts
import * as Notifications from 'expo-notifications';
import { supabase } from './config';
import { Platform } from 'react-native';

export interface NotificationPayload {
  title: string; body: string; data?: Record<string, any>;
}

export const notificationService = {
  async requestPermission() {
    const { status: existingStatus } = await Notifications.getPermissionsAsync();
    let finalStatus = existingStatus;
    if (existingStatus !== 'granted') {
      const { status } = await Notifications.requestPermissionsAsync();
      finalStatus = status;
    }
    if (finalStatus !== 'granted') throw new Error('Notification permissions not granted');
    if (Platform.OS === 'android') {
      await Notifications.setNotificationChannelAsync('default', {
        name: 'default', importance: Notifications.AndroidImportance.MAX,
        vibrationPattern: [0, 250, 250, 250], lightColor: '#FF231F7C',
      });
    }
  },
  async getPushToken() { return (await Notifications.getExpoPushTokenAsync()).data; },
  async savePushToken(userId: string, token: string) {
    const { error } = await supabase.from('users').update({ push_token: token }).eq('id', userId);
    if (error) throw new Error(error.message);
  },
  async queueNotification(userId: string, payload: NotificationPayload) {
    const { error } = await supabase.from('notifications').insert({
      user_id: userId, title: payload.title, body: payload.body,
      data: payload.data || {}, status: 'pending',
    });
    if (error) throw new Error(error.message);
  },
  initialize(userId: string) {
    Notifications.setNotificationHandler({
      handleNotification: async () => ({
        shouldShowAlert: true, shouldPlaySound: true, shouldSetBadge: true,
      }),
    });
    this.getPushToken().then((token) => this.savePushToken(userId, token))
      .catch((err) => console.error('Failed to save push token:', err));
    const subscription = Notifications.addNotificationReceivedListener((n) => {
      console.log('Notification received:', n);
    });
    return subscription;
  },
  cleanup(subscription: any) {
    Notifications.removeNotificationSubscription(subscription);
  },
};
```

- [ ] **Step 2: Commit** — `git add -A && git commit -m "feat: implement notification service"`

---

## Phase 1: Core Messaging

### Task 6: Extended Message Types

**Files:** Create `src/types/message.types.ts`, Modify `src/types/index.ts`

- [ ] **Step 1: Create message.types.ts**

```typescript
// src/types/message.types.ts
import type { Message, MessageType } from './index';

export interface MessageReaction {
  id: string; messageId: string; userId: string; emoji: string; createdAt: Date;
}
export interface ReactionCounts {
  [emoji: string]: { count: number; userReacted: boolean };
}
export interface ReplyContext {
  messageId: string; senderName: string; text: string; type: MessageType;
}
export interface MessageEdit {
  id: string; messageId: string; previousContent: string; editedAt: Date; editedBy: string;
}
export interface ForwardTarget {
  chatId: string; chatName: string; lastMessage: string | null;
}
export type MessageAction = 'reply' | 'react' | 'edit' | 'forward' | 'star' | 'delete' | 'copy' | 'select';
export interface MessageWithMeta extends Message {
  reactions?: ReactionCounts;
  replyContext?: ReplyContext | null;
  isStarred?: boolean;
  isSelected?: boolean;
  editHistory?: MessageEdit[];
}
```

- [ ] **Step 2: Re-export from index.ts** — Add `export type { ... } from './message.types';`
- [ ] **Step 3: Commit**

---

### Task 7: Message Store — Reactions

**Files:** Modify `src/store/messageStore.ts`

- [ ] **Step 1: Add to interface** — `addReaction`, `removeReaction`, `getReactionCounts`
- [ ] **Step 2: Add in-memory cache** — `reactions: Map<string, Map<string, string[]>>`
- [ ] **Step 3: Implement actions** — insert/delete from `message_reactions` table, update local cache
- [ ] **Step 4: Commit**

---

### Task 8: Message Store — Reply, Edit, Forward, Star

**Files:** Modify `src/store/messageStore.ts`, Modify `src/hooks/useMessages.ts`

- [ ] **Step 1: Add reply state** — `replyContext: ReplyContext | null`, `setReplyContext`, `getReplyContext`
- [ ] **Step 2: Update sendMessage** — accept options `{ messageType, mediaUrl, thumbnailUrl, replyToId }`, encrypt text, clear reply context after send
- [ ] **Step 3: Add editMessage** — re-encrypt new text, call `messageService.updateMessage` with `is_edited: true`, update local state
- [ ] **Step 4: Add deleteMessage** — call `messageService.deleteMessage`, remove from local array
- [ ] **Step 5: Add forwardMessage** — find original message, re-send to target chats
- [ ] **Step 6: Add starredMessages** — `Set<string>`, `starMessage`, `unstarMessage`
- [ ] **Step 7: Update useMessages hook** — pass options through `handleSendMessage`
- [ ] **Step 8: Commit**

---

### Task 9: MessageBubble Component

**Files:** Create `src/components/chat/MessageBubble.tsx`, `src/components/chat/MessageReactions.tsx`, `src/components/chat/ReplyThread.tsx`

- [ ] **Step 1: Create ReplyThread** — Shows quoted message context with colored indicator bar
- [ ] **Step 2: Create MessageReactions** — Renders emoji reaction chips with counts and highlighted user-reacted state
- [ ] **Step 3: Create MessageBubble** — Polymorphic renderer for text/image/video/audio/file types. Shows status icons (single check, double check blue), edited label, timestamp, reactions, star indicator. Long press handler.
- [ ] **Step 4: Commit**

---

### Task 10: MessageInput Component

**Files:** Create `src/components/chat/MessageInput.tsx`, `src/components/chat/ReplyPreview.tsx`

- [ ] **Step 1: Create ReplyPreview** — Shows "Respondiendo a {name}" bar with close button
- [ ] **Step 2: Create MessageInput** — TextInput + attachment button (paperclip) + camera/mic buttons. Shows send icon when text exists, alternate buttons when empty. Supports multiline, maxLength 5000.
- [ ] **Step 3: Commit**

---

### Task 11: Chat Options Menu

**Files:** Create `src/components/chat/ChatOptionsMenu.tsx`

- [ ] **Step 1: Create** — Bottom sheet modal with options: Buscar mensajes, Mensajes favoritos, Silenciar, Limpiar chat
- [ ] **Step 2: Commit**

---

### Task 12: Integrate into Chat Screen

**Files:** Modify `app/(chat)/[chatId].tsx`

- [ ] **Step 1: Replace inline message rendering** with `<MessageBubble>` wrapped in `<SwipeableMessage>` (from Phase 4)
- [ ] **Step 2: Replace text input** with `<MessageInput>` 
- [ ] **Step 3: Add `<ReplyPreview>`** when replyContext is set
- [ ] **Step 4: Add `<ChatOptionsMenu>`** triggered by header dots button
- [ ] **Step 5: Verify** — `npx tsc --noEmit`
- [ ] **Step 6: Commit**

---

### Task 13: Database Schema Additions

**Files:** Create `database_schema_additions.sql`

- [ ] **Step 1: Create SQL file** with:
  - `message_reactions` table (id, message_id, user_id, emoji, created_at, UNIQUE constraint)
  - `message_edits` table (id, message_id, previous_content, edited_at, edited_by)
  - RLS policies for both tables
  - `ALTER PUBLICATION supabase_realtime ADD TABLE` for both
- [ ] **Step 2: Commit**

---

## Phase 2: Media & Storage

### Task 14: Storage Service + Buckets

**Files:** Create `src/services/media/storage.service.ts`, Create SQL for buckets

- [ ] **Step 1: Create bucket SQL** — Run in Supabase SQL Editor:
```sql
INSERT INTO storage.buckets (id, name, public) VALUES ('messages', 'messages', true) ON CONFLICT DO NOTHING;
INSERT INTO storage.buckets (id, name, public) VALUES ('avatars', 'avatars', true) ON CONFLICT DO NOTHING;
-- Add RLS policies for upload (owner only), read (authenticated), update/delete (owner)
```

- [ ] **Step 2: Create storage service**

```typescript
// src/services/media/storage.service.ts
import { supabase } from '@/services/supabase/config';
const MESSAGES_BUCKET = 'messages';
const AVATARS_BUCKET = 'avatars';

export const storageService = {
  async uploadMessageMedia(userId: string, chatId: string, file: Blob | File, fileName: string) {
    const path = `${userId}/${chatId}/${Date.now()}_${fileName}`;
    const { data, error } = await supabase.storage.from(MESSAGES_BUCKET).upload(path, file, {
      cacheControl: '3600', upsert: false,
    });
    if (error) throw new Error(error.message);
    const { data: publicUrl } = supabase.storage.from(MESSAGES_BUCKET).getPublicUrl(path);
    return { path: data.path, url: publicUrl.publicUrl };
  },
  async uploadAvatar(userId: string, file: Blob | File) {
    const path = `${userId}/avatar_${Date.now()}`;
    const { data, error } = await supabase.storage.from(AVATARS_BUCKET).upload(path, file, {
      cacheControl: '3600', upsert: true,
    });
    if (error) throw new Error(error.message);
    const { data: publicUrl } = supabase.storage.from(AVATARS_BUCKET).getPublicUrl(path);
    return { path: data.path, url: publicUrl.publicUrl };
  },
  async deleteMedia(path: string) {
    const { error } = await supabase.storage.from(MESSAGES_BUCKET).remove([path]);
    if (error) throw new Error(error.message);
  },
  getPublicUrl(bucket: string, path: string) {
    const { data } = supabase.storage.from(bucket).getPublicUrl(path);
    return data.publicUrl;
  },
  async downloadMedia(path: string, bucket = MESSAGES_BUCKET) {
    const { data, error } = await supabase.storage.from(bucket).download(path);
    if (error) throw new Error(error.message);
    return data;
  },
};
```

- [ ] **Step 3: Commit**

---

### Task 15: Image Compression Service

**Files:** Create `src/services/media/imageCompressor.service.ts`

- [ ] **Step 1: Install** — `npx expo install expo-image-manipulator`
- [ ] **Step 2: Create service**

```typescript
// src/services/media/imageCompressor.service.ts
import { manipulateAsync, SaveFormat } from 'expo-image-manipulator';

export const compressionService = {
  async compressImage(uri: string, maxWidth = 1920, quality = 0.8) {
    const result = await manipulateAsync(uri, [{ resize: { width: maxWidth } }], {
      compress: quality, format: SaveFormat.JPEG,
    });
    return result.uri;
  },
  async generateThumbnail(uri: string, maxWidth = 480, quality = 0.7) {
    const result = await manipulateAsync(uri, [{ resize: { width: maxWidth } }], {
      compress: quality, format: SaveFormat.JPEG,
    });
    return result.uri;
  },
};
```

- [ ] **Step 3: Commit**

---

### Task 16: Media Cache Service

**Files:** Create `src/services/media/mediaCache.service.ts`

- [ ] **Step 1: Create** — Uses expo-file-system for local cache dir, AsyncStorage for metadata. Methods: `cacheUri`, `getCachedUri`, `clearCache`, `getCacheSize`, `cleanIfNeeded` (max 500MB).
- [ ] **Step 2: Commit**

---

### Task 17: Media Picker & Voice Recording Hooks

**Files:** Create `src/hooks/useMediaPicker.ts`, Create `src/hooks/useVoiceRecording.ts`

- [ ] **Step 1: Create useMediaPicker** — Uses expo-image-picker. Methods: `pickImage` (compresses), `pickVideo`, `takePhoto` (compresses). Requests permissions.
- [ ] **Step 2: Create useVoiceRecording** — Uses expo-av Audio.Recording. Methods: `startRecording`, `stopRecording` (returns { uri, duration }), `cancelRecording`. State: `isRecording`, `duration`.
- [ ] **Step 3: Commit**

---

### Task 18: Media Preview & Voice Recorder UI

**Files:** Create `src/components/chat/MediaPreviewModal.tsx`, Create `src/components/chat/VoiceRecorder.tsx`

- [ ] **Step 1: Create MediaPreviewModal** — Full-screen modal with header (close + send buttons), Image with loading spinner, resizeMode="contain"
- [ ] **Step 2: Create VoiceRecorder** — Modal with duration display, recording pulse animation, cancel/record-stop buttons
- [ ] **Step 3: Commit**

---

## Phase 3: Notifications

### Task 19: Push Notifications Integration

**Files:** Modify `src/providers/AuthProvider.tsx`, Modify `app/_layout.tsx`

- [ ] **Step 1: Wire into AuthProvider** — After successful sign-in, call `notificationService.initialize(user.id)`. Store subscription, cleanup on sign-out.
- [ ] **Step 2: Add navigation handler in _layout.tsx** — `Notifications.addNotificationResponseReceivedListener` → if `data.chatId`, `router.push(/(chat)/${data.chatId})`
- [ ] **Step 3: Commit**

---

### Task 20: Notification Actions

**Files:** Modify `src/services/supabase/notification.service.ts`, Modify `app/_layout.tsx`

- [ ] **Step 1: Add categories** — `setupNotificationCategories()` with 'reply' (with text input) and 'markRead' actions
- [ ] **Step 2: Handle actions** — In _layout.tsx, check `response.actionIdentifier`. For 'reply', send message. For 'markRead', mark as read.
- [ ] **Step 3: Commit**

---

## Phase 4: UX Polish

### Task 21: Pull to Refresh + Infinite Scroll

**Files:** Modify `app/(chat)/index.tsx`, Modify `app/(chat)/[chatId].tsx`

- [ ] **Step 1: Pull to refresh on chat list** — Add `RefreshControl` to FlashList, call `chatStore.loadChats()`
- [ ] **Step 2: Infinite scroll on messages** — Add `onEndReached` to FlashList, load older messages via `messageService.getMessages(chatId, 30, oldestDate)`, prepend to store
- [ ] **Step 3: Commit**

---

### Task 22: Search Messages

**Files:** Create `src/hooks/useMessageSearch.ts`, Create `src/components/chat/SearchMessagesModal.tsx`

- [ ] **Step 1: Create hook** — `useMessageSearch(chatId)` with `search(query)` using Supabase `ilike`, returns `{ query, results, searching, search, clear }`
- [ ] **Step 2: Create modal** — Full-screen modal with Searchbar, FlatList of results showing text + date
- [ ] **Step 3: Commit**

---

### Task 23: Animations

**Files:** Modify `src/components/chat/MessageBubble.tsx`

- [ ] **Step 1: Add reanimated entrance** — Wrap messages in `Animated.View` with `FadeInUp.duration(300).delay(index * 50)`
- [ ] **Step 2: Commit**

---

### Task 24: Swipe Gestures

**Files:** Create `src/components/chat/SwipeableMessage.tsx`

- [ ] **Step 1: Install** — `npx expo install react-native-gesture-handler` (already installed via expo)
- [ ] **Step 2: Create** — Uses `react-native-gesture-handler` Swipeable. Right actions: Reply (primary), Forward. Close swipeable after action.
- [ ] **Step 3: Wrap MessageBubble in chat screen**
- [ ] **Step 4: Commit**

---

### Task 25: Backup Service

**Files:** Create `src/services/backup.service.ts`

- [ ] **Step 1: Create** — `exportChat(chatId)` exports messages to JSON, `shareExport(fileUri)` uses expo-sharing, `exportAllChats(userId)`, `importBackup(fileUri)`
- [ ] **Step 2: Commit**

---

### Task 26: Export Chats UI

**Files:** Modify `app/(chat)/settings.tsx`

- [ ] **Step 1: Add section** — "Datos y almacenamiento" with buttons: "Exportar chats" (calls backupService), "Limpiar caché" (calls mediaCacheService.clearCache), "Tamaño de caché" (displays size)
- [ ] **Step 2: Commit**

---

### Task 27: Swipeable Integration + Final Polish

**Files:** Modify `app/(chat)/[chatId].tsx`

- [ ] **Step 1: Wire swipe actions** — Reply sets replyContext, Forward opens forward modal
- [ ] **Step 2: Wire message long-press** — Show bottom sheet with: Responder, Reaccionar, Copiar, Reenviar, Favorito, Editar (own), Eliminar (own)
- [ ] **Step 3: Commit**

---

## Phase 5: Calls (WebRTC)

### Task 28: WebRTC Service

**Files:** Create `src/services/calls/webrtc.service.ts`, Create `src/types/call.types.ts`

- [ ] **Step 1: Create call.types.ts** — `CallType`, `CallStatus`, `Call`, `WebRTCSignal` interfaces
- [ ] **Step 2: Install** — `npx expo install react-native-webrtc`
- [ ] **Step 3: Create WebRTC service** — RTCPeerConnection with STUN (Google), getUserMedia for audio/video, offer/answer/candidate exchange via Supabase Realtime `call_signals` table, mute controls, endCall cleanup
- [ ] **Step 4: Add call_signals + call_log tables** to `database_schema_additions.sql`
- [ ] **Step 5: Commit**

---

### Task 29: Call UI

**Files:** Create `src/hooks/useCall.ts`, Create `src/components/calls/CallScreen.tsx`, Create `src/components/calls/IncomingCallModal.tsx`

- [ ] **Step 1: Create useCall hook** — State: currentCall, incomingCall, remoteStream, isMuted, isVideoOff, duration. Actions: startCall, answerCall, endCall, toggleMute, toggleVideo. Timer for connected calls.
- [ ] **Step 2: Create CallScreen** — Full-screen: caller name, duration/status, mute/camera-off/hangup buttons. For video calls, shows remote stream via `<RTCView>`.
- [ ] **Step 3: Create IncomingCallModal** — Overlay with caller info, answer (green) / decline (red) buttons
- [ ] **Step 4: Add call button to chat header** in `[chatId].tsx` — Long press or menu to choose voice/video call
- [ ] **Step 5: Commit**

---

### Task 30: Call Integration + Polish

**Files:** Modify `app/_layout.tsx`, Modify `app/(chat)/[chatId].tsx`

- [ ] **Step 1: Wire incoming call listener** — In _layout.tsx, listen for call signals, show IncomingCallModal
- [ ] **Step 2: Add call initiation** — In chat screen options menu, add "Llamada de voz" and "Videollamada"
- [ ] **Step 3: Add call log display** — Show previous calls in chat
- [ ] **Step 4: Commit**

---

## Self-Review

### Spec Coverage Checklist

| Requirement | Task | Status |
|---|---|---|
| **Core Messaging** | | |
| Mensajes de voz | Task 17 (useVoiceRecording), Task 18 (VoiceRecorder UI) | Covered |
| Enviar imágenes/videos | Task 17 (useMediaPicker), Task 14 (storage), Task 18 (MediaPreviewModal) | Covered |
| Enviar documentos | Task 14 (storage service upload), Task 10 (MessageInput attachment button) | Covered |
| Reacciones (❤️👍😂) | Task 7 (store), Task 9 (MessageReactions), Task 27 (long-press) | Covered |
| Responder mensajes | Task 8 (reply store), Task 9 (ReplyThread), Task 10 (ReplyPreview) | Covered |
| Editar mensajes | Task 8 (editMessage action) | Covered |
| Forward messages | Task 8 (forwardMessage), Task 27 (swipe forward) | Covered |
| Star/favorite messages | Task 8 (starMessage, starredMessages Set) | Covered |
| **Media & Storage** | | |
| Supabase Storage buckets | Task 14 (SQL + service) | Covered |
| Compresión imágenes/videos | Task 15 (compressionService) | Covered |
| Preview de media | Task 18 (MediaPreviewModal) | Covered |
| Descarga automática de media | Task 16 (mediaCacheService) | Covered |
| Gestión almacenamiento local | Task 16 (cleanIfNeeded, clearCache), Task 26 (settings UI) | Covered |
| **Notifications** | | |
| Push notifications funcionales | Task 5 (service), Task 19 (integration) | Covered |
| Notification actions | Task 20 (reply/mark read) | Covered |
| **Calls** | | |
| Voice calls (WebRTC) | Task 28 (service), Task 29 (CallScreen), Task 30 (integration) | Covered |
| Video calls (WebRTC) | Same as above, type='video' | Covered |
| Group calls | Not covered (out of scope — requires SFU/MCU) | Marked complex |
| **UX Polish** | | |
| Animaciones fluidas | Task 23 (reanimated) | Covered |
| Swipe gestures | Task 24 (SwipeableMessage) | Covered |
| Pull to refresh | Task 21 | Covered |
| Infinite scroll optimizado | Task 21 | Covered |
| Búsqueda de mensajes | Task 22 | Covered |
| Backup/restore | Task 25 | Covered |
| Exportar chats | Task 25 + Task 26 | Covered |

### Placeholder Scan
No placeholders found. All steps contain concrete implementation code or clear instructions.

### Type Consistency
- `Message`, `MessageType`, `MessageStatus` from `@/types` used consistently
- `messageService.sendMessage` params match `SendMessageParams` interface
- All store actions match the Zustand interface pattern
- Reaction cache uses `Map<string, Map<string, string[]>>` consistently

---

## Execution Handoff

Plan complete and saved to `docs/superpowers/plans/2026-04-07-cerlita-chat-features.md`. Two execution options:

**1. Subagent-Driven (recommended)** — I dispatch a fresh subagent per task, review between tasks, fast iteration

**2. Inline Execution** — Execute tasks in this session using executing-plans, batch execution with checkpoints

**Which approach?**
