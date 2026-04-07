import React from 'react';
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

export const MessageBubble: React.FC<MessageBubbleProps> = ({
  message, isMyMessage, reactions, replyContext, onLongPress, onReactionPress, onReplyPress, isStarred,
}) => {
  const getStatusIcon = () => {
    if (!isMyMessage) return null;
    if (message.status === 'read') {
      return <IconButton icon="check-all" size={14} iconColor={theme.colors.tickRead} style={styles.statusIcon} />;
    }
    if (message.status === 'delivered') {
      return <IconButton icon="check-all" size={14} iconColor={theme.colors.tickDelivered} style={styles.statusIcon} />;
    }
    if (message.status === 'sent') {
      return <IconButton icon="check" size={14} iconColor={theme.colors.tickDelivered} style={styles.statusIcon} />;
    }
    if (message.status === 'failed') {
      return <IconButton icon="alert-circle" size={14} iconColor={theme.colors.error} style={styles.statusIcon} />;
    }
    return null;
  };

  const typeIcons: Record<string, string> = { image: '\u{1F4F7}', video: '\u{1F3A5}', audio: '\u{1F3A4}', file: '\u{1F4C4}' };

  return (
    <View style={[styles.container, isMyMessage ? styles.myMessage : styles.theirMessage]} testID={`message-${message.id}`}>
      {replyContext && (
        <TouchableOpacity style={styles.replyThread} onPress={onReplyPress} activeOpacity={0.7}>
          <ReplyThread context={replyContext} isMyMessage={isMyMessage} />
        </TouchableOpacity>
      )}
      <TouchableOpacity
        style={[styles.bubble, isMyMessage ? styles.myBubble : styles.theirBubble]}
        onLongPress={onLongPress}
        activeOpacity={0.8}
      >
        {message.type === 'text' && (
          <Text style={[styles.text, isMyMessage ? styles.myText : styles.theirText]}>
            {message.text}
            {message.editedAt && <Text style={styles.editedLabel}> (editado)</Text>}
          </Text>
        )}
        {message.type !== 'text' && message.mediaURL && (
          <Text style={[styles.text, isMyMessage ? styles.myText : styles.theirText]}>
            {typeIcons[message.type] || '\u{1F4CE}'} {message.type}
          </Text>
        )}
        {reactions && <MessageReactions reactions={reactions} onReactionPress={onReactionPress} />}
        <View style={styles.meta}>
          <Text style={styles.time}>{format(new Date(message.createdAt), 'HH:mm', { locale: es })}</Text>
          {getStatusIcon()}
        </View>
      </TouchableOpacity>
      {isStarred && (
        <View style={styles.starIndicator}>
          <IconButton icon="star" size={16} iconColor="#FFD700" style={styles.starIcon} />
        </View>
      )}
    </View>
  );
};

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
