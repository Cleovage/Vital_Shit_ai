import { useState, useEffect } from 'react';

interface WeatherData {
  temperature: number;
  condition: string;
  loading: boolean;
  error: string | null;
}

export const useWeather = (latitude: number, longitude: number): WeatherData => {
  const [weather, setWeather] = useState<WeatherData>({
    temperature: 0,
    condition: '',
    loading: true,
    error: null,
  });

  useEffect(() => {
    if (!latitude || !longitude) return;

    const fetchWeather = async () => {
      try {
        // Mock weather API response
        // In a real app, you'd use OpenWeatherMap, WeatherAPI, or similar
        await new Promise(resolve => setTimeout(resolve, 1000)); // Simulate API delay
        
        const mockTemperatures = [18, 22, 15, 25, 12, 28, 20, 16, 24, 19];
        const mockConditions = ['Clear', 'Cloudy', 'Partly Cloudy', 'Sunny', 'Overcast'];
        
        const temperature = mockTemperatures[Math.floor(Math.random() * mockTemperatures.length)];
        const condition = mockConditions[Math.floor(Math.random() * mockConditions.length)];
        
        setWeather({
          temperature,
          condition,
          loading: false,
          error: null,
        });
      } catch (error) {
        setWeather(prev => ({
          ...prev,
          loading: false,
          error: 'Failed to fetch weather data',
        }));
      }
    };

    fetchWeather();
  }, [latitude, longitude]);

  return weather;
};