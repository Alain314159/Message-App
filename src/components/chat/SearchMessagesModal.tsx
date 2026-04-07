import React from 'react';
import { View, Text, StyleSheet, Modal, FlatList, TouchableOpacity } from 'react-native';
import { Searchbar, IconButton, ActivityIndicator } from 'react-native-paper';
import { theme } from '@/config/theme';
import { useMessageSearch } from '@/hooks/useMessageSearch';
import type { Message } from '@/types';

interface SearchMessagesModalProps {
  visible: boolean;
  chatId: string;
  onClose: () => void;
  onMessageSelect?: (message: Message) => void;
}

export const SearchMessagesModal: React.FC<SearchMessagesModalProps> = ({
  visible,
  chatId,
  onClose,
  onMessageSelect,
}) => {
  const { query, results, searching, search, clear } = useMessageSearch(chatId);

  return (
    <Modal visible={visible} animationType="slide" onRequestClose={onClose}>
      <View style={styles.container}>
        <View style={styles.header}>
          <IconButton icon="arrow-left" size={24} onPress={onClose} />
          <Text style={styles.title}>Buscar mensajes</Text>
          <View style={{ width: 40 }} />
        </View>
        <Searchbar
          placeholder="Buscar..."
          onChangeText={search}
          value={query}
          style={styles.searchBar}
          iconColor={theme.colors.primary}
          clearIcon="close"
          onClearIconPress={clear}
        />
        {searching ? (
          <View style={styles.loadingContainer}>
            <ActivityIndicator size="large" color={theme.colors.primary} />
          </View>
        ) : (
          <FlatList
            data={results}
            keyExtractor={(item) => item.id}
            renderItem={({ item }) => (
              <TouchableOpacity
                style={styles.resultItem}
                onPress={() => onMessageSelect?.(item)}
              >
                <Text style={styles.resultText} numberOfLines={2}>
                  {(item as any).text || `\u{1F4CE} ${(item as any).type}`}
                </Text>
                <Text style={styles.resultDate}>
                  {new Date((item as any).createdAt).toLocaleDateString()}
                </Text>
              </TouchableOpacity>
            )}
            ListEmptyComponent={
              query.length > 0 ? (
                <View style={styles.emptyContainer}>
                  <Text style={styles.emptyText}>No se encontraron resultados</Text>
                </View>
              ) : null
            }
          />
        )}
      </View>
    </Modal>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: theme.colors.background },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    padding: theme.spacing.sm,
  },
  title: {
    flex: 1,
    fontSize: 18,
    fontWeight: '600',
    color: theme.colors.textPrimary,
  },
  searchBar: {
    marginHorizontal: 12,
    marginVertical: 8,
    borderRadius: 8,
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  resultItem: {
    padding: 16,
    borderBottomWidth: 1,
    borderBottomColor: theme.colors.border,
  },
  resultText: {
    fontSize: 15,
    color: theme.colors.textPrimary,
    marginBottom: 4,
  },
  resultDate: {
    fontSize: 12,
    color: theme.colors.textSecondary,
  },
  emptyContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    paddingVertical: 48,
  },
  emptyText: {
    fontSize: 16,
    color: theme.colors.textSecondary,
  },
});
