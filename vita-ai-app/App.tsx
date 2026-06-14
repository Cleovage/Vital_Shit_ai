import { StatusBar } from 'expo-status-bar';
import { StyleSheet, View } from 'react-native';
import { AppNavigator } from './src/navigation/AppNavigator';
import { useThemeStore } from './src/store/useThemeStore';
import { useHealthStore } from './src/store/useHealthStore';
import { useEffect } from 'react';

export default function App() {
  const { isDarkMode } = useThemeStore();
  const { initHealthConnect } = useHealthStore();

  useEffect(() => {
    initHealthConnect();
  }, []);

  return (
    <View style={styles.container}>
      <StatusBar style={isDarkMode ? "light" : "dark"} />
      <AppNavigator />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
});
