import { useState, useEffect } from 'react';

interface TimeData {
  time: string;
  date: string;
}

export const useTime = (): TimeData => {
  const [timeData, setTimeData] = useState<TimeData>({
    time: '',
    date: '',
  });

  useEffect(() => {
    const updateTime = () => {
      const now = new Date();
      const time = now.toLocaleTimeString([], { 
        hour: '2-digit', 
        minute: '2-digit',
        hour12: false 
      });
      const date = now.toLocaleDateString([], { 
        weekday: 'long',
        month: 'long', 
        day: 'numeric' 
      });
      
      setTimeData({ time, date });
    };

    updateTime();
    const interval = setInterval(updateTime, 1000);

    return () => clearInterval(interval);
  }, []);

  return timeData;
};