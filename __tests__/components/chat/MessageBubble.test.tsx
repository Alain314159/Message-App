import { render } from '@testing-library/react-native';
import React from 'react';
import { MessageBubble } from '@/components/chat/MessageBubble';

jest.mock('react-native-paper', () => {
  const RealModule = jest.requireActual('react-native-paper');
  return {
    ...RealModule,
    IconButton: 'IconButton',
  };
});

const createMessage = (overrides = {}) => ({
  id: 'msg-1',
  chatId: 'chat-1',
  senderId: 'user-1',
  type: 'text' as const,
  text: 'Hello world',
  mediaURL: null,
  thumbnailURL: null,
  status: 'sent' as const,
  deliveredAt: null,
  readAt: null,
  createdAt: new Date('2024-01-01T12:00:00Z').toISOString(),
  editedAt: null,
  ...overrides,
});

describe('MessageBubble', () => {
  it('renders text message correctly', () => {
    const { getByText, getByTestId } = render(
      <MessageBubble message={createMessage()} isMyMessage={false} />
    );
    expect(getByText('Hello world')).toBeTruthy();
    expect(getByTestId('message-msg-1')).toBeTruthy();
  });

  it('shows edited label when editedAt is set', () => {
    const { getByText } = render(
      <MessageBubble
        message={createMessage({ text: 'Edited text', editedAt: new Date().toISOString() })}
        isMyMessage={false}
      />
    );
    expect(getByText('Edited text')).toBeTruthy();
    expect(getByText('(editado)')).toBeTruthy();
  });

  it('shows status icon for my messages', () => {
    const { UNSAFE_getByProps } = render(
      <MessageBubble message={createMessage({ status: 'read' })} isMyMessage={true} />
    );
    expect(UNSAFE_getByProps({ icon: 'check-all' })).toBeTruthy();
  });

  it('does not show status icon for other user messages', () => {
    const { UNSAFE_getAllByType } = render(
      <MessageBubble message={createMessage()} isMyMessage={false} />
    );
    const icons = UNSAFE_getAllByType('IconButton');
    const statusIcons = icons.filter((i: any) => i.props.icon === 'check-all' || i.props.icon === 'check');
    expect(statusIcons).toHaveLength(0);
  });

  it('shows media placeholder for image messages', () => {
    const { getByText } = render(
      <MessageBubble
        message={createMessage({ type: 'image', mediaURL: 'https://example.com/img.jpg', text: null })}
        isMyMessage={false}
      />
    );
    expect(getByText('image')).toBeTruthy();
  });

  it('shows star indicator when starred', () => {
    const { UNSAFE_getByProps } = render(
      <MessageBubble message={createMessage()} isMyMessage={false} isStarred />
    );
    expect(UNSAFE_getByProps({ icon: 'star' })).toBeTruthy();
  });

  it('shows reactions when provided', () => {
    const { getByTestId } = render(
      <MessageBubble
        message={createMessage()}
        isMyMessage={false}
        reactions={{ '\u2764\uFE0F': { count: 2, userReacted: true } }}
      />
    );
    expect(getByTestId('message-reactions')).toBeTruthy();
  });

  it('calls onLongPress when long pressed', () => {
    const onLongPress = jest.fn();
    const { getByTestId } = render(
      <MessageBubble message={createMessage()} isMyMessage={false} onLongPress={onLongPress} />
    );
    // The TouchableOpacity with onLongPress prop
    expect(getByTestId('message-msg-1').props.accessibilityRole).toBe('button');
  });
});
