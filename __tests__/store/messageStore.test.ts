import { useMessageStore } from '@/store/messageStore';

jest.mock('@/services/supabase/message.service', () => ({
  messageService: {
    getMessages: jest.fn().mockResolvedValue([]),
    sendMessage: jest.fn().mockResolvedValue({}),
    updateMessage: jest.fn().mockResolvedValue({}),
    deleteMessage: jest.fn().mockResolvedValue({}),
    subscribeToMessages: jest.fn(),
    markAllAsRead: jest.fn().mockResolvedValue({}),
    updateMessageStatus: jest.fn().mockResolvedValue({}),
  },
}));

jest.mock('@/services/crypto/e2e.service', () => ({
  e2eEncryptionService: {
    encrypt: jest.fn().mockResolvedValue({ ciphertext: 'enc', iv: 'iv' }),
    decrypt: jest.fn().mockResolvedValue('decrypted'),
  },
}));

jest.mock('@/services/supabase/config', () => ({
  supabase: {
    from: jest.fn(() => ({
      select: jest.fn().mockReturnThis(),
      eq: jest.fn().mockReturnThis(),
      single: jest.fn().mockResolvedValue({ data: null, error: null }),
      insert: jest.fn().mockReturnThis(),
      delete: jest.fn().mockReturnThis(),
      match: jest.fn().mockResolvedValue({ error: null }),
    })),
  },
}));

describe('messageStore', () => {
  beforeEach(() => {
    const store = useMessageStore.getState();
    useMessageStore.setState({
      messages: [],
      loading: false,
      error: null,
      typingUsers: new Set(),
      replyContext: null,
      starredMessages: new Set(),
      reactions: new Map(),
      currentUserId: 'user-1',
    });
  });

  it('should set current user id', () => {
    useMessageStore.getState().setCurrentUserId('test-user');
    expect(useMessageStore.getState().currentUserId).toBe('test-user');
  });

  it('should set reply context', () => {
    const ctx = { messageId: 'm1', senderName: 'Test', text: 'Hi', type: 'text' as const };
    useMessageStore.getState().setReplyContext(ctx);
    expect(useMessageStore.getState().replyContext).toEqual(ctx);
  });

  it('should clear reply context', () => {
    useMessageStore.getState().setReplyContext({ messageId: 'm1', senderName: 'T', text: 'Hi', type: 'text' as const });
    useMessageStore.getState().setReplyContext(null);
    expect(useMessageStore.getState().replyContext).toBeNull();
  });

  it('should star and unstar messages', () => {
    useMessageStore.getState().starMessage('msg-1');
    expect(useMessageStore.getState().starredMessages.has('msg-1')).toBe(true);
    useMessageStore.getState().unstarMessage('msg-1');
    expect(useMessageStore.getState().starredMessages.has('msg-1')).toBe(false);
  });

  it('should toggle typing users', () => {
    useMessageStore.getState().setTyping('user-2', true);
    expect(useMessageStore.getState().typingUsers.has('user-2')).toBe(true);
    useMessageStore.getState().setTyping('user-2', false);
    expect(useMessageStore.getState().typingUsers.has('user-2')).toBe(false);
  });

  it('should set error', () => {
    useMessageStore.getState().setError('Something went wrong');
    expect(useMessageStore.getState().error).toBe('Something went wrong');
    useMessageStore.getState().setError(null);
    expect(useMessageStore.getState().error).toBeNull();
  });

  it('should get reaction counts', () => {
    const store = useMessageStore.getState();
    const reactions = new Map();
    reactions.set('msg-1', new Map([['\u2764\uFE0F', ['user-1', 'user-2']]]));
    useMessageStore.setState({ reactions });
    const counts = store.getReactionCounts('msg-1', 'user-1');
    expect(counts['\u2764\uFE0F'].count).toBe(2);
    expect(counts['\u2764\uFE0F'].userReacted).toBe(true);
  });

  it('should unsubscribe from messages for specific chat', () => {
    const mockUnsub = jest.fn();
    useMessageStore.setState({
      channels: new Map([['chat-1', { unsubscribe: mockUnsub } as any]]),
    });
    useMessageStore.getState().unsubscribeFromMessages('chat-1');
    expect(mockUnsub).toHaveBeenCalled();
    expect(useMessageStore.getState().channels.has('chat-1')).toBe(false);
  });

  it('should unsubscribe from all messages', () => {
    const unsub1 = jest.fn();
    const unsub2 = jest.fn();
    useMessageStore.setState({
      channels: new Map([
        ['chat-1', { unsubscribe: unsub1 } as any],
        ['chat-2', { unsubscribe: unsub2 } as any],
      ]),
    });
    useMessageStore.getState().unsubscribeFromMessages();
    expect(unsub1).toHaveBeenCalled();
    expect(unsub2).toHaveBeenCalled();
    expect(useMessageStore.getState().channels.size).toBe(0);
  });
});
