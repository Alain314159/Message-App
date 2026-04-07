import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { IconButton } from 'react-native-paper';
import { theme } from '@/config/theme';
import type { ReplyContext } from '@/types/message.types';

interface ReplyPreviewProps {
  context: ReplyContext;
  onClose: () => void;
}

export const ReplyPreview: React.FC<ReplyPreviewProps> = ({ context, onClose }) => (
  <View style={styles.container} testID="reply-preview">
    <View style={styles.indicator} />
    <View style={styles.content}>
      <Text style={styles.label}>
        Respondiendo a <Text style={styles.name}>{context.senderName}</Text>
      </Text>
      <Text style={styles.text} numberOfLines={1}>
        {context.type === 'text' ? context.text : `\u{1F4CE} ${context.type}`}
      </Text>
    </View>
    <IconButton icon="close" size={20} onPress={onClose} iconColor={theme.colors.textSecondary} />
  </View>
);

const styles = StyleSheet.create({
  container: { flexDirection: 'row', alignItems: 'center', paddingHorizontal: 12, paddingVertical: 8,
    backgroundColor: theme.colors.surface, borderTopWidth: 1, borderTopColor: theme.colors.border },
  indicator: { width: 3, height: 32, backgroundColor: theme.colors.primary, borderRadius: 2, marginRight: 8 },
  content: { flex: 1 },
  label: { fontSize: 12, color: theme.colors.textSecondary },
  name: { fontWeight: '600', color: theme.colors.primary },
  text: { fontSize: 13, color: theme.colors.textPrimary, marginTop: 2 },
});
