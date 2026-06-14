import React from 'react';
import { View, StyleSheet, ViewStyle, StyleProp } from 'react-native';
import { BlurView } from 'expo-blur';
import { useThemeStore } from '../store/useThemeStore';
import { Colors } from '../theme/colors';

interface GlassCardProps {
  children: React.ReactNode;
  style?: StyleProp<ViewStyle>;
  intensityMultiplier?: number;
}

export const GlassCard: React.FC<GlassCardProps> = ({ children, style, intensityMultiplier = 1 }) => {
  const { isDarkMode, glassmorphismEnabled, blurIntensity, transparencyLevel, glowEffect } = useThemeStore();
  
  const theme = isDarkMode ? Colors.dark : Colors.light;
  
  // Calculate exact hex string for rgba wrapper
  const baseColor = isDarkMode ? '255, 255, 255' : '255, 255, 255';
  
  const solidColor = theme.surface;
  const glassColor = `rgba(${baseColor}, ${transparencyLevel})`;
  
  const containerStyle = [
    styles.card,
    {
      borderColor: theme.border,
      backgroundColor: glassmorphismEnabled ? glassColor : solidColor,
    },
    glowEffect && {
      shadowColor: theme.glow,
      shadowOffset: { width: 0, height: 4 },
      shadowOpacity: 0.3,
      shadowRadius: 12,
      elevation: 8,
    },
    style
  ];

  if (glassmorphismEnabled) {
    return (
      <View style={containerStyle}>
        <BlurView 
          intensity={blurIntensity * intensityMultiplier} 
          tint={isDarkMode ? "dark" : "light"} 
          style={StyleSheet.absoluteFillObject} 
          experimentalBlurMethod="dimezisBlurView"
        />
        <View style={styles.content}>
          {children}
        </View>
      </View>
    );
  }

  return (
    <View style={containerStyle}>
      <View style={styles.content}>
        {children}
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  card: {
    borderRadius: 24,
    borderWidth: 1,
    overflow: 'hidden',
  },
  content: {
    padding: 20,
    zIndex: 2,
  }
});
