import { initializeApp, getApps, type FirebaseApp } from 'firebase/app';
import { getAuth } from 'firebase/auth';
import { getFirestore } from 'firebase/firestore';
import { getStorage } from 'firebase/storage';
import { getMessaging, type Messaging } from 'firebase/messaging';
import { getAnalytics } from 'firebase/analytics';
import { Platform } from 'react-native';

const firebaseConfig = {
  apiKey: process.env.EXPO_PUBLIC_FIREBASE_API_KEY,
  authDomain: process.env.EXPO_PUBLIC_FIREBASE_AUTH_DOMAIN,
  projectId: process.env.EXPO_PUBLIC_FIREBASE_PROJECT_ID,
  storageBucket: process.env.EXPO_PUBLIC_FIREBASE_STORAGE_BUCKET,
  messagingSenderId: process.env.EXPO_PUBLIC_FIREBASE_MESSAGING_SENDER_ID,
  appId: process.env.EXPO_PUBLIC_FIREBASE_APP_ID,
};

// Validate configuration
const requiredKeys = ['apiKey', 'authDomain', 'projectId', 'storageBucket', 'messagingSenderId', 'appId'] as const;
const missingKeys = requiredKeys.filter(key => !firebaseConfig[key]);

if (missingKeys.length > 0) {
  console.warn(
    `⚠️ Firebase configuration incomplete. Missing: ${missingKeys.join(', ')}. ` +
    'Add Firebase variables to your .env file.'
  );
}

// Initialize Firebase app (singleton)
const app: FirebaseApp = getApps().length === 0 ? initializeApp(firebaseConfig) : getApps()[0];

// Initialize core Firebase services
export const auth = getAuth(app);
export const db = getFirestore(app);
export const storage = getStorage(app);

// Analytics (web only)
export const analytics = Platform.OS === 'web' && typeof window !== 'undefined'
  ? getAnalytics(app)
  : null;

// Cloud Messaging — web uses firebase/messaging SDK; native uses expo-notifications + FCM token
let nativeMessagingInstance: Messaging | null = null;

if (Platform.OS === 'web' && typeof window !== 'undefined') {
  nativeMessagingInstance = getMessaging(app);
} else {
  // On native platforms, initialize Messaging for Firestore integration.
  // The actual FCM token is obtained via expo-notifications (not firebase/messaging),
  // but we still initialize the SDK so it's available for web-compatible flows.
  try {
    nativeMessagingInstance = getMessaging(app);
  } catch {
    // Safe to ignore — native token flow uses expo-notifications instead
    nativeMessagingInstance = null;
  }
}

/**
 * Get the Firebase Messaging instance.
 * On web this is the standard firebase/messaging SDK.
 * On native this may be null — use expo-notifications for the FCM token instead.
 */
export function getMessagingInstance(): Messaging | null {
  return nativeMessagingInstance;
}

export { app };
