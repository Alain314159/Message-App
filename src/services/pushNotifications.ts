import * as Notifications from 'expo-notifications';
import * as Device from 'expo-device';
import { Platform } from 'react-native';
import Constants from 'expo-constants';
import { doc, setDoc, deleteDoc, collection, serverTimestamp } from 'firebase/firestore';
import { db } from '@/config/firebase';
import type { Subscription } from 'expo-notifications';

// ---------------------------------------------------------------------------
// Types
// ---------------------------------------------------------------------------

export interface NotificationData {
  type?: string;
  chatId?: string;
  messageId?: string;
  senderId?: string;
  [key: string]: unknown;
}

export interface NotificationSetupResult {
  success: boolean;
  fcmToken: string | null;
  error: string | null;
}

// ---------------------------------------------------------------------------
// Expo notification handler — controls foreground behaviour
// ---------------------------------------------------------------------------

Notifications.setNotificationHandler({
  handleNotification: async () => ({
    shouldShowAlert: true,
    shouldPlaySound: true,
    shouldSetBadge: true,
    shouldShowBanner: true,
    shouldShowList: true,
  }),
});

// ---------------------------------------------------------------------------
// Permission request (iOS + Android)
// ---------------------------------------------------------------------------

async function requestPermissions(): Promise<boolean> {
  const { status: existingStatus } = await Notifications.getPermissionsAsync();

  if (existingStatus !== 'granted') {
    const { status } = await Notifications.requestPermissionsAsync({
      ios: {
        allowAlert: true,
        allowBadge: true,
        allowSound: true,
        allowAnnouncements: true,
      },
    });

    if (status !== 'granted') {
      console.warn('[PushNotifications] Permission not granted');
      return false;
    }
  }

  return true;
}

// ---------------------------------------------------------------------------
// Android notification channel
// ---------------------------------------------------------------------------

async function configureAndroidChannel(): Promise<void> {
  if (Platform.OS !== 'android') return;

  await Notifications.setNotificationChannelAsync('default', {
    name: 'Default',
    importance: Notifications.AndroidImportance.HIGH,
    vibrationPattern: [0, 250, 250, 250],
    lightColor: '#FF69B4',
    sound: 'default',
    enableVibrate: true,
    enableLights: true,
    lockscreenVisibility: Notifications.AndroidNotificationVisibility.PUBLIC,
  });

  // High-priority channel for chat messages
  await Notifications.setNotificationChannelAsync('chat', {
    name: 'Chat Messages',
    importance: Notifications.AndroidImportance.MAX,
    vibrationPattern: [0, 250, 250, 250],
    lightColor: '#FF69B4',
    sound: 'default',
    enableVibrate: true,
    enableLights: true,
    bypassDnd: true,
    lockscreenVisibility: Notifications.AndroidNotificationVisibility.PUBLIC,
  });
}

// ---------------------------------------------------------------------------
// FCM token retrieval (native) / Expo push token (fallback)
// ---------------------------------------------------------------------------

async function getPushToken(): Promise<string | null> {
  if (!Device.isDevice) {
    console.warn('[PushNotifications] Must use a physical device for push notifications');
    return null;
  }

  const projectId = Constants.expoConfig?.extra?.eas?.projectId;
  const experienceId = Constants.expoConfig?.extra?.router?.origin;

  try {
    // On native builds with FCM configured (google-services.json / APNs key),
    // expo-notifications returns the FCM/APNs token directly.
    const tokenResponse = await Notifications.getDevicePushTokenAsync({
      projectId,
      experienceId,
    });

    return tokenResponse.data ?? null;
  } catch (error) {
    console.error('[PushNotifications] Failed to get device push token:', error);
    return null;
  }
}

// ---------------------------------------------------------------------------
// Firestore — save / remove FCM token
// ---------------------------------------------------------------------------

/**
 * Save an FCM token to Firestore under `users/{uid}/fcmTokens/{tokenId}`.
 * Each token is stored as a separate document so stale tokens can be cleaned up.
 */
export async function saveFcmTokenToFirestore(uid: string, token: string): Promise<void> {
  try {
    const tokenId = token.slice(-20); // use last 20 chars as doc id for uniqueness
    const tokenRef = doc(db, 'users', uid, 'fcmTokens', tokenId);

    await setDoc(tokenRef, {
      token,
      platform: Platform.OS,
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
      appVersion: Constants.expoConfig?.version ?? 'unknown',
    });

    console.log('[PushNotifications] FCM token saved to Firestore');
  } catch (error) {
    console.error('[PushNotifications] Failed to save FCM token to Firestore:', error);
    throw error;
  }
}

/**
 * Remove a specific FCM token from Firestore.
 */
export async function removeFcmTokenFromFirestore(uid: string, token: string): Promise<void> {
  try {
    const tokenId = token.slice(-20);
    const tokenRef = doc(db, 'users', uid, 'fcmTokens', tokenId);
    await deleteDoc(tokenRef);
    console.log('[PushNotifications] FCM token removed from Firestore');
  } catch (error) {
    console.error('[PushNotifications] Failed to remove FCM token from Firestore:', error);
  }
}

// ---------------------------------------------------------------------------
// Notification tap handler
// ---------------------------------------------------------------------------

function handleNotificationTap(response: Notifications.NotificationResponse): void {
  const data = response.notification.request.content.data as NotificationData | undefined;
  console.log('[PushNotifications] Notification tapped:', {
    type: data?.type,
    chatId: data?.chatId,
    messageId: data?.messageId,
    actionIdentifier: response.actionIdentifier,
  });

  // Navigation is handled by the consumer via the listener callback.
  // We re-emit via a global event so the root layout can navigate.
  if (typeof window !== 'undefined' && window.dispatchEvent) {
    window.dispatchEvent(
      new CustomEvent('notification-tap', { detail: data })
    );
  }
}

// ---------------------------------------------------------------------------
// Incoming notification handler
// ---------------------------------------------------------------------------

function handleNotificationReceived(notification: Notifications.Notification): void {
  const data = notification.request.content.data as NotificationData | undefined;
  console.log('[PushNotifications] Notification received:', {
    title: notification.request.content.title,
    body: notification.request.content.body,
    type: data?.type,
    chatId: data?.chatId,
  });
}

// ---------------------------------------------------------------------------
// Public API
// ---------------------------------------------------------------------------

/**
 * Initialise the full FCM push notification stack for a given user.
 *
 * Call this once the user is authenticated (e.g. in AuthProvider or _layout).
 *
 * @returns A cleanup function that removes all listeners.
 */
export function initializePushNotifications(
  uid: string,
  onTapCallback?: (data: NotificationData) => void,
  onReceivedCallback?: (notification: Notifications.Notification) => void
): () => void {
  let receivedListener: Subscription | null = null;
  let responseListener: Subscription | null = null;
  let initialised = true;

  (async () => {
    try {
      // 1. Request permissions
      const permitted = await requestPermissions();
      if (!permitted) {
        console.warn('[PushNotifications] Permissions denied — push notifications disabled');
        return;
      }

      // 2. Configure Android channels
      await configureAndroidChannel();

      // 3. Obtain the FCM / APNs token
      const token = await getPushToken();
      if (!token) {
        console.warn('[PushNotifications] Could not obtain push token');
        return;
      }

      console.log(`[PushNotifications] Got push token (${Platform.OS}): ${token.slice(0, 20)}...`);

      // 4. Save to Firestore
      await saveFcmTokenToFirestore(uid, token);

      // 5. Register listeners (only if still mounted)
      if (!initialised) return;

      receivedListener = Notifications.addNotificationReceivedListener((n) => {
        handleNotificationReceived(n);
        onReceivedCallback?.(n);
      });

      responseListener = Notifications.addNotificationResponseReceivedListener((r) => {
        handleNotificationTap(r);
        const data = r.notification.request.content.data as NotificationData | undefined;
        onTapCallback?.(data ?? {});
      });
    } catch (error) {
      console.error('[PushNotifications] Initialization failed:', error);
    }
  })();

  // Return cleanup function
  return () => {
    initialised = false;
    if (receivedListener) {
      Notifications.removeNotificationSubscription(receivedListener);
    }
    if (responseListener) {
      Notifications.removeNotificationSubscription(responseListener);
    }
    console.log('[PushNotifications] Cleanup complete');
  };
}

/**
 * Standalone helper to re-register the token (e.g. after login or token refresh).
 */
export async function refreshFcmToken(uid: string): Promise<string | null> {
  const token = await getPushToken();
  if (token) {
    await saveFcmTokenToFirestore(uid, token);
  }
  return token;
}
