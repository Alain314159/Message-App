import React from 'react';
import { render, fireEvent } from '@testing-library/react-native';
import { MessageInput } from '@/components/chat/MessageInput';

jest.mock('react-native-paper', () => {
  const RealModule = jest.requireActual('react-native-paper');
  return {
    ...RealModule,
    TextInput: ({ value, onChangeText, placeholder, testID, ...props }: any) => {
      const { View, TextInput: RNTextInput } = require('react-native');
      return React.createElement(View, props, [
        React.createElement(RNTextInput, {
          key: 'input',
          value,
          onChangeText,
          placeholder,
          testID,
        }),
      ]);
    },
    IconButton: 'IconButton',
  };
});

describe('MessageInput', () => {
  it('renders text input with placeholder', () => {
    const { getByPlaceholderText } = render(
      <MessageInput value="" onChangeText={() => {}} onSend={() => {}} />
    );
    expect(getByPlaceholderText('Escribe un mensaje...')).toBeTruthy();
  });

  it('shows send button when text is entered', () => {
    const { UNSAFE_getByProps } = render(
      <MessageInput value="Hello" onChangeText={() => {}} onSend={() => {}} />
    );
    expect(UNSAFE_getByProps({ icon: 'send' })).toBeTruthy();
  });

  it('shows camera and mic buttons when input is empty', () => {
    const { UNSAFE_getByProps } = render(
      <MessageInput value="" onChangeText={() => {}} onSend={() => {}} />
    );
    expect(UNSAFE_getByProps({ icon: 'camera' })).toBeTruthy();
    expect(UNSAFE_getByProps({ icon: 'microphone' })).toBeTruthy();
  });

  it('calls onSend when send button is pressed', () => {
    const onSend = jest.fn();
    const { UNSAFE_getByProps } = render(
      <MessageInput value="Hello" onChangeText={() => {}} onSend={onSend} />
    );
    fireEvent.press(UNSAFE_getByProps({ icon: 'send' }));
    expect(onSend).toHaveBeenCalled();
  });

  it('calls onAttachmentPress when attachment button is pressed', () => {
    const onAttachmentPress = jest.fn();
    const { UNSAFE_getByProps } = render(
      <MessageInput value="" onChangeText={() => {}} onSend={() => {}} onAttachmentPress={onAttachmentPress} />
    );
    fireEvent.press(UNSAFE_getByProps({ icon: 'paperclip' }));
    expect(onAttachmentPress).toHaveBeenCalled();
  });

  it('disables send button when disabled prop is true', () => {
    const { UNSAFE_getByProps } = render(
      <MessageInput value="Hello" onChangeText={() => {}} onSend={() => {}} disabled />
    );
    const sendButton = UNSAFE_getByProps({ icon: 'send' });
    expect(sendButton.props.disabled).toBe(true);
  });

  it('supports custom placeholder', () => {
    const { getByPlaceholderText } = render(
      <MessageInput value="" onChangeText={() => {}} onSend={() => {}} placeholder="Type here..." />
    );
    expect(getByPlaceholderText('Type here...')).toBeTruthy();
  });
});
