import React, { useEffect } from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity, Button } from 'react-native';
import CircularProgress from 'react-native-circular-progress-indicator';
import { useThemeStore } from '../store/useThemeStore';
import { useHealthStore } from '../store/useHealthStore';
import { Colors } from '../theme/colors';
import { GlassCard } from '../components/GlassCard';
import { Flame, Dumbbell, Moon, Beef, Lightbulb, Droplets, Heart } from 'lucide-react-native';

export const DashboardScreen = () => {
  const { isDarkMode, glowEffect } = useThemeStore();
  const theme = isDarkMode ? Colors.dark : Colors.light;
  const { hasPermissions, snapshot, requestPermissions, fetchDailySnapshot } = useHealthStore();

  // Simple readiness computation based on sleep and hr (dummy logic)
  const computeReadiness = () => {
    let base = 70;
    if (snapshot.sleepHours > 7) base += 20;
    else if (snapshot.sleepHours > 5) base += 10;
    if (snapshot.avgHeartRate > 0 && snapshot.avgHeartRate < 60) base += 10;
    return Math.min(100, base);
  };

  const readinessScore = computeReadiness();

  return (
    <ScrollView style={[styles.container, { backgroundColor: theme.background }]} contentContainerStyle={styles.scrollContent}>
      
      {/* Header */}
      <View style={styles.header}>
        <Text style={[styles.kicker, { color: theme.primary }]}>Health companion</Text>
        <Text style={[styles.title, { color: theme.text }]}>VitaAI</Text>
      </View>

      {!hasPermissions && (
        <GlassCard style={[styles.section, { borderColor: '#3B82F6', borderWidth: 1 }]}>
           <View style={{flexDirection: 'row', alignItems: 'center', marginBottom: 12}}>
              <Heart color="#3B82F6" size={24} />
              <Text style={{fontSize: 18, fontWeight: 'bold', marginLeft: 8, color: theme.text}}>Connect Health Data</Text>
           </View>
           <Text style={{color: theme.textSecondary, marginBottom: 16}}>
             Grant Health Connect access to sync your steps, sleep, and heart rate securely.
           </Text>
           <TouchableOpacity 
              style={{backgroundColor: theme.primary, padding: 12, borderRadius: 8, alignItems: 'center'}}
              onPress={requestPermissions}
           >
              <Text style={{color: '#FFF', fontWeight: 'bold'}}>Enable Sync</Text>
           </TouchableOpacity>
        </GlassCard>
      )}

      {/* Readiness Score Card */}
      <GlassCard style={styles.section}>
        <View style={styles.readinessHeader}>
          <Text style={[styles.readinessTitle, { color: theme.textSecondary }]}>Readiness</Text>
          <View style={[styles.badge, { backgroundColor: 'rgba(16, 185, 129, 0.1)', borderColor: 'rgba(16, 185, 129, 0.3)' }]}>
            <Text style={styles.badgeText}>{readinessScore > 80 ? 'Prime' : 'Good'}</Text>
          </View>
        </View>

        <View style={styles.readinessContent}>
          <View style={styles.readinessStats}>
            <View style={styles.scoreRow}>
              <Text style={[styles.scoreLarge, { color: theme.text }]}>{readinessScore}</Text>
              <Text style={[styles.scoreUnit, { color: theme.textSecondary }]}>%</Text>
            </View>

            <View style={styles.miniStatsRow}>
              <View style={styles.miniStat}>
                <Moon color="#3B82F6" size={16} />
                <Text style={[styles.miniStatLabel, { color: theme.textSecondary }]}>Sleep</Text>
                <Text style={[styles.miniStatValue, { color: theme.text }]}>{snapshot.sleepHours.toFixed(1)}h</Text>
              </View>
              <View style={styles.miniStat}>
                <Flame color="#F43F5E" size={16} />
                <Text style={[styles.miniStatLabel, { color: theme.textSecondary }]}>HR</Text>
                <Text style={[styles.miniStatValue, { color: theme.text }]}>{Math.round(snapshot.avgHeartRate)} bpm</Text>
              </View>
            </View>
          </View>

          <View style={styles.readinessRing}>
            <CircularProgress
              value={readinessScore}
              radius={60}
              duration={2000}
              progressValueColor={theme.text}
              maxValue={100}
              titleColor={theme.textSecondary}
              titleStyle={{fontWeight: 'bold'}}
              activeStrokeColor={theme.primary}
              inActiveStrokeColor={theme.border}
              activeStrokeWidth={12}
              inActiveStrokeWidth={12}
            />
          </View>
        </View>

        <View style={styles.insightBox}>
          <Text style={[styles.insightText, { color: theme.text }]}>
            <Text style={{ fontWeight: 'bold' }}>{readinessScore > 80 ? 'Prime condition. ' : 'Ready to move. '}</Text>
            You have logged {snapshot.steps} steps so far. Keep pushing towards your daily goal!
          </Text>
        </View>
      </GlassCard>

      {/* 2x2 Bento Grid */}
      <View style={styles.bentoRow}>
        <MetricCard 
          icon={<Flame color="#B45309" size={24} />}
          iconBg="#FEF3C7"
          label="Active energy"
          value={`${Math.round(snapshot.caloriesBurned)} kcal`}
          trend=""
          trendUp={true}
        />
        <MetricCard 
          icon={<Dumbbell color="#0E7490" size={24} />}
          iconBg="#CFFAFE"
          label="Training"
          value={`${Math.round(snapshot.activeMinutes)} min`}
          trend=""
          trendUp={false}
        />
      </View>
      <View style={styles.bentoRow}>
        <MetricCard 
          icon={<Droplets color="#2563EB" size={24} />}
          iconBg="#DBEAFE"
          label="Hydration"
          value={`${snapshot.hydrationLiters.toFixed(1)} L`}
          trend=""
          trendUp={true}
        />
        <MetricCard 
          icon={<Moon color="#1D4ED8" size={24} />}
          iconBg="#DBEAFE"
          label="Sleep"
          value={`${snapshot.sleepHours.toFixed(1)}h`}
          trend=""
          trendUp={true}
        />
      </View>

      {/* Insights */}
      <Text style={[styles.sectionHeader, { color: theme.text }]}>Today's Insights & Tips</Text>
      
      <GlassCard style={styles.tipCard}>
        <View style={styles.tipHeader}>
          <View style={[styles.tipIconBox, { backgroundColor: '#FFE4E6' }]}>
            <Lightbulb color="#E11D48" size={20} />
          </View>
          <View style={styles.tipBody}>
            <Text style={[styles.tipTitle, { color: theme.text }]}>Recovery Prioritization</Text>
            <Text style={[styles.tipDesc, { color: theme.textSecondary }]}>
              {snapshot.sleepHours < 6 ? "You had a short sleep last night. Consider light stretching instead of HIIT today." : "Great sleep duration! You're ready for a heavy session."}
            </Text>
          </View>
        </View>
      </GlassCard>

    </ScrollView>
  );
};

const MetricCard = ({ icon, iconBg, label, value, trend, trendUp }: any) => {
  const { isDarkMode } = useThemeStore();
  const theme = isDarkMode ? Colors.dark : Colors.light;
  
  return (
    <GlassCard style={styles.bentoCard}>
      <View style={styles.bentoContent}>
        <View style={[styles.bentoIcon, { backgroundColor: iconBg }]}>
          {icon}
        </View>
        <View style={styles.bentoTextCol}>
          <Text style={[styles.bentoLabel, { color: theme.textSecondary }]}>{label}</Text>
          <Text style={[styles.bentoValue, { color: theme.text }]}>{value}</Text>
          {trend ? (
            <Text style={[styles.bentoTrend, { color: trendUp ? '#059669' : '#DC2626' }]}>
              {trend}
            </Text>
          ) : null}
        </View>
      </View>
    </GlassCard>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  scrollContent: {
    padding: 20,
    paddingTop: 60,
    paddingBottom: 120, // Space for nav bar
  },
  header: {
    marginBottom: 24,
  },
  kicker: {
    fontSize: 14,
    fontWeight: '600',
    textTransform: 'uppercase',
    letterSpacing: 1.5,
    marginBottom: 4,
  },
  title: {
    fontSize: 36,
    fontWeight: '900',
  },
  section: {
    marginBottom: 20,
  },
  readinessHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 16,
  },
  readinessTitle: {
    fontSize: 14,
    fontWeight: 'bold',
    textTransform: 'uppercase',
    letterSpacing: 1,
    marginRight: 12,
  },
  badge: {
    paddingHorizontal: 12,
    paddingVertical: 4,
    borderRadius: 12,
    borderWidth: 1,
  },
  badgeText: {
    color: '#059669',
    fontWeight: 'bold',
    fontSize: 12,
  },
  readinessContent: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 20,
  },
  readinessStats: {
    flex: 1,
  },
  scoreRow: {
    flexDirection: 'row',
    alignItems: 'flex-end',
    marginBottom: 16,
  },
  scoreLarge: {
    fontSize: 56,
    fontWeight: '900',
    lineHeight: 60,
  },
  scoreUnit: {
    fontSize: 24,
    fontWeight: 'bold',
    marginBottom: 8,
    marginLeft: 4,
  },
  miniStatsRow: {
    flexDirection: 'row',
    gap: 16,
  },
  miniStat: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
  },
  miniStatLabel: {
    fontSize: 12,
    fontWeight: '500',
  },
  miniStatValue: {
    fontSize: 14,
    fontWeight: 'bold',
  },
  readinessRing: {
    alignItems: 'center',
    justifyContent: 'center',
  },
  insightBox: {
    backgroundColor: 'rgba(0,0,0,0.03)',
    padding: 16,
    borderRadius: 16,
  },
  insightText: {
    fontSize: 14,
    lineHeight: 20,
  },
  bentoRow: {
    flexDirection: 'row',
    gap: 12,
    marginBottom: 12,
  },
  bentoCard: {
    flex: 1,
    padding: 16,
  },
  bentoContent: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
  },
  bentoIcon: {
    width: 48,
    height: 48,
    borderRadius: 16,
    alignItems: 'center',
    justifyContent: 'center',
  },
  bentoTextCol: {
    flex: 1,
  },
  bentoLabel: {
    fontSize: 12,
    fontWeight: '600',
    marginBottom: 2,
  },
  bentoValue: {
    fontSize: 16,
    fontWeight: 'bold',
  },
  bentoTrend: {
    fontSize: 12,
    fontWeight: 'bold',
    marginTop: 2,
  },
  sectionHeader: {
    fontSize: 20,
    fontWeight: '800',
    marginTop: 12,
    marginBottom: 16,
  },
  tipCard: {
    marginBottom: 12,
    padding: 16,
  },
  tipHeader: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    gap: 16,
  },
  tipIconBox: {
    width: 44,
    height: 44,
    borderRadius: 14,
    alignItems: 'center',
    justifyContent: 'center',
  },
  tipBody: {
    flex: 1,
  },
  tipTitle: {
    fontSize: 16,
    fontWeight: 'bold',
    marginBottom: 6,
  },
  tipDesc: {
    fontSize: 14,
    lineHeight: 20,
  }
});
