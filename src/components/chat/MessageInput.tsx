import React, { useState } from 'react';
import { View, StyleSheet, TouchableOpacity } from 'react-native';
import { TextInput, IconButton } from 'react-native-paper';
import { theme } from '@/config/theme';
import type { ReplyContext } from '@/types/message.types';

interface MessageInputProps {
  value: string;
  onChangeText: (text: string) => void;
  onSend: () => void;
  onAttachmentPress?: () => void;
  onCameraPress?: () => void;
  onVoicePress?: () => void;
  replyContext?: ReplyContext | null;
  onReplyClose?: () => void;
  disabled?: boolean;
  placeholder?: string;
}

export const MessageInput: React.FC<MessageInputProps> = ({
  value, onChangeText, onSend, onAttachmentPress, onCameraPress, onVoicePress,
  replyContext, onReplyClose, disabled = false, placeholder = 'Escribe un mensaje...',
}) => {
  const hasText = value.trim().length > 0;

  return (
    <View style={styles.container}>
      <View style={styles.inputRow}>
        <TouchableOpacity style={styles.attachButton} onPress={onAttachmentPress}>
          <IconButton icon="paperclip" size={22} iconColor={theme.colors.textSecondary} />
        </TouchableOpacity>
        <TextInput
          value={value} onChangeText={onChangeText} placeholder={placeholder}
          mode="outlined" multiline maxLength={5000} style={styles.input}
          contentStyle={styles.inputContent} disabled={disabled} testID="message-input"
        />
        {hasText ? (
          <TouchableOpacity style={styles.sendButton} onPress={onSend} disabled={!hasText || disabled}>
            <IconButton icon="send" size={22} iconColor={theme.colors.primary} />
          </TouchableOpacity>
        ) : (
          <View style={styles.alternateButtons}>
            <IconButton icon="camera" size={22} iconColor={theme.colors.textSecondary} onPress={onCameraPress} />
            <IconButton icon="microphone" size={22} iconColor={theme.colors.textSecondary} onPress={onVoicePress} />
          </View>
        )}
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: { padding: 8, backgroundColor: theme.colors.background, borderTopWidth: 1, borderTopColor: theme.colors.border },
  inputRow: { flexDirection: 'row', alignItems: 'flex-end', gap: 4 },
  attachButton: { justifyContent: 'center', alignItems: 'center' },
  input: { flex: 1, backgroundColor: theme.colors.surface, maxHeight: 120 },
  inputContent: { minHeight: 40 },
  sendButton: { justifyContent: 'center', alignItems: 'center' },
  alternateButtons: { flexDirection: 'row' },
});
