import { create } from 'zustand';
import { 
  initialize, 
  requestPermission, 
  readRecords, 
  getGrantedPermissions,
  Permission
} from 'react-native-health-connect';

export interface HealthSnapshot {
  steps: number;
  caloriesBurned: number;
  activeMinutes: number;
  sleepHours: number;
  hydrationLiters: number;
  avgHeartRate: number;
}

interface HealthState {
  isInitialized: boolean;
  hasPermissions: boolean;
  snapshot: HealthSnapshot;
  initHealthConnect: () => Promise<void>;
  requestPermissions: () => Promise<void>;
  fetchDailySnapshot: () => Promise<void>;
}

const REQUIRED_PERMISSIONS: Permission[] = [
  { accessType: 'read', recordType: 'Steps' },
  { accessType: 'read', recordType: 'ActiveCaloriesBurned' },
  { accessType: 'read', recordType: 'ExerciseSession' },
  { accessType: 'read', recordType: 'SleepSession' },
  { accessType: 'read', recordType: 'Hydration' },
  { accessType: 'write', recordType: 'Hydration' },
  { accessType: 'read', recordType: 'HeartRate' },
];

export const useHealthStore = create<HealthState>((set, get) => ({
  isInitialized: false,
  hasPermissions: false,
  snapshot: {
    steps: 0,
    caloriesBurned: 0,
    activeMinutes: 0,
    sleepHours: 0,
    hydrationLiters: 0,
    avgHeartRate: 0,
  },

  initHealthConnect: async () => {
    try {
      const isInitialized = await initialize();
      set({ isInitialized });

      if (isInitialized) {
        const granted = await getGrantedPermissions();
        // Simple check: if we have some granted permissions, we assume we have what we need
        // For production, you'd do a deep array comparison.
        if (granted.length >= REQUIRED_PERMISSIONS.length - 2) {
          set({ hasPermissions: true });
          get().fetchDailySnapshot();
        }
      }
    } catch (e) {
      console.error('Failed to init Health Connect:', e);
    }
  },

  requestPermissions: async () => {
    try {
      const granted = await requestPermission(REQUIRED_PERMISSIONS);
      if (granted.length > 0) {
        set({ hasPermissions: true });
        get().fetchDailySnapshot();
      }
    } catch (e) {
      console.error('Failed to request permissions:', e);
    }
  },

  fetchDailySnapshot: async () => {
    const { hasPermissions } = get();
    if (!hasPermissions) return;

    try {
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      const now = new Date();

      const timeRangeFilter = {
        operator: 'between' as const,
        startTime: today.toISOString(),
        endTime: now.toISOString()
      };

      // 1. Steps
      const stepsRecord = await readRecords('Steps', { timeRangeFilter });
      const totalSteps = stepsRecord.records.reduce((acc, cur) => acc + curr.count, 0);

      // 2. Calories
      const caloriesRecord = await readRecords('ActiveCaloriesBurned', { timeRangeFilter });
      const totalCalories = caloriesRecord.records.reduce((acc, cur) => acc + cur.energy.inKilocalories, 0);

      // 3. Active Minutes (from ExerciseSessions)
      const exerciseRecord = await readRecords('ExerciseSession', { timeRangeFilter });
      let activeMins = 0;
      exerciseRecord.records.forEach(session => {
        const start = new Date(session.startTime).getTime();
        const end = new Date(session.endTime).getTime();
        activeMins += (end - start) / (1000 * 60);
      });

      // 4. Sleep (look back 24 hours to catch last night)
      const yesterday = new Date(now.getTime() - 24 * 60 * 60 * 1000);
      const sleepRecord = await readRecords('SleepSession', { 
        timeRangeFilter: {
          operator: 'between',
          startTime: yesterday.toISOString(),
          endTime: now.toISOString()
        }
      });
      let sleepMins = 0;
      sleepRecord.records.forEach(session => {
        const start = new Date(session.startTime).getTime();
        const end = new Date(session.endTime).getTime();
        sleepMins += (end - start) / (1000 * 60);
      });

      // 5. Hydration
      const hydrationRecord = await readRecords('Hydration', { timeRangeFilter });
      const totalHydrationLiters = hydrationRecord.records.reduce((acc, cur) => acc + cur.volume.inLiters, 0);

      // 6. Heart Rate
      const hrRecord = await readRecords('HeartRate', { timeRangeFilter });
      let hrSum = 0;
      let hrCount = 0;
      hrRecord.records.forEach(record => {
        record.samples.forEach(sample => {
          hrSum += sample.beatsPerMinute;
          hrCount++;
        });
      });
      const avgHr = hrCount > 0 ? hrSum / hrCount : 0;

      set({
        snapshot: {
          steps: totalSteps,
          caloriesBurned: totalCalories,
          activeMinutes: activeMins,
          sleepHours: sleepMins / 60,
          hydrationLiters: totalHydrationLiters,
          avgHeartRate: avgHr,
        }
      });
    } catch (e) {
      console.error('Error fetching health snapshot:', e);
    }
  }
}));
