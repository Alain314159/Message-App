import { chatService } from '@/services/supabase/chat.service';
import { supabase } from '@/services/supabase/config';

jest.mock('@/services/supabase/config', () => ({
  supabase: {
    from: jest.fn(),
    rpc: jest.fn(),
    channel: jest.fn(),
  },
}));

describe('chatService', () => {
  beforeEach(() => jest.clearAllMocks());

  describe('getUserChats', () => {
    it('should get user chats', async () => {
      const mockChats = [{ id: '1', name: 'Test Chat', participant_ids: ['user-123'] }];
      const mockChain = {
        select: jest.fn().mockReturnThis(),
        contains: jest.fn().mockReturnThis(),
        order: jest.fn().mockResolvedValue({ data: mockChats, error: null }),
      };
      (supabase.from as jest.Mock).mockReturnValue(mockChain);

      const result = await chatService.getUserChats('user-123');

      expect(supabase.from).toHaveBeenCalledWith('chats');
      expect(mockChain.select).toHaveBeenCalledWith('*');
      expect(mockChain.contains).toHaveBeenCalledWith('participant_ids', ['user-123']);
      expect(result).toEqual(mockChats);
    });

    it('should throw on error', async () => {
      const mockChain = {
        select: jest.fn().mockReturnThis(),
        contains: jest.fn().mockReturnThis(),
        order: jest.fn().mockResolvedValue({ data: null, error: { message: 'DB error' } }),
      };
      (supabase.from as jest.Mock).mockReturnValue(mockChain);

      await expect(chatService.getUserChats('user-123')).rejects.toThrow('DB error');
    });
  });

  describe('getOrCreateDirectChat', () => {
    it('should create a direct chat using RPC', async () => {
      (supabase.rpc as jest.Mock).mockResolvedValue({ data: 'chat-123', error: null });

      const result = await chatService.getOrCreateDirectChat('user-1', 'user-2');

      expect(supabase.rpc).toHaveBeenCalledWith('get_or_create_direct_chat', {
        user1_id: 'user-1',
        user2_id: 'user-2',
      });
      expect(result).toBe('chat-123');
    });

    it('should throw on RPC error', async () => {
      (supabase.rpc as jest.Mock).mockResolvedValue({
        data: null,
        error: { message: 'RPC failed' },
      });

      await expect(chatService.getOrCreateDirectChat('u1', 'u2')).rejects.toThrow('RPC failed');
    });
  });

  describe('subscribeToUserChats', () => {
    it('should subscribe to realtime changes', async () => {
      const mockChannel = {
        on: jest.fn().mockReturnThis(),
        subscribe: jest.fn(),
      };
      (supabase.channel as jest.Mock).mockReturnValue(mockChannel);

      const callback = jest.fn();
      chatService.subscribeToUserChats('user-123', callback);

      expect(supabase.channel).toHaveBeenCalledWith('chats:user-123');
      expect(mockChannel.on).toHaveBeenCalled();
      expect(mockChannel.subscribe).toHaveBeenCalled();
    });
  });

  describe('getChatById', () => {
    it('should get chat by id', async () => {
      const mockChat = { id: 'chat-1', name: 'Test' };
      const mockChain = {
        select: jest.fn().mockReturnThis(),
        eq: jest.fn().mockReturnThis(),
        single: jest.fn().mockResolvedValue({ data: mockChat, error: null }),
      };
      (supabase.from as jest.Mock).mockReturnValue(mockChain);

      const result = await chatService.getChatById('chat-1');
      expect(result).toEqual(mockChat);
    });
  });

  describe('createChat', () => {
    it('should create a group chat', async () => {
      const mockChat = { id: 'g1', name: 'Group', is_group: true };
      const mockChain = {
        insert: jest.fn().mockReturnThis(),
        select: jest.fn().mockReturnThis(),
        single: jest.fn().mockResolvedValue({ data: mockChat, error: null }),
      };
      (supabase.from as jest.Mock).mockReturnValue(mockChain);

      const result = await chatService.createChat({ name: 'Group', isGroup: true, participantIds: ['u1', 'u2'] });
      expect(result).toEqual(mockChat);
      expect(mockChain.insert).toHaveBeenCalledWith({
        name: 'Group',
        is_group: true,
        participant_ids: ['u1', 'u2'],
      });
    });
  });
});
