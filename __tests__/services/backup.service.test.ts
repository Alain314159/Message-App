import { backupService } from '@/services/backup.service';
import * as FileSystem from 'expo-file-system';
import * as Sharing from 'expo-sharing';
import AsyncStorage from '@react-native-async-storage/async-storage';

jest.mock('@/services/supabase/config', () => ({
  supabase: { from: jest.fn() },
}));
jest.mock('expo-file-system', () => ({
  documentDirectory: 'file:///data/',
  getInfoAsync: jest.fn(),
  makeDirectoryAsync: jest.fn(),
  downloadAsync: jest.fn(),
  deleteAsync: jest.fn(),
  readAsStringAsync: jest.fn(),
  writeAsStringAsync: jest.fn(),
}));
jest.mock('expo-sharing', () => ({ isAvailableAsync: jest.fn(), shareAsync: jest.fn() }));
jest.mock('@react-native-async-storage/async-storage', () => ({
  getItem: jest.fn(),
  setItem: jest.fn(),
  removeItem: jest.fn(),
}));

describe('backupService', () => {
  beforeEach(() => jest.clearAllMocks());

  describe('exportChat', () => {
    it('should export chat to JSON file', async () => {
      const mockMessages = [{ id: '1', sender_id: 'u1', message_type: 'text', content: 'Hi', created_at: '2024-01-01' }];
      const mockChain = {
        select: jest.fn().mockReturnThis(),
        eq: jest.fn().mockReturnThis(),
        order: jest.fn().mockResolvedValue({ data: mockMessages, error: null }),
      };
      (require('@/services/supabase/config').supabase.from as jest.Mock).mockReturnValue(mockChain);
      (FileSystem.writeAsStringAsync as jest.Mock).mockResolvedValue(undefined);

      const result = await backupService.exportChat('chat-123');

      expect(result).toBe('file:///data/chat_export_chat-123.json');
      expect(FileSystem.writeAsStringAsync).toHaveBeenCalled();
    });
  });

  describe('shareExport', () => {
    it('should share file when sharing is available', async () => {
      (Sharing.isAvailableAsync as jest.Mock).mockResolvedValue(true);
      await backupService.shareExport('file:///data/export.json');
      expect(Sharing.shareAsync).toHaveBeenCalledWith('file:///data/export.json');
    });

    it('should not share when sharing is not available', async () => {
      (Sharing.isAvailableAsync as jest.Mock).mockResolvedValue(false);
      await backupService.shareExport('file:///data/export.json');
      expect(Sharing.shareAsync).not.toHaveBeenCalled();
    });
  });

  describe('importBackup', () => {
    it('should return true for valid backup file', async () => {
      const validData = JSON.stringify({ chatId: 'c1', messages: [] });
      (FileSystem.readAsStringAsync as jest.Mock).mockResolvedValue(validData);
      const result = await backupService.importBackup('file:///backup.json');
      expect(result).toBe(true);
      expect(AsyncStorage.setItem).toHaveBeenCalled();
    });

    it('should return false for invalid backup file', async () => {
      (FileSystem.readAsStringAsync as jest.Mock).mockResolvedValue('invalid');
      const result = await backupService.importBackup('file:///bad.json');
      expect(result).toBe(false);
    });

    it('should return false for missing fields', async () => {
      (FileSystem.readAsStringAsync as jest.Mock).mockResolvedValue(JSON.stringify({ foo: 'bar' }));
      const result = await backupService.importBackup('file:///missing.json');
      expect(result).toBe(false);
    });
  });
});
