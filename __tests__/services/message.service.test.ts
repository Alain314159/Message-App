import { messageService } from '@/services/supabase/message.service';
import { supabase } from '@/services/supabase/config';

jest.mock('@/services/supabase/config', () => ({
  supabase: {
    from: jest.fn(),
    channel: jest.fn(),
  },
}));

describe('messageService', () => {
  beforeEach(() => jest.clearAllMocks());

  describe('getMessages', () => {
    it('should get messages for a chat', async () => {
      const mockMessages = [
        { id: '1', content: 'Hello', chat_id: 'chat-123' },
        { id: '2', content: 'Hi', chat_id: 'chat-123' },
      ];
      const mockChain = {
        select: jest.fn().mockReturnThis(),
        eq: jest.fn().mockReturnThis(),
        order: jest.fn().mockReturnThis(),
        limit: jest.fn().mockResolvedValue({ data: mockMessages, error: null }),
      };
      (supabase.from as jest.Mock).mockReturnValue(mockChain);

      const result = await messageService.getMessages('chat-123');

      expect(supabase.from).toHaveBeenCalledWith('messages');
      expect(result).toEqual(mockMessages);
    });

    it('should support pagination with before date', async () => {
      const mockMessages = [{ id: '3', content: 'Older' }];
      const beforeDate = new Date('2024-01-01');
      const mockChain = {
        select: jest.fn().mockReturnThis(),
        eq: jest.fn().mockReturnThis(),
        order: jest.fn().mockReturnThis(),
        limit: jest.fn().mockReturnThis(),
        lt: jest.fn().mockResolvedValue({ data: mockMessages, error: null }),
      };
      (supabase.from as jest.Mock).mockReturnValue(mockChain);

      const result = await messageService.getMessages('chat-123', 20, beforeDate);
      expect(result).toEqual(mockMessages);
      expect(mockChain.lt).toHaveBeenCalledWith('created_at', beforeDate.toISOString());
    });
  });

  describe('sendMessage', () => {
    it('should send a text message', async () => {
      const mockMsg = { id: '1', content: 'encrypted', message_type: 'text' };
      const mockChain = {
        insert: jest.fn().mockReturnThis(),
        select: jest.fn().mockReturnThis(),
        single: jest.fn().mockResolvedValue({ data: mockMsg, error: null }),
      };
      (supabase.from as jest.Mock).mockReturnValue(mockChain);

      await messageService.sendMessage({
        chatId: 'chat-123',
        senderId: 'user-1',
        content: 'encrypted-text',
        messageType: 'text',
      });

      expect(mockChain.insert).toHaveBeenCalledWith({
        chat_id: 'chat-123',
        sender_id: 'user-1',
        content: 'encrypted-text',
        message_type: 'text',
        media_url: null,
        thumbnail_url: null,
        reply_to_id: null,
        status: 'sent',
      });
    });

    it('should send a message with media and reply', async () => {
      const mockMsg = { id: '2', message_type: 'image' };
      const mockChain = {
        insert: jest.fn().mockReturnThis(),
        select: jest.fn().mockReturnThis(),
        single: jest.fn().mockResolvedValue({ data: mockMsg, error: null }),
      };
      (supabase.from as jest.Mock).mockReturnValue(mockChain);

      await messageService.sendMessage({
        chatId: 'chat-123',
        senderId: 'user-1',
        content: 'encrypted',
        messageType: 'image',
        mediaUrl: 'https://example.com/img.jpg',
        replyToId: 'reply-1',
      });

      expect(mockChain.insert).toHaveBeenCalledWith(expect.objectContaining({
        message_type: 'image',
        media_url: 'https://example.com/img.jpg',
        reply_to_id: 'reply-1',
      }));
    });
  });

  describe('updateMessageStatus', () => {
    it('should update message status', async () => {
      const mockChain = {
        update: jest.fn().mockReturnThis(),
        eq: jest.fn().mockResolvedValue({ error: null }),
      };
      (supabase.from as jest.Mock).mockReturnValue(mockChain);

      await messageService.updateMessageStatus('msg-1', 'read');

      expect(mockChain.update).toHaveBeenCalledWith({ status: 'read' });
      expect(mockChain.eq).toHaveBeenCalledWith('id', 'msg-1');
    });
  });

  describe('markAllAsRead', () => {
    it('should mark all messages as read', async () => {
      const mockChain = {
        update: jest.fn().mockReturnThis(),
        eq: jest.fn().mockReturnThis(),
        neq: jest.fn().mockReturnThis(),
        in: jest.fn().mockResolvedValue({ error: null }),
      };
      (supabase.from as jest.Mock).mockReturnValue(mockChain);

      await messageService.markAllAsRead('chat-123', 'user-1');

      expect(mockChain.update).toHaveBeenCalledWith({ status: 'read' });
      expect(mockChain.eq).toHaveBeenCalledWith('chat_id', 'chat-123');
    });
  });

  describe('deleteMessage', () => {
    it('should delete a message', async () => {
      const mockChain = {
        delete: jest.fn().mockReturnThis(),
        eq: jest.fn().mockResolvedValue({ error: null }),
      };
      (supabase.from as jest.Mock).mockReturnValue(mockChain);

      await messageService.deleteMessage('msg-1');
      expect(mockChain.eq).toHaveBeenCalledWith('id', 'msg-1');
    });
  });

  describe('subscribeToMessages', () => {
    it('should subscribe to realtime messages', async () => {
      const mockChannel = {
        on: jest.fn().mockReturnThis(),
        subscribe: jest.fn(),
      };
      (supabase.channel as jest.Mock).mockReturnValue(mockChannel);

      const callback = jest.fn();
      messageService.subscribeToMessages('chat-123', callback);

      expect(supabase.channel).toHaveBeenCalledWith('messages:chat-123');
      expect(mockChannel.on).toHaveBeenCalled();
      expect(mockChannel.subscribe).toHaveBeenCalled();
    });
  });
});
