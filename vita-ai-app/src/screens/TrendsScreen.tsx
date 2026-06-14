import React from 'react';
import { View, Text, StyleSheet, ScrollView } from 'react-native';
import { useThemeStore } from '../store/useThemeStore';
import { Colors } from '../theme/colors';
import { GlassCard } from '../components/GlassCard';

export const TrendsScreen = () => {
  const { isDarkMode } = useThemeStore();
  const theme = isDarkMode ? Colors.dark : Colors.light;

  // Dummy chart data
  const data = [40, 60, 80, 50, 90, 70, 100];
  const max = Math.max(...data);

  return (
    <ScrollView style={[styles.container, { backgroundColor: theme.background }]} contentContainerStyle={styles.scrollContent}>
      
      {/* Header */}
      <View style={styles.header}>
        <Text style={[styles.kicker, { color: theme.primary }]}>Analytics</Text>
        <Text style={[styles.title, { color: theme.text }]}>Trends</Text>
      </View>

      <GlassCard style={styles.section}>
        <Text style={[styles.sectionTitle, { color: theme.text }]}>Weekly Activity</Text>
        <Text style={{ color: theme.textSecondary, marginBottom: 24 }}>Last 7 days</Text>
        
        <View style={styles.chartContainer}>
          {data.map((val, i) => (
            <View key={i} style={styles.barWrapper}>
              <View style={[
                styles.bar, 
                { height: (val / max) * 150, backgroundColor: theme.primary }
              ]} />
            </View>
          ))}
        </View>
        <View style={styles.chartLabels}>
          {['M', 'T', 'W', 'T', 'F', 'S', 'S'].map((day, i) => (
            <Text key={i} style={[styles.dayLabel, { color: theme.textSecondary }]}>{day}</Text>
          ))}
        </View>
      </GlassCard>

    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  scrollContent: {
    padding: 20,
    paddingTop: 60,
    paddingBottom: 120,
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
    padding: 20,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 4,
  },
  chartContainer: {
    flexDirection: 'row',
    height: 150,
    alignItems: 'flex-end',
    justifyContent: 'space-between',
    paddingHorizontal: 8,
  },
  barWrapper: {
    width: '10%',
    alignItems: 'center',
  },
  bar: {
    width: '100%',
    borderRadius: 8,
  },
  chartLabels: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginTop: 12,
    paddingHorizontal: 8,
  },
  dayLabel: {
    fontSize: 12,
    fontWeight: '500',
    width: '10%',
    textAlign: 'center',
  }
});
