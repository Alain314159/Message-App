import * as FileSystem from 'expo-file-system';
import AsyncStorage from '@react-native-async-storage/async-storage';

const CACHE_DIR = `${FileSystem.documentDirectory}media_cache/`;
const CACHE_META_KEY = 'media_cache_meta';
const MAX_CACHE_SIZE_MB = 500;

interface CacheEntry {
  localUri: string;
  downloadedAt: number;
  size: number;
}

interface CacheMeta {
  [url: string]: CacheEntry;
}

export const mediaCacheService = {
  async init() {
    const dirInfo = await FileSystem.getInfoAsync(CACHE_DIR);
    if (!dirInfo.exists) {
      await FileSystem.makeDirectoryAsync(CACHE_DIR, { intermediates: true });
    }
  },

  async getCachedUri(url: string): Promise<string | null> {
    const metaStr = await AsyncStorage.getItem(CACHE_META_KEY);
    if (!metaStr) return null;
    const meta: CacheMeta = JSON.parse(metaStr);
    return meta[url]?.localUri || null;
  },

  async cacheUri(url: string): Promise<string> {
    const cached = await this.getCachedUri(url);
    if (cached) return cached;

    await this.init();
    const fileName = url.split('/').pop() || `file_${Date.now()}`;
    const localUri = `${CACHE_DIR}${fileName}`;
    const { uri } = await FileSystem.downloadAsync(url, localUri);
    const info = await FileSystem.getInfoAsync(uri);

    const metaStr = await AsyncStorage.getItem(CACHE_META_KEY);
    const meta: CacheMeta = metaStr ? JSON.parse(metaStr) : {};
    meta[url] = { localUri: uri, downloadedAt: Date.now(), size: (info as any).size || 0 };
    await AsyncStorage.setItem(CACHE_META_KEY, JSON.stringify(meta));

    await this.cleanIfNeeded();
    return uri;
  },

  async cleanIfNeeded() {
    const metaStr = await AsyncStorage.getItem(CACHE_META_KEY);
    if (!metaStr) return;

    const meta: CacheMeta = JSON.parse(metaStr);
    const entries = Object.entries(meta).sort((a, b) => a[1].downloadedAt - b[1].downloadedAt);
    let totalSize = entries.reduce((sum, [, m]) => sum + m.size, 0);
    const maxSizeBytes = MAX_CACHE_SIZE_MB * 1024 * 1024;

    while (totalSize > maxSizeBytes && entries.length > 0) {
      const [url, m] = entries.shift()!;
      try {
        await FileSystem.deleteAsync(m.localUri, { idempotent: true });
      } catch {}
      totalSize -= m.size;
      delete meta[url];
    }
    await AsyncStorage.setItem(CACHE_META_KEY, JSON.stringify(meta));
  },

  async clearCache() {
    await FileSystem.deleteAsync(CACHE_DIR, { idempotent: true });
    await FileSystem.makeDirectoryAsync(CACHE_DIR, { intermediates: true });
    await AsyncStorage.removeItem(CACHE_META_KEY);
  },

  async getCacheSize(): Promise<number> {
    const metaStr = await AsyncStorage.getItem(CACHE_META_KEY);
    if (!metaStr) return 0;
    const meta: CacheMeta = JSON.parse(metaStr);
    return Object.values(meta).reduce((sum, m) => sum + m.size, 0);
  },
};
