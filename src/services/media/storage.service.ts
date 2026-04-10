import { supabase } from '@/services/supabase/config';

const MESSAGES_BUCKET = 'messages';
const AVATARS_BUCKET = 'avatars';

export interface MediaFile {
  uri: string;
  name: string;
  type: string;
}

export interface UploadResult {
  path: string;
  url: string;
}

export const storageService = {
  async uploadMessageMedia(userId: string, chatId: string, file: MediaFile): Promise<UploadResult> {
    const path = `${userId}/${chatId}/${Date.now()}_${file.name}`;
    const response = await fetch(file.uri);
    const blob = await response.blob();
    const { data, error } = await supabase.storage
      .from(MESSAGES_BUCKET)
      .upload(path, blob, { contentType: file.type, cacheControl: '3600', upsert: false });
    if (error) throw new Error(`Upload failed: ${error.message}`);
    const { data: publicUrl } = supabase.storage.from(MESSAGES_BUCKET).getPublicUrl(path);
    return { path: data.path, url: publicUrl.publicUrl };
  },

  async uploadAvatar(userId: string, uri: string): Promise<UploadResult> {
    const path = `${userId}/avatar_${Date.now()}`;
    const response = await fetch(uri);
    const blob = await response.blob();
    const { data, error } = await supabase.storage
      .from(AVATARS_BUCKET)
      .upload(path, blob, { cacheControl: '3600', upsert: true });
    if (error) throw new Error(`Avatar upload failed: ${error.message}`);
    const { data: publicUrl } = supabase.storage.from(AVATARS_BUCKET).getPublicUrl(path);
    return { path: data.path, url: publicUrl.publicUrl };
  },

  async deleteMedia(path: string) {
    const { error } = await supabase.storage.from(MESSAGES_BUCKET).remove([path]);
    if (error) throw new Error(error.message);
  },

  getPublicUrl(bucket: string, path: string) {
    const { data } = supabase.storage.from(bucket).getPublicUrl(path);
    return data.publicUrl;
  },

  async downloadMedia(path: string, bucket = MESSAGES_BUCKET) {
    const { data, error } = await supabase.storage.from(bucket).download(path);
    if (error) throw new Error(error.message);
    return data;
  },
};
