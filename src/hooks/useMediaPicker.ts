import { useCallback } from 'react';
import * as ImagePicker from 'expo-image-picker';
import { compressionService } from '@/services/media/imageCompressor.service';

export interface PickedMedia {
  uri: string;
  type: string;
  fileName: string;
  fileType: 'image' | 'video';
}

export function useMediaPicker() {
  const requestPermissions = useCallback(async () => {
    const { status } = await ImagePicker.requestMediaLibraryPermissionsAsync();
    if (status !== 'granted') throw new Error('Media library permission not granted');
  }, []);

  const pickImage = useCallback(async (): Promise<PickedMedia | null> => {
    await requestPermissions();
    const result = await ImagePicker.launchImageLibraryAsync({
      mediaTypes: ImagePicker.MediaTypeOptions.Images,
      allowsEditing: true,
      aspect: [4, 3],
      quality: 0.8,
    });
    if (result.canceled) return null;
    const asset = result.assets[0];
    const compressedUri = await compressionService.compressImage(asset.uri);
    return {
      uri: compressedUri,
      type: asset.mimeType || 'image/jpeg',
      fileName: asset.uri.split('/').pop() || 'image.jpg',
      fileType: 'image',
    };
  }, [requestPermissions]);

  const pickVideo = useCallback(async (): Promise<PickedMedia | null> => {
    await requestPermissions();
    const result = await ImagePicker.launchImageLibraryAsync({
      mediaTypes: ImagePicker.MediaTypeOptions.Videos,
      allowsEditing: false,
    });
    if (result.canceled) return null;
    const asset = result.assets[0];
    return {
      uri: asset.uri,
      type: asset.mimeType || 'video/mp4',
      fileName: asset.uri.split('/').pop() || 'video.mp4',
      fileType: 'video',
    };
  }, [requestPermissions]);

  const takePhoto = useCallback(async (): Promise<PickedMedia | null> => {
    const { status } = await ImagePicker.requestCameraPermissionsAsync();
    if (status !== 'granted') throw new Error('Camera permission not granted');
    const result = await ImagePicker.launchCameraAsync({
      allowsEditing: true,
      aspect: [4, 3],
      quality: 0.8,
    });
    if (result.canceled) return null;
    const asset = result.assets[0];
    const compressedUri = await compressionService.compressImage(asset.uri);
    return {
      uri: compressedUri,
      type: asset.mimeType || 'image/jpeg',
      fileName: asset.uri.split('/').pop() || 'photo.jpg',
      fileType: 'image',
    };
  }, []);

  return { pickImage, pickVideo, takePhoto };
}
