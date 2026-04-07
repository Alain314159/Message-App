import { supabase } from '@/services/supabase/config';
import * as FileSystem from 'expo-file-system';
import * as Sharing from 'expo-sharing';
import AsyncStorage from '@react-native-async-storage/async-storage';

export const backupService = {
  async exportChat(chatId: string) {
    const { data: messages, error } = await supabase
      .from('messages')
      .select('*')
      .eq('chat_id', chatId)
      .order('created_at', { ascending: true });

    if (error) throw new Error(error.message);

    const exportData = {
      chatId,
      exportedAt: new Date().toISOString(),
      messages: messages.map((m: any) => ({
        id: m.id,
        sender_id: m.sender_id,
        type: m.message_type,
        content: m.content,
        created_at: m.created_at,
      })),
    };

    const fileUri = `${FileSystem.documentDirectory}chat_export_${chatId}.json`;
    await FileSystem.writeAsStringAsync(fileUri, JSON.stringify(exportData, null, 2));
    return fileUri;
  },

  async shareExport(fileUri: string) {
    if (await Sharing.isAvailableAsync()) {
      await Sharing.shareAsync(fileUri);
    }
  },

  async exportAllChats(userId: string) {
    type ChatRow = { id: string; name: string | null; is_group: boolean; participant_ids: string[]; last_message_id: string | null; created_at: string; updated_at: string };

    const { data: chats, error } = await supabase
      .from('chats')
      .select('*')
      .contains('participant_ids', [userId]) as { data: ChatRow[] | null; error: { message: string } | null };

    if (error) throw new Error(error.message);

    const results: { chatId: string; fileUri: string }[] = [];

    if (!chats || chats.length === 0) return results;

    for (const chat of chats) {
      const fileUri = await this.exportChat(chat.id);
      results.push({ chatId: chat.id, fileUri });
    }
    return results;
  },

  async importBackup(fileUri: string): Promise<boolean> {
    try {
      const content = await FileSystem.readAsStringAsync(fileUri);
      const data = JSON.parse(content);
      if (!data.chatId || !data.messages) return false;
      await AsyncStorage.setItem(`backup_${data.chatId}`, JSON.stringify(data));
      return true;
    } catch {
      return false;
    }
  },
};
