import { notifyMigrationDone, requestNativeValue } from './NativeBridge';
import { StorageService } from './StorageService';

export const MIGRATION_DONE_KEY = 'migration_done';
const KEYS = ['username'];

export async function runMigration(): Promise<void> {
  if (StorageService.contains(MIGRATION_DONE_KEY)) return;

  for (const key of KEYS) {
    const value = await requestNativeValue(key);
    if (value != null && value !== '') {
      StorageService.set(key, value);
      notifyMigrationDone({ key, value });
    }
  }

  StorageService.set(MIGRATION_DONE_KEY, 'true');
}
