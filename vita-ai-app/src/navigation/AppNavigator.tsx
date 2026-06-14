import React from 'react';
import { View, TouchableOpacity, StyleSheet } from 'react-native';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { NavigationContainer } from '@react-navigation/native';
import { Home, Activity, Bot, TrendingUp, Settings } from 'lucide-react-native';
import { useThemeStore } from '../store/useThemeStore';
import { Colors } from '../theme/colors';
import { GlassCard } from '../components/GlassCard';

import { SettingsScreen } from '../screens/SettingsScreen';
import { DummyScreen } from '../screens/DummyScreen';
import { DashboardScreen } from '../screens/DashboardScreen';

const Tab = createBottomTabNavigator();

const CustomTabBar = ({ state, descriptors, navigation }: any) => {
  const { isDarkMode, glowEffect } = useThemeStore();
  const theme = isDarkMode ? Colors.dark : Colors.light;

  return (
    <View style={styles.tabBarContainer}>
      <GlassCard 
        style={[styles.tabBar, glowEffect && { shadowColor: theme.primary }]}
        intensityMultiplier={1.5}
      >
        <View style={styles.tabBarInner}>
          {state.routes.map((route: any, index: number) => {
            const { options } = descriptors[route.key];
            const isFocused = state.index === index;

            const onPress = () => {
              const event = navigation.emit({
                type: 'tabPress',
                target: route.key,
                canPreventDefault: true,
              });

              if (!isFocused && !event.defaultPrevented) {
                navigation.navigate(route.name);
              }
            };

            let IconComponent;
            switch (route.name) {
              case 'Dashboard': IconComponent = Home; break;
              case 'Vitals': IconComponent = Activity; break;
              case 'Chat': IconComponent = Bot; break;
              case 'Trends': IconComponent = TrendingUp; break;
              case 'Settings': IconComponent = Settings; break;
              default: IconComponent = Home;
            }

            return (
              <TouchableOpacity
                key={route.key}
                onPress={onPress}
                style={[
                  styles.tabItem,
                  isFocused && { backgroundColor: isDarkMode ? 'rgba(255,255,255,0.1)' : 'rgba(0,0,0,0.05)' }
                ]}
              >
                <IconComponent 
                  color={isFocused ? theme.primary : theme.textSecondary} 
                  size={24} 
                  strokeWidth={isFocused ? 2.5 : 2}
                />
              </TouchableOpacity>
            );
          })}
        </View>
      </GlassCard>
    </View>
  );
};

export const AppNavigator = () => {
  return (
    <NavigationContainer>
      <Tab.Navigator
        tabBar={(props) => <CustomTabBar {...props} />}
        screenOptions={{
          headerShown: false,
        }}
      >
        <Tab.Screen name="Dashboard" component={DashboardScreen} />
        <Tab.Screen name="Vitals" children={() => <DummyScreen title="Vitals" />} />
        <Tab.Screen name="Chat" children={() => <DummyScreen title="VitaAI Chat" />} />
        <Tab.Screen name="Trends" children={() => <DummyScreen title="Trends & Analytics" />} />
        <Tab.Screen name="Settings" component={SettingsScreen} />
      </Tab.Navigator>
    </NavigationContainer>
  );
};

const styles = StyleSheet.create({
  tabBarContainer: {
    position: 'absolute',
    bottom: 24,
    left: 20,
    right: 20,
    zIndex: 100,
  },
  tabBar: {
    borderRadius: 30,
    padding: 0,
  },
  tabBarInner: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    alignItems: 'center',
    paddingVertical: 12,
    paddingHorizontal: 16,
  },
  tabItem: {
    padding: 12,
    borderRadius: 20,
  }
});
