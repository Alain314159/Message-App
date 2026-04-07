import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { theme } from '@/config/theme';
import type { ReplyContext } from '@/types';

interface ReplyThreadProps {
  context: ReplyContext;
  isMyMessage: boolean;
  onPress?: () => void;
}

export const ReplyThread: React.FC<ReplyThreadProps> = ({ context, isMyMessage, onPress }) => (
  <TouchableOpacity
    style={[styles.container, isMyMessage ? styles.myContainer : styles.theirContainer]}
    onPress={onPress}
    activeOpacity={0.7}
  >
    <View style={styles.indicator} />
    <View style={styles.content}>
      <Text style={styles.senderName}>{context.senderName}</Text>
      <Text style={styles.text} numberOfLines={2}>
        {context.type === 'text' ? context.text : `\u{1F4CE} ${context.type}`}
      </Text>
    </View>
  </TouchableOpacity>
);

const styles = StyleSheet.create({
  container: { flexDirection: 'row', marginBottom: 4, borderRadius: 8, overflow: 'hidden' },
  myContainer: { backgroundColor: 'rgba(0,0,0,0.08)' },
  theirContainer: { backgroundColor: 'rgba(255,255,255,0.15)' },
  indicator: { width: 3, backgroundColor: theme.colors.primary },
  content: { flex: 1, paddingVertical: 4, paddingHorizontal: 8 },
  senderName: { fontSize: 12, fontWeight: '600', color: theme.colors.primary, marginBottom: 2 },
  text: { fontSize: 12, color: 'rgba(255,255,255,0.7)' },
});
