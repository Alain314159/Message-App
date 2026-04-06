import React, { useState, useCallback } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  Modal,
  Image,
  ScrollView,
  Alert,
} from 'react-native';
import { IconButton, ActivityIndicator } from 'react-native-paper';
import * as ImagePicker from 'expo-image-picker';
import { theme } from '@/config/theme';
import { haptics } from '@/services/haptics';

// Avatares predefinidos del sistema
const SYSTEM_AVATARS = [
  {
    id: 0,
    image: require('@/assets/images/cerdita-avatar.jpg'),
    name: 'Cerdita',
  },
  {
    id: 1,
    image: require('@/assets/images/koala-avatar.jpg'),
    name: 'Koala',
  },
];

export interface AvatarOption {
  type: 'system' | 'custom';
  uri?: string;
  systemId?: number;
  imageName?: string;
}

interface AvatarSelectorProps {
  currentAvatar?: AvatarOption;
  onAvatarSelect: (avatar: AvatarOption) => Promise<void>;
  visible: boolean;
  onClose: () => void;
}

export function AvatarSelector({
  currentAvatar,
  onAvatarSelect,
  visible,
  onClose,
}: AvatarSelectorProps) {
  const [uploading, setUploading] = useState(false);

  const handleSystemAvatarSelect = useCallback(
    async (avatar: typeof SYSTEM_AVATARS[number]) => {
      await haptics.medium();
      const selectedAvatar: AvatarOption = {
        type: 'system',
        systemId: avatar.id,
        imageName: avatar.name,
      };
      await onAvatarSelect(selectedAvatar);
    },
    [onAvatarSelect]
  );

  const handlePickImage = useCallback(async () => {
    try {
      // Request permission
      const { status } = await ImagePicker.requestMediaLibraryPermissionsAsync();
      if (status !== 'granted') {
        Alert.alert(
          'Permiso requerido',
          'Necesitas dar permiso para acceder a tus fotos.'
        );
        return;
      }

      // Launch image picker
      const result = await ImagePicker.launchImageLibraryAsync({
        mediaTypes: ImagePicker.MediaTypeOptions.Images,
        allowsEditing: true,
        aspect: [1, 1],
        quality: 0.8,
      });

      if (!result.canceled && result.assets[0]) {
        setUploading(true);
        await haptics.medium();

        const avatar: AvatarOption = {
          type: 'custom',
          uri: result.assets[0].uri,
        };

        await onAvatarSelect(avatar);
      }
    } catch (error) {
      console.error('Failed to pick image:', error);
      Alert.alert('Error', 'No se pudo seleccionar la imagen');
    } finally {
      setUploading(false);
    }
  }, [onAvatarSelect]);

  const handleTakePhoto = useCallback(async () => {
    try {
      // Request permission
      const { status } = await ImagePicker.requestCameraPermissionsAsync();
      if (status !== 'granted') {
        Alert.alert(
          'Permiso requerido',
          'Necesitas dar permiso para usar la cámara.'
        );
        return;
      }

      // Launch camera
      const result = await ImagePicker.launchCameraAsync({
        allowsEditing: true,
        aspect: [1, 1],
        quality: 0.8,
      });

      if (!result.canceled && result.assets[0]) {
        setUploading(true);
        await haptics.success();

        const avatar: AvatarOption = {
          type: 'custom',
          uri: result.assets[0].uri,
        };

        await onAvatarSelect(avatar);
      }
    } catch (error) {
      console.error('Failed to take photo:', error);
      Alert.alert('Error', 'No se pudo tomar la foto');
    } finally {
      setUploading(false);
    }
  }, [onAvatarSelect]);

  const isCurrentSystem = (id: number) =>
    currentAvatar?.type === 'system' && currentAvatar.systemId === id;

  const isCurrentCustom = () => currentAvatar?.type === 'custom';

  return (
    <Modal
      visible={visible}
      animationType="slide"
      transparent={false}
      onRequestClose={onClose}
    >
      <View style={styles.container}>
        {/* Header */}
        <View style={styles.header}>
          <IconButton
            icon="close"
            size={24}
            onPress={onClose}
            accessibilityLabel="Cerrar"
            testID="close-avatar-selector"
          />
          <Text style={styles.title}>Elige tu Avatar</Text>
          <View style={{ width: 40 }} />
        </View>

        <ScrollView contentContainerStyle={styles.content}>
          {/* System Avatars Section */}
          <View style={styles.section}>
            <Text style={styles.sectionTitle}>Avatares del Sistema</Text>
            <View style={styles.avatarGrid}>
              {SYSTEM_AVATARS.map((avatar) => {
                const isSelected = isCurrentSystem(avatar.id);
                return (
                  <TouchableOpacity
                    key={avatar.id}
                    style={[
                      styles.avatarOption,
                      isSelected && styles.avatarOptionSelected,
                    ]}
                    onPress={() => handleSystemAvatarSelect(avatar)}
                    accessibilityLabel={`Avatar ${avatar.name}`}
                    testID={`system-avatar-${avatar.id}`}
                  >
                    <Image source={avatar.image} style={styles.avatarImage} />
                    {isSelected && (
                      <View style={styles.selectedBadge}>
                        <IconButton
                          icon="check-circle"
                          size={20}
                          iconColor={theme.colors.textInverse}
                          style={styles.badgeIcon}
                        />
                      </View>
                    )}
                    <View style={styles.avatarNameContainer}>
                      <Text style={styles.avatarNameText}>{avatar.name}</Text>
                    </View>
                  </TouchableOpacity>
                );
              })}
            </View>
          </View>

          {/* Custom Photo Section */}
          <View style={styles.section}>
            <Text style={styles.sectionTitle}>Tu Propia Foto</Text>
            <View style={styles.customAvatarContainer}>
              {uploading ? (
                <View style={styles.uploadingContainer}>
                  <ActivityIndicator size="large" color={theme.colors.primary} />
                  <Text style={styles.uploadingText}>Subiendo...</Text>
                </View>
              ) : (
                <>
                  <TouchableOpacity
                    style={[
                      styles.avatarOption,
                      styles.customAvatarOption,
                      isCurrentCustom() && styles.avatarOptionSelected,
                    ]}
                    onPress={handlePickImage}
                    accessibilityLabel="Seleccionar foto de galería"
                    testID="pick-from-gallery"
                  >
                    {isCurrentCustom() && currentAvatar?.uri ? (
                      <>
                        <Image
                          source={{ uri: currentAvatar.uri }}
                          style={styles.avatarImage}
                        />
                        <View style={styles.selectedBadge}>
                          <IconButton
                            icon="check-circle"
                            size={20}
                            iconColor={theme.colors.textInverse}
                            style={styles.badgeIcon}
                          />
                        </View>
                      </>
                    ) : (
                      <>
                        <IconButton
                          icon="image-plus"
                          size={48}
                          iconColor={theme.colors.primary}
                        />
                        <Text style={styles.customAvatarText}>
                          Elegir de Galería
                        </Text>
                      </>
                    )}
                  </TouchableOpacity>

                  <TouchableOpacity
                    style={[
                      styles.avatarOption,
                      styles.customAvatarOption,
                    ]}
                    onPress={handleTakePhoto}
                    accessibilityLabel="Tomar foto con cámara"
                    testID="take-photo"
                  >
                    <IconButton
                      icon="camera"
                      size={48}
                      iconColor={theme.colors.primary}
                    />
                    <Text style={styles.customAvatarText}>Tomar Foto</Text>
                  </TouchableOpacity>
                </>
              )}
            </View>
          </View>
        </ScrollView>
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: theme.colors.background,
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: theme.spacing.md,
    borderBottomWidth: 1,
    borderBottomColor: theme.colors.border,
  },
  title: {
    fontSize: 20,
    fontWeight: 'bold',
    color: theme.colors.textPrimary,
  },
  content: {
    padding: theme.spacing.lg,
  },
  section: {
    marginBottom: theme.spacing.xl,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: theme.colors.textPrimary,
    marginBottom: theme.spacing.md,
  },
  avatarGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: theme.spacing.md,
    justifyContent: 'center',
  },
  avatarOption: {
    width: 120,
    height: 120,
    borderRadius: theme.borderRadius.lg,
    overflow: 'hidden',
    backgroundColor: theme.colors.backgroundSecondary,
    justifyContent: 'center',
    alignItems: 'center',
    borderWidth: 3,
    borderColor: 'transparent',
  },
  avatarOptionSelected: {
    borderColor: theme.colors.primary,
  },
  avatarImage: {
    width: '100%',
    height: '100%',
    resizeMode: 'cover',
  },
  systemAvatarPlaceholder: {
    width: '100%',
    height: '100%',
    justifyContent: 'center',
    alignItems: 'center',
  },
  systemAvatarInitial: {
    fontSize: 48,
  },
  selectedBadge: {
    position: 'absolute',
    bottom: 4,
    right: 4,
    backgroundColor: theme.colors.primary,
    borderRadius: 12,
  },
  badgeIcon: {
    margin: 0,
    padding: 0,
  },
  avatarNameContainer: {
    position: 'absolute',
    bottom: 0,
    left: 0,
    right: 0,
    backgroundColor: 'rgba(0, 0, 0, 0.6)',
    paddingVertical: 4,
    paddingHorizontal: 8,
  },
  avatarNameText: {
    color: theme.colors.textInverse,
    fontSize: 12,
    fontWeight: '600',
    textAlign: 'center',
  },
  customAvatarContainer: {
    gap: theme.spacing.md,
  },
  customAvatarOption: {
    width: '100%',
    height: 140,
    flexDirection: 'column',
  },
  uploadingContainer: {
    justifyContent: 'center',
    alignItems: 'center',
    padding: theme.spacing.xl,
  },
  uploadingText: {
    marginTop: theme.spacing.sm,
    fontSize: 16,
    color: theme.colors.textSecondary,
  },
  customAvatarText: {
    fontSize: 16,
    fontWeight: '600',
    color: theme.colors.primary,
  },
});
