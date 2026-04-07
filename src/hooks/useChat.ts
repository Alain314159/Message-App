import { useEffect, useCallback } from 'react';
import { useChatStore } from '@/store/chatStore';
import { useAuthStore } from '@/store/authStore';
import { chatService } from '@/services/supabase/chat.service';

export function useChat(chatId?: string) {
  const {
    chats,
    activeChat,
    loading,
    error,
    loadChats,
    createChat,
    setActiveChat,
    subscribeToChats,
    unsubscribeFromChats,
    setError,
  } = useChatStore();

  const { user } = useAuthStore();

  useEffect(() => {
    if (user) {
      subscribeToChats(user.id);
    }
    return () => { unsubscribeFromChats(); };
  }, [user]);

  useEffect(() => {
    if (chatId) {
      chatService.getChatById(chatId).then((chat) => {
        setActiveChat(chat as any);
      }).catch(console.error);
    }
  }, [chatId]);

  const getParticipants = useCallback(async () => {
    if (!chatId) return [];
    return chatService.getParticipants(chatId);
  }, [chatId]);

  return {
    chats,
    activeChat,
    loading,
    error,
    loadChats,
    createChat,
    getParticipants,
    setActiveChat,
    setError,
  };
}
