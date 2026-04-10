import React from 'react';
import { render, fireEvent } from '@testing-library/react-native';
import { ReplyPreview } from '@/components/chat/ReplyPreview';

jest.mock('react-native-paper', () => {
  const RealModule = jest.requireActual('react-native-paper');
  return { ...RealModule, IconButton: 'IconButton' };
});

describe('ReplyPreview', () => {
  const defaultProps = {
    context: { messageId: 'msg-1', senderName: 'John', text: 'Hello there', type: 'text' as const },
    onClose: jest.fn(),
  };

  it('renders reply context correctly', () => {
    const { getByText } = render(<ReplyPreview {...defaultProps} />);
    expect(getByText('Respondiendo a')).toBeTruthy();
    expect(getByText('John')).toBeTruthy();
    expect(getByText('Hello there')).toBeTruthy();
  });

  it('calls onClose when close button is pressed', () => {
    const onClose = jest.fn();
    const { UNSAFE_getByProps } = render(<ReplyPreview {...defaultProps} onClose={onClose} />);
    fireEvent.press(UNSAFE_getByProps({ icon: 'close' }));
    expect(onClose).toHaveBeenCalled();
  });

  it('shows media type for non-text messages', () => {
    const { getByText } = render(
      <ReplyPreview context={{ messageId: 'm1', senderName: 'Jane', text: '', type: 'image' }} onClose={jest.fn()} />
    );
    expect(getByText('\u{1F4CE} image')).toBeTruthy();
  });
});
