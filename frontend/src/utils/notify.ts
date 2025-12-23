// src/utils/notify.ts
import toast from 'react-hot-toast';

type NotifyType = 'success' | 'error' | 'info' | 'warning';

function playTone(frequency = 660, duration = 120, volume = 0.0025) {
  try {
    const Ctx = (window.AudioContext || (window as any).webkitAudioContext);
    if (!Ctx) return;
    const ctx = new Ctx();
    const o = ctx.createOscillator();
    const g = ctx.createGain();
    o.type = 'sine';
    o.frequency.value = frequency;
    g.gain.value = volume;
    o.connect(g);
    g.connect(ctx.destination);
    o.start();
    setTimeout(() => {
      try { o.stop(); ctx.close(); } catch (e) { /* ignore */ }
    }, duration);
  } catch (e) {
    // ignore
  }
}

function shouldPlaySound(): boolean {
  try {
    const raw = localStorage.getItem('appSettings');
    if (!raw) return true;
    const obj = JSON.parse(raw);
    if (obj && typeof obj.notificationSound !== 'undefined') return Boolean(obj.notificationSound);
  } catch (e) {
    // ignore
  }
  return true;
}

function playForType(type: NotifyType) {
  if (!shouldPlaySound()) return;
  if (type === 'success') playTone(880, 90, 0.002);
  else if (type === 'error') playTone(220, 220, 0.0035);
  else if (type === 'warning') playTone(440, 160, 0.0025);
  else playTone(660, 120, 0.002);
}

export const notify = {
  success: (msg: string) => {
    playForType('success');
    toast.success(msg);
  },
  error: (msg: string) => {
    playForType('error');
    toast.error(msg);
  },
  info: (msg: string) => {
    // info uses toast without special styling
    playForType('info');
    toast(msg);
  },
  warning: (msg: string) => {
    playForType('warning');
    toast(msg);
  },
  raw: (fn: (t: typeof toast) => void) => fn(toast)
};

export default notify;
