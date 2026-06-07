import { useState, useEffect } from 'react';

interface GeolocationData {
  latitude: number;
  longitude: number;
  city: string;
  loading: boolean;
  error: string | null;
}

export const useGeolocation = (): GeolocationData => {
  const [data, setData] = useState<GeolocationData>({
    latitude: 0,
    longitude: 0,
    city: '',
    loading: true,
    error: null,
  });

  useEffect(() => {
    if (!navigator.geolocation) {
      setData(prev => ({
        ...prev,
        loading: false,
        error: 'Geolocation is not supported by this browser.',
      }));
      return;
    }

    navigator.geolocation.getCurrentPosition(
      async (position) => {
        const { latitude, longitude } = position.coords;
        
        try {
          // Mock reverse geocoding - in a real app, you'd use a service like OpenCage or Google Maps
          const mockCities = [
            'San Francisco', 'New York', 'London', 'Tokyo', 'Paris', 
            'Berlin', 'Sydney', 'Toronto', 'Amsterdam', 'Stockholm'
          ];
          const city = mockCities[Math.floor(Math.random() * mockCities.length)];
          
          setData({
            latitude,
            longitude,
            city,
            loading: false,
            error: null,
          });
        } catch (error) {
          setData(prev => ({
            ...prev,
            loading: false,
            error: 'Failed to get city name',
          }));
        }
      },
      (error) => {
        setData(prev => ({
          ...prev,
          loading: false,
          error: error.message,
        }));
      },
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 600000, // 10 minutes
      }
    );
  }, []);

  return data;
};