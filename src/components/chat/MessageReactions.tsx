import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { theme } from '@/config/theme';

interface MessageReactionsProps {
  reactions: Record<string, { count: number; userReacted: boolean }>;
  onReactionPress?: (emoji: string) => void;
}

export const MessageReactions: React.FC<MessageReactionsProps> = ({ reactions, onReactionPress }) => {
  const entries = Object.entries(reactions).filter(([, data]) => data.count > 0);
  if (entries.length === 0) return null;

  return (
    <View style={styles.container} testID="message-reactions">
      {entries.map(([emoji, data]) => (
        <TouchableOpacity
          key={emoji}
          style={[styles.reaction, data.userReacted && styles.reacted]}
          onPress={() => onReactionPress?.(emoji)}
          activeOpacity={0.7}
        >
          <Text style={styles.emoji}>{emoji}</Text>
          <Text style={[styles.count, data.userReacted && styles.countReacted]}>{data.count}</Text>
        </TouchableOpacity>
      ))}
    </View>
  );
};

const styles = StyleSheet.create({
  container: { flexDirection: 'row', flexWrap: 'wrap', gap: 4, marginTop: 4 },
  reaction: {
    flexDirection: 'row', alignItems: 'center', paddingHorizontal: 6, paddingVertical: 2,
    borderRadius: 12, backgroundColor: 'rgba(255,255,255,0.1)', borderWidth: 1, borderColor: 'transparent',
  },
  reacted: { borderColor: theme.colors.primary, backgroundColor: 'rgba(255,105,180,0.15)' },
  emoji: { fontSize: 14, marginRight: 4 },
  count: { fontSize: 11, color: 'rgba(255,255,255,0.6)' },
  countReacted: { color: theme.colors.primary, fontWeight: '600' },
});
