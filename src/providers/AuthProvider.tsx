import React, { createContext, useContext, useEffect, useState, useCallback } from 'react';
import { useRouter, useSegments } from 'expo-router';
import { AppState, type AppStateStatus } from 'react-native';
import { useAuthStore } from '@/store/authStore';
import { notificationService } from '@/services/supabase/notification.service';
import { SplashScreen } from '@/components/ui/SplashScreen';
import { authService } from '@/services/supabase/auth.service';

interface AuthContextType {
  user: any;
  isLoading: boolean;
  isAuthenticated: boolean;
  signIn: (email: string, password: string) => Promise<void>;
  signUp: (email: string, password: string, displayName: string) => Promise<void>;
  signOut: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const segments = useSegments();
  const router = useRouter();
  const { user, isAuthenticated, loading, initialize, signOut: storeSignOut, signIn: storeSignIn, signUp: storeSignUp } = useAuthStore();
  const [isInitializing, setIsInitializing] = useState(true);

  // Inicializar autenticación
  useEffect(() => {
    const initAuth = async () => {
      try {
        await initialize();
      } catch (error) {
        console.error('Failed to initialize auth:', error);
      } finally {
        setIsInitializing(false);
      }
    };

    initAuth();
  }, []);

  // Manejar cambios de estado de la app (background/foreground)
  useEffect(() => {
    const handleAppStateChange = async (nextAppState: AppStateStatus) => {
      // Presence tracking would need Supabase presence - skip for now
      console.log('App state changed:', nextAppState);
    };

    const subscription = AppState.addEventListener('change', handleAppStateChange);
    return () => { subscription.remove(); };
  }, [user]);

  // Inicializar notificaciones cuando está autenticado
  useEffect(() => {
    if (isAuthenticated && !loading && user?.id) {
      const sub = notificationService.initialize(user.id);
      return () => { notificationService.cleanup(sub); };
    }
  }, [isAuthenticated, loading, user?.id]);

  // Proteger rutas
  useEffect(() => {
    if (isInitializing || loading) return;

    const inAuthGroup = segments[0] === '(auth)';
    const inChatGroup = segments[0] === '(chat)';

    if (!isAuthenticated && !inAuthGroup) {
      // No autenticado, redirigir a login
      router.replace('/(auth)/login');
    } else if (isAuthenticated && inAuthGroup) {
      // Autenticado pero en rutas de auth, redirigir a chats
      router.replace('/(chat)');
    }
  }, [isAuthenticated, loading, isInitializing, segments]);

  // Función de signOut
  const signOut = useCallback(async () => {
    try {
      await storeSignOut();
      router.replace('/(auth)/login');
    } catch (error) {
      console.error('Failed to sign out:', error);
    }
  }, [storeSignOut, router]);

  // Mostrar pantalla de carga mientras inicializa
  if (isInitializing || loading) {
    return <SplashScreen />;
  }

  return (
    <AuthContext.Provider value={{ user, isLoading: loading, isAuthenticated, signIn: storeSignIn, signUp: storeSignUp, signOut }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth debe ser usado dentro de un AuthProvider');
  }
  return context;
};
