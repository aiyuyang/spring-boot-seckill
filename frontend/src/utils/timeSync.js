import dayjs from 'dayjs';

// Server time synchronization utility
let serverTimeOffset = 0; // Offset between server time and client time (milliseconds)

/**
 * Set server time offset
 * @param {string|number} serverTime - Server time (ISO string or timestamp)
 */
export const setServerTimeOffset = (serverTime) => {
  let server;
  if (typeof serverTime === 'number') {
    // If it's a timestamp (milliseconds)
    server = dayjs(serverTime);
  } else {
    // If it's an ISO string
    server = dayjs(serverTime);
  }
  const client = dayjs();
  serverTimeOffset = server.valueOf() - client.valueOf();
  console.log('Time sync:', {
    server: server.format('YYYY-MM-DD HH:mm:ss'),
    client: client.format('YYYY-MM-DD HH:mm:ss'),
    offset: serverTimeOffset,
    offsetSeconds: Math.round(serverTimeOffset / 1000)
  });
};

/**
 * Get synced current time
 */
export const getSyncedTime = () => {
  return dayjs().add(serverTimeOffset, 'millisecond');
};

/**
 * Calculate countdown (seconds)
 * @param {string} targetTime - Target time (ISO format)
 * @returns {number} Remaining seconds
 */
export const getCountdown = (targetTime) => {
  const now = getSyncedTime();
  const target = dayjs(targetTime);
  const diff = target.diff(now, 'second');
  const result = Math.max(0, diff);
  
  // Debug info (development only)
  if (process.env.NODE_ENV === 'development' && result === 0 && diff < 0) {
    console.log('Countdown ended:', {
      now: now.format('YYYY-MM-DD HH:mm:ss'),
      target: target.format('YYYY-MM-DD HH:mm:ss'),
      diff: diff,
      diffMinutes: Math.round(diff / 60)
    });
  }
  
  return result;
};

/**
 * Format countdown display
 * @param {number} seconds - Remaining seconds
 * @returns {string} Formatted time string (HH:mm:ss)
 */
export const formatCountdown = (seconds) => {
  if (seconds <= 0) return '00:00:00';
  
  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  const secs = seconds % 60;
  
  return `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
};
