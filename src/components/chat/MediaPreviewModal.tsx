import React, { useState } from 'react';
import { Modal, View, StyleSheet, Image, Dimensions, TouchableOpacity, Text } from 'react-native';
import { IconButton, ActivityIndicator } from 'react-native-paper';
import { theme } from '@/config/theme';

interface MediaPreviewModalProps {
  visible: boolean;
  mediaUri: string | null;
  mediaType?: 'image' | 'video';
  onClose: () => void;
  onSend?: () => void;
}

export const MediaPreviewModal: React.FC<MediaPreviewModalProps> = ({
  visible,
  mediaUri,
  mediaType = 'image',
  onClose,
  onSend,
}) => {
  const [loading, setLoading] = useState(true);
  if (!mediaUri) return null;

  return (
    <Modal visible={visible} transparent animationType="fade">
      <View style={styles.overlay}>
        <View style={styles.header}>
          <IconButton icon="close" size={24} iconColor="#fff" onPress={onClose} />
          <Text style={styles.title}>Vista previa</Text>
          {onSend && <IconButton icon="send" size={24} iconColor={theme.colors.primary} onPress={onSend} />}
        </View>
        <View style={styles.mediaContainer}>
          {loading && (
            <View style={styles.loadingOverlay}>
              <ActivityIndicator size="large" color="#fff" />
            </View>
          )}
          <Image
            source={{ uri: mediaUri }}
            style={styles.image}
            resizeMode="contain"
            onLoadEnd={() => setLoading(false)}
          />
        </View>
      </View>
    </Modal>
  );
};

const { width, height } = Dimensions.get('window');
const styles = StyleSheet.create({
  overlay: { flex: 1, backgroundColor: '#000' },
  header: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', paddingTop: 48, paddingHorizontal: 16, paddingBottom: 12 },
  title: { fontSize: 18, fontWeight: '600', color: '#fff' },
  mediaContainer: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  image: { width, height: height * 0.7 },
  loadingOverlay: { ...StyleSheet.absoluteFillObject, justifyContent: 'center', alignItems: 'center', backgroundColor: 'rgba(0,0,0,0.5)', zIndex: 1 },
});
