import React from 'react';
import { View, StyleSheet, Modal, TouchableOpacity, Text } from 'react-native';
import { IconButton } from 'react-native-paper';
import { theme } from '@/config/theme';

interface ChatOptionsMenuProps {
  visible: boolean;
  onClose: () => void;
  onSearch?: () => void;
  onStarred?: () => void;
  onMute?: () => void;
  onClearChat?: () => void;
}

export const ChatOptionsMenu: React.FC<ChatOptionsMenuProps> = ({
  visible, onClose, onSearch, onStarred, onMute, onClearChat,
}) => (
  <Modal visible={visible} transparent animationType="fade" onRequestClose={onClose}>
    <TouchableOpacity style={styles.overlay} onPress={onClose} activeOpacity={1}>
      <View style={styles.menu}>
        {onSearch && (
          <TouchableOpacity style={styles.option} onPress={onSearch}>
            <IconButton icon="magnify" size={20} iconColor={theme.colors.textPrimary} />
            <Text style={styles.optionText}>Buscar mensajes</Text>
          </TouchableOpacity>
        )}
        {onStarred && (
          <TouchableOpacity style={styles.option} onPress={onStarred}>
            <IconButton icon="star" size={20} iconColor={theme.colors.textPrimary} />
            <Text style={styles.optionText}>Mensajes favoritos</Text>
          </TouchableOpacity>
        )}
        {onMute && (
          <TouchableOpacity style={styles.option} onPress={onMute}>
            <IconButton icon="volume-off" size={20} iconColor={theme.colors.textPrimary} />
            <Text style={styles.optionText}>Silenciar</Text>
          </TouchableOpacity>
        )}
        {onClearChat && (
          <TouchableOpacity style={styles.option} onPress={onClearChat}>
            <IconButton icon="delete-sweep" size={20} iconColor={theme.colors.error} />
            <Text style={[styles.optionText, styles.danger]}>Limpiar chat</Text>
          </TouchableOpacity>
        )}
      </View>
    </TouchableOpacity>
  </Modal>
);

const styles = StyleSheet.create({
  overlay: { flex: 1, backgroundColor: 'rgba(0,0,0,0.4)', justifyContent: 'flex-end' },
  menu: { backgroundColor: theme.colors.surface, borderTopLeftRadius: 16, borderTopRightRadius: 16, paddingBottom: 20 },
  option: { flexDirection: 'row', alignItems: 'center', paddingVertical: 12, paddingHorizontal: 16 },
  optionText: { fontSize: 16, color: theme.colors.textPrimary, marginLeft: 8 },
  danger: { color: theme.colors.error },
});
