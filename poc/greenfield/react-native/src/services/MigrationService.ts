import { NativeModules, Platform, Settings } from 'react-native';
import { StorageService } from './StorageService';

export const MIGRATION_DONE_KEY = 'migration_done';
const KEYS = ['username'];

async function readNativeValue(key: string): Promise<string | null> {
  if (Platform.OS === 'ios') {
    return Settings.get(key) ?? null;
  }
  return NativeModules.NativeStorage.getItem(key);
}

export async function runMigration(): Promise<void> {
  if (StorageService.contains(MIGRATION_DONE_KEY)) return;

  for (const key of KEYS) {
    const value = await readNativeValue(key);
    if (value != null) {
      StorageService.set(key, value);
    }
  }

  StorageService.set(MIGRATION_DONE_KEY, 'true');
}
