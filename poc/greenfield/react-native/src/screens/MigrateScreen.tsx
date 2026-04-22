import React, { useState } from 'react';
import { ActivityIndicator, Button, StyleSheet, Text, View } from 'react-native';
import { MIGRATION_DONE_KEY, runMigration } from '../services/MigrationService';
import { StorageService } from '../services/StorageService';

export function MigrateScreen() {
  const alreadyMigrated = StorageService.contains(MIGRATION_DONE_KEY);
  const [status, setStatus] = useState<'idle' | 'loading' | 'done' | 'error'>(
    alreadyMigrated ? 'done' : 'idle',
  );
  const [username, setUsername] = useState<string | undefined>(
    alreadyMigrated ? StorageService.get('username') : undefined,
  );

  async function handleMigrate() {
    setStatus('loading');
    try {
      await runMigration();
      setUsername(StorageService.get('username'));
      setStatus('done');
    } catch {
      setStatus('error');
    }
  }

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Data Migration</Text>

      {status === 'idle' && (
        <Button title="Migrate from Native Storage" onPress={handleMigrate} />
      )}

      {status === 'loading' && <ActivityIndicator size="large" />}

      {status === 'done' && (
        <>
          <Text style={styles.badge}>
            {alreadyMigrated ? 'Already migrated' : 'Migration complete'}
          </Text>
          <Text style={styles.result}>
            {username ? `Username: ${username}` : 'No data found in native storage.'}
          </Text>
        </>
      )}

      {status === 'error' && (
        <Text style={styles.error}>Migration failed. Check native storage setup.</Text>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, alignItems: 'center', justifyContent: 'center', gap: 24 },
  title: { fontSize: 22, fontWeight: '600' },
  badge: { fontSize: 13, fontWeight: '600', color: '#6c757d', textTransform: 'uppercase', letterSpacing: 1 },
  result: { fontSize: 16, color: '#2a9d8f' },
  error: { fontSize: 16, color: '#e63946' },
});
