import React from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity } from 'react-native';
import CircularProgress from 'react-native-circular-progress-indicator';
import { useThemeStore } from '../store/useThemeStore';
import { Colors } from '../theme/colors';
import { GlassCard } from '../components/GlassCard';
import { Activity, Dumbbell, Store, Footprints, Flame } from 'lucide-react-native';

export const VitalsScreen = () => {
  const { isDarkMode } = useThemeStore();
  const theme = isDarkMode ? Colors.dark : Colors.light;

  return (
    <ScrollView style={[styles.container, { backgroundColor: theme.background }]} contentContainerStyle={styles.scrollContent}>
      
      {/* Header */}
      <View style={styles.header}>
        <Text style={[styles.kicker, { color: theme.primary }]}>Your Body</Text>
        <Text style={[styles.title, { color: theme.text }]}>Vitals</Text>
      </View>

      {/* Main Rings Card */}
      <GlassCard style={styles.section}>
        <Text style={[styles.sectionTitle, { color: theme.text }]}>Daily Activity</Text>
        
        <View style={styles.ringsContainer}>
          <View style={styles.ringItem}>
            <CircularProgress
              value={8500}
              radius={40}
              maxValue={10000}
              activeStrokeColor="#10B981"
              inActiveStrokeColor={theme.border}
              showProgressValue={false}
            />
            <View style={styles.ringLabelContainer}>
              <Footprints color="#10B981" size={20} />
              <Text style={[styles.ringVal, { color: theme.text }]}>8.5k</Text>
              <Text style={[styles.ringTitle, { color: theme.textSecondary }]}>Steps</Text>
            </View>
          </View>
          
          <View style={styles.ringItem}>
            <CircularProgress
              value={450}
              radius={40}
              maxValue={600}
              activeStrokeColor="#F59E0B"
              inActiveStrokeColor={theme.border}
              showProgressValue={false}
            />
            <View style={styles.ringLabelContainer}>
              <Flame color="#F59E0B" size={20} />
              <Text style={[styles.ringVal, { color: theme.text }]}>450</Text>
              <Text style={[styles.ringTitle, { color: theme.textSecondary }]}>Kcal</Text>
            </View>
          </View>

          <View style={styles.ringItem}>
            <CircularProgress
              value={45}
              radius={40}
              maxValue={60}
              activeStrokeColor="#3B82F6"
              inActiveStrokeColor={theme.border}
              showProgressValue={false}
            />
            <View style={styles.ringLabelContainer}>
              <Activity color="#3B82F6" size={20} />
              <Text style={[styles.ringVal, { color: theme.text }]}>45m</Text>
              <Text style={[styles.ringTitle, { color: theme.textSecondary }]}>Active</Text>
            </View>
          </View>
        </View>
      </GlassCard>

      {/* Actions Stack */}
      <Text style={[styles.subHeader, { color: theme.text }]}>Actions</Text>
      
      <TouchableOpacity activeOpacity={0.8}>
        <GlassCard style={styles.actionCard}>
          <View style={styles.actionRow}>
            <View style={[styles.actionIconBox, { backgroundColor: 'rgba(59, 130, 246, 0.1)' }]}>
              <Dumbbell color="#3B82F6" size={24} />
            </View>
            <View style={styles.actionText}>
              <Text style={[styles.actionTitle, { color: theme.text }]}>Start adaptive session</Text>
              <Text style={[styles.actionDesc, { color: theme.textSecondary }]}>20 min full-body strength</Text>
            </View>
          </View>
        </GlassCard>
      </TouchableOpacity>

      <TouchableOpacity activeOpacity={0.8} style={{ marginTop: 12 }}>
        <GlassCard style={styles.actionCard}>
          <View style={styles.actionRow}>
            <View style={[styles.actionIconBox, { backgroundColor: 'rgba(16, 185, 129, 0.1)' }]}>
              <Store color="#10B981" size={24} />
            </View>
            <View style={styles.actionText}>
              <Text style={[styles.actionTitle, { color: theme.text }]}>View Health Products</Text>
              <Text style={[styles.actionDesc, { color: theme.textSecondary }]}>Recommended gear & supplements</Text>
            </View>
          </View>
        </GlassCard>
      </TouchableOpacity>

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
    marginBottom: 24,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 16,
  },
  subHeader: {
    fontSize: 20,
    fontWeight: '800',
    marginBottom: 16,
  },
  ringsContainer: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    alignItems: 'center',
    paddingVertical: 12,
  },
  ringItem: {
    alignItems: 'center',
  },
  ringLabelContainer: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    justifyContent: 'center',
    alignItems: 'center',
  },
  ringVal: {
    fontSize: 16,
    fontWeight: 'bold',
    marginTop: 4,
  },
  ringTitle: {
    fontSize: 12,
    fontWeight: '500',
  },
  actionCard: {
    padding: 16,
  },
  actionRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  actionIconBox: {
    width: 52,
    height: 52,
    borderRadius: 16,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 16,
  },
  actionText: {
    flex: 1,
  },
  actionTitle: {
    fontSize: 16,
    fontWeight: 'bold',
    marginBottom: 4,
  },
  actionDesc: {
    fontSize: 14,
  }
});
