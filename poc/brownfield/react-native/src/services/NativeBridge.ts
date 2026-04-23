import ReactNativeBrownfield, {
  type MessageEvent,
} from '@callstack/react-native-brownfield';

type NativeResponse = { requestId: string; value: string | null };

const pending = new Map<string, (value: string | null) => void>();
let subscribed = false;

function ensureSubscription() {
  if (subscribed) return;
  subscribed = true;
  ReactNativeBrownfield.onMessage((event: MessageEvent) => {
    const data = event.data as Partial<NativeResponse> & { type?: string };
    if (data?.type === 'nativeValueResponse' && typeof data.requestId === 'string') {
      const resolve = pending.get(data.requestId);
      if (resolve) {
        pending.delete(data.requestId);
        resolve(data.value ?? null);
      }
    }
  });
}

export function requestNativeValue(key: string, timeoutMs = 3000): Promise<string | null> {
  ensureSubscription();
  const requestId = `${Date.now()}-${Math.random().toString(36).slice(2)}`;

  return new Promise((resolve) => {
    const timer = setTimeout(() => {
      if (pending.delete(requestId)) resolve(null);
    }, timeoutMs);

    pending.set(requestId, (value) => {
      clearTimeout(timer);
      resolve(value);
    });

    ReactNativeBrownfield.postMessage({ type: 'getNativeValue', requestId, key });
  });
}

export function notifyMigrationDone(payload: { key: string; value: string | null }) {
  ReactNativeBrownfield.postMessage({ type: 'migrationDone', ...payload });
}
