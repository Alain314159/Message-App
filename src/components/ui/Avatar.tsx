import React from 'react';
import { View, StyleSheet, Image } from 'react-native';
import { Avatar as PaperAvatar } from 'react-native-paper';
import { theme } from '@/config/theme';

// Importar avatares del sistema
const SYSTEM_AVATARS = [
  require('@/assets/images/cerdita-avatar.jpg'),
  require('@/assets/images/koala-avatar.jpg'),
];

export interface AvatarProps {
  uri?: string;
  systemAvatarId?: number;
  size?: number;
  isOnline?: boolean;
  displayName?: string;
  showStatus?: boolean;
}

// Obtener iniciales del displayName
function getInitials(name?: string): string {
  if (!name) return '?';
  const parts = name.trim().split(' ');
  if (parts.length === 1) return parts[0][0]?.toUpperCase() || '?';
  return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
}

export function Avatar({
  uri,
  systemAvatarId,
  size = 56,
  isOnline = false,
  displayName,
  showStatus = true,
}: AvatarProps) {
  // Si hay URI personalizada, usarla
  if (uri) {
    return (
      <View style={styles.container}>
        <PaperAvatar.Image size={size} source={{ uri }} />
        {showStatus && isOnline && (
          <View style={[styles.indicator, { width: size * 0.25, height: size * 0.25 }]} />
        )}
      </View>
    );
  }

  // Si hay un avatar del sistema, mostrar la imagen real
  if (systemAvatarId !== undefined && SYSTEM_AVATARS[systemAvatarId]) {
    return (
      <View style={styles.container}>
        <PaperAvatar.Image
          size={size}
          source={SYSTEM_AVATARS[systemAvatarId]}
        />
        {showStatus && isOnline && (
          <View style={[styles.indicator, { width: size * 0.25, height: size * 0.25 }]} />
        )}
      </View>
    );
  }

  // Fallback: mostrar iniciales o ícono por defecto
  return (
    <View style={styles.container}>
      <PaperAvatar.Text
        size={size}
        label={displayName ? getInitials(displayName) : 'U'}
        color={theme.colors.textInverse}
        style={{ backgroundColor: theme.colors.primary }}
      />
      {showStatus && isOnline && (
        <View style={[styles.indicator, { width: size * 0.25, height: size * 0.25 }]} />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    position: 'relative',
  },
  indicator: {
    position: 'absolute',
    bottom: 0,
    right: 0,
    borderRadius: 999,
    backgroundColor: theme.colors.online,
    borderWidth: 2,
    borderColor: theme.colors.background,
  },
});
