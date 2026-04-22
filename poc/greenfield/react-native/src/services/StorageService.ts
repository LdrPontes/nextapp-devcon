import { createMMKV } from 'react-native-mmkv';

const storage = createMMKV();

export const StorageService = {
  set: (key: string, value: string) => storage.set(key, value),
  get: (key: string) => storage.getString(key),
  contains: (key: string) => storage.contains(key),
};
