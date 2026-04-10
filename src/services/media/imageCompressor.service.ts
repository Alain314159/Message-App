import { manipulateAsync, SaveFormat } from 'expo-image-manipulator';

export const compressionService = {
  async compressImage(uri: string, maxWidth = 1920, quality = 0.8) {
    const result = await manipulateAsync(uri, [{ resize: { width: maxWidth } }], {
      compress: quality,
      format: SaveFormat.JPEG,
    });
    return result.uri;
  },

  async generateThumbnail(uri: string, maxWidth = 480, quality = 0.7) {
    const result = await manipulateAsync(uri, [{ resize: { width: maxWidth } }], {
      compress: quality,
      format: SaveFormat.JPEG,
    });
    return result.uri;
  },
};
