import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { GlassCard } from '../components/GlassCard';
import { useThemeStore } from '../store/useThemeStore';
import { Colors } from '../theme/colors';

export const DummyScreen = ({ title }: { title: string }) => {
  const { isDarkMode } = useThemeStore();
  const theme = isDarkMode ? Colors.dark : Colors.light;

  return (
    <View style={[styles.container, { backgroundColor: theme.background }]}>
      <GlassCard style={styles.card}>
        <Text style={[styles.title, { color: theme.text }]}>{title}</Text>
        <Text style={{ color: theme.textSecondary, marginTop: 10 }}>
          This screen will be rebuilt in React Native.
        </Text>
      </GlassCard>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    padding: 20,
  },
  card: {
    alignItems: 'center',
    padding: 40,
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
  }
});
