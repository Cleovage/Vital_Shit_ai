import React from 'react';
import { View, Text, StyleSheet, Switch, ScrollView } from 'react-native';
import Slider from '@react-native-community/slider';
import { useThemeStore } from '../store/useThemeStore';
import { Colors } from '../theme/colors';
import { GlassCard } from '../components/GlassCard';
import { Settings, Droplets, Sun, Moon, Sparkles } from 'lucide-react-native';

export const SettingsScreen = () => {
  const { 
    isDarkMode, 
    glassmorphismEnabled, 
    blurIntensity, 
    transparencyLevel, 
    glowEffect,
    toggleDarkMode,
    toggleGlassmorphism,
    setBlurIntensity,
    setTransparencyLevel,
    toggleGlowEffect
  } = useThemeStore();

  const theme = isDarkMode ? Colors.dark : Colors.light;

  return (
    <ScrollView style={[styles.container, { backgroundColor: theme.background }]}>
      <View style={styles.header}>
        <Settings color={theme.text} size={32} />
        <Text style={[styles.headerTitle, { color: theme.text }]}>UI Tuning</Text>
      </View>

      <GlassCard style={styles.section}>
        <View style={styles.row}>
          <View style={styles.rowLeft}>
            {isDarkMode ? <Moon color={theme.text} size={20} /> : <Sun color={theme.text} size={20} />}
            <Text style={[styles.label, { color: theme.text }]}>Dark Mode</Text>
          </View>
          <Switch value={isDarkMode} onValueChange={toggleDarkMode} />
        </View>

        <View style={styles.divider} />

        <View style={styles.row}>
          <View style={styles.rowLeft}>
            <Droplets color={theme.text} size={20} />
            <Text style={[styles.label, { color: theme.text }]}>Glassmorphism</Text>
          </View>
          <Switch value={glassmorphismEnabled} onValueChange={toggleGlassmorphism} />
        </View>

        <View style={styles.divider} />

        <View style={styles.row}>
          <View style={styles.rowLeft}>
            <Sparkles color={theme.text} size={20} />
            <Text style={[styles.label, { color: theme.text }]}>Glow Effect</Text>
          </View>
          <Switch value={glowEffect} onValueChange={toggleGlowEffect} />
        </View>
      </GlassCard>

      {glassmorphismEnabled && (
        <GlassCard style={styles.section}>
          <Text style={[styles.sectionTitle, { color: theme.text }]}>Glass Settings</Text>
          
          <View style={styles.sliderContainer}>
            <Text style={[styles.label, { color: theme.textSecondary }]}>
              Blur Intensity: {Math.round(blurIntensity)}
            </Text>
            <Slider
              style={{ width: '100%', height: 40 }}
              minimumValue={1}
              maximumValue={100}
              value={blurIntensity}
              onValueChange={setBlurIntensity}
              minimumTrackTintColor={theme.primary}
              maximumTrackTintColor={theme.border}
            />
          </View>

          <View style={styles.sliderContainer}>
            <Text style={[styles.label, { color: theme.textSecondary }]}>
              Transparency: {Math.round(transparencyLevel * 100)}%
            </Text>
            <Slider
              style={{ width: '100%', height: 40 }}
              minimumValue={0.0}
              maximumValue={1.0}
              value={transparencyLevel}
              onValueChange={setTransparencyLevel}
              minimumTrackTintColor={theme.primary}
              maximumTrackTintColor={theme.border}
            />
          </View>
        </GlassCard>
      )}

      {/* Preview Card */}
      <View style={styles.previewContainer}>
        <Text style={[styles.sectionTitle, { color: theme.text, marginBottom: 12 }]}>Preview</Text>
        <GlassCard>
          <Text style={{ color: theme.text, fontSize: 18, fontWeight: 'bold' }}>Sample Card</Text>
          <Text style={{ color: theme.textSecondary, marginTop: 8 }}>
            Adjust the sliders above to see how this card changes in real-time. Notice the blur, border, and glow effects.
          </Text>
        </GlassCard>
      </View>
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 20,
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 24,
    marginTop: 40,
  },
  headerTitle: {
    fontSize: 28,
    fontWeight: 'bold',
    marginLeft: 12,
  },
  section: {
    marginBottom: 20,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: '600',
    marginBottom: 16,
  },
  row: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 8,
  },
  rowLeft: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  label: {
    fontSize: 16,
    marginLeft: 12,
  },
  divider: {
    height: 1,
    backgroundColor: 'rgba(150,150,150,0.1)',
    marginVertical: 8,
  },
  sliderContainer: {
    marginBottom: 16,
  },
  previewContainer: {
    marginTop: 20,
    marginBottom: 40,
  }
});
