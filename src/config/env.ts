import { Platform } from 'react-native';

// Environment variables from app.json or .env
const { EXPO_PUBLIC_SUPABASE_URL, EXPO_PUBLIC_SUPABASE_ANON_KEY } = process.env;

export const SUPABASE_URL = EXPO_PUBLIC_SUPABASE_URL || '';
export const SUPABASE_ANON_KEY = EXPO_PUBLIC_SUPABASE_ANON_KEY || '';

if (!SUPABASE_URL || !SUPABASE_ANON_KEY) {
  console.warn(
    '⚠️ Supabase credentials not configured. Copy .env.example to .env and add your credentials.'
  );
}

export const config = {
  supabase: {
    url: EXPO_PUBLIC_SUPABASE_URL || '',
    anonKey: EXPO_PUBLIC_SUPABASE_ANON_KEY || '',
  },
  
  app: {
    name: 'Cerlita Chat',
    version: '1.0.0',
    scheme: 'cerlitachat',
  },
  
  features: {
    enableTypingIndicator: true,
    enablePresence: true,
    enableNotifications: true,
    enableE2EEncryption: true,
    maxImageSizeMB: 10,
    maxVideoSizeMB: 50,
    messagePageSize: 50,
  },
  
  isIOS: Platform.OS === 'ios',
  isAndroid: Platform.OS === 'android',
  isWeb: Platform.OS === 'web',
};
