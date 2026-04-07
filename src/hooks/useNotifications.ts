import { useEffect, useRef, useCallback } from 'react';
import * as Notifications from 'expo-notifications';
import { notificationService } from '@/services/supabase/notification.service';
import { useRouter } from 'expo-router';

export function useNotifications(userId: string | null) {
  const router = useRouter();
  const subscriptionRef = useRef<Notifications.Subscription | null>(null);

  useEffect(() => {
    if (!userId) return;

    const sub = notificationService.initialize(userId);
    subscriptionRef.current = sub;

    const responseListener = Notifications.addNotificationResponseReceivedListener((response) => {
      const actionIdentifier = response.actionIdentifier;
      const data = response.notification.request.content.data;

      if (actionIdentifier === 'reply') {
        console.log('Quick reply:', response.userText, data?.chatId);
      } else if (data?.chatId) {
        router.push(`/(chat)/${data.chatId}` as any);
      }
    });

    return () => {
      notificationService.cleanup(sub);
      Notifications.removeNotificationSubscription(responseListener);
    };
  }, [userId, router]);

  const setupCategories = useCallback(async () => {
    await Notifications.setNotificationCategoryAsync('message', [
      {
        identifier: 'reply',
        buttonTitle: 'Responder',
        options: { opensAppToForeground: true },
        textInput: { submitButtonTitle: 'Enviar', placeholder: 'Escribe...' },
      },
      {
        identifier: 'markRead',
        buttonTitle: 'Marcar como leído',
        options: { isDestructive: false, isAuthenticationRequired: false },
      },
    ]);
  }, []);

  return { setupCategories };
}
