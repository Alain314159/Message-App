import React, { useCallback, useMemo } from 'react';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { IconButton } from 'react-native-paper';
import { theme } from '@/config/theme';
import type { Message } from '@/types';
import type { ReplyContext } from '@/types/message.types';
import { ReplyThread } from './ReplyThread';
import { MessageReactions } from './MessageReactions';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';

interface MessageBubbleProps {
  message: Message;
  isMyMessage: boolean;
  reactions?: Record<string, { count: number; userReacted: boolean }>;
  replyContext?: ReplyContext | null;
  onLongPress?: () => void;
  onReactionPress?: (emoji: string) => void;
  onReplyPress?: () => void;
  isStarred?: boolean;
}

// Constants outside component — avoid recreation on every render
const TYPE_EMOJIS: Record<string, string> = {
  image: '\u{1F4F7}',
  video: '\u{1F3A5}',
  audio: '\u{1F3A4}',
  file: '\u{1F4C4}',
};

const STATUS_CONFIG = [
  { status: 'read', icon: 'check-all' as const, colorKey: 'tickRead' },
  { status: 'delivered', icon: 'check-all' as const, colorKey: 'tickDelivered' },
  { status: 'sent', icon: 'check' as const, colorKey: 'tickDelivered' },
  { status: 'failed', icon: 'alert-circle' as const, colorKey: 'error' },
];

function StatusIcon({ status }: { status: string }) {
  const config = STATUS_CONFIG.find((c) => c.status === status);
  if (!config) return null;
  return (
    <IconButton
      icon={config.icon}
      size={14}
      iconColor={theme.colors[config.colorKey as keyof typeof theme.colors]}
      style={styles.statusIcon}
    />
  );
}

export const MessageBubble = React.memo(function MessageBubble({
  message,
  isMyMessage,
  reactions,
  replyContext,
  onLongPress,
  onReactionPress,
  onReplyPress,
  isStarred,
}: MessageBubbleProps) {
  const messageText = useMemo(() => {
    if (message.type === 'text') {
      return (
        <Text style={[styles.text, isMyMessage ? styles.myText : styles.theirText]}>
          {message.text}
          {message.editedAt && <Text style={styles.editedLabel}> (editado)</Text>}
        </Text>
      );
    }
    if (message.mediaURL) {
      return (
        <Text style={[styles.text, isMyMessage ? styles.myText : styles.theirText]}>
          {TYPE_EMOJIS[message.type] || '\u{1F4CE}'} {message.type}
        </Text>
      );
    }
    return null;
  }, [message.type, message.text, message.mediaURL, message.editedAt, isMyMessage]);

  const handleLongPress = useCallback(() => {
    onLongPress?.();
  }, [onLongPress]);

  return (
    <View
      style={[styles.container, isMyMessage ? styles.myMessage : styles.theirMessage]}
      testID={`message-${message.id}`}
    >
      {replyContext && (
        <TouchableOpacity
          style={styles.replyThread}
          onPress={onReplyPress}
          activeOpacity={0.7}
        >
          <ReplyThread context={replyContext} isMyMessage={isMyMessage} />
        </TouchableOpacity>
      )}
      <TouchableOpacity
        style={[styles.bubble, isMyMessage ? styles.myBubble : styles.theirBubble]}
        onLongPress={handleLongPress}
        activeOpacity={0.8}
      >
        {messageText}
        {reactions && (
          <MessageReactions
            reactions={reactions}
            onReactionPress={onReactionPress}
          />
        )}
        <View style={styles.meta}>
          <Text style={styles.time}>
            {format(new Date(message.createdAt), 'HH:mm', { locale: es })}
          </Text>
          {isMyMessage && <StatusIcon status={message.status} />}
        </View>
      </TouchableOpacity>
      {isStarred && (
        <View style={styles.starIndicator}>
          <IconButton icon="star" size={16} iconColor="#FFD700" style={styles.starIcon} />
        </View>
      )}
    </View>
  );
});

const styles = StyleSheet.create({
  container: { marginVertical: 2, maxWidth: '85%' },
  myMessage: { alignSelf: 'flex-end' },
  theirMessage: { alignSelf: 'flex-start' },
  replyThread: { marginBottom: 4 },
  bubble: { paddingHorizontal: 14, paddingVertical: 10, borderRadius: 16 },
  myBubble: { backgroundColor: theme.colors.messageSent, borderBottomRightRadius: 4 },
  theirBubble: { backgroundColor: theme.colors.messageReceived, borderBottomLeftRadius: 4 },
  text: { fontSize: 15, lineHeight: 20 },
  myText: { color: theme.colors.messageSentText },
  theirText: { color: theme.colors.messageReceivedText },
  editedLabel: { fontSize: 11, fontStyle: 'italic', opacity: 0.6 },
  meta: { flexDirection: 'row', alignItems: 'center', marginTop: 4, justifyContent: 'flex-end' },
  time: { fontSize: 10, color: 'rgba(255,255,255,0.5)' },
  statusIcon: { margin: 0, padding: 0, width: 18, height: 18, marginLeft: 2 },
  starIndicator: { position: 'absolute', top: -8, right: -8 },
  starIcon: { margin: 0 },
});
