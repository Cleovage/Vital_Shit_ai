import { create } from 'zustand';

interface ThemeState {
  isDarkMode: boolean;
  glassmorphismEnabled: boolean;
  blurIntensity: number; // 1 to 100
  transparencyLevel: number; // 0.1 to 1.0
  glowEffect: boolean;
  toggleDarkMode: () => void;
  toggleGlassmorphism: () => void;
  setBlurIntensity: (val: number) => void;
  setTransparencyLevel: (val: number) => void;
  toggleGlowEffect: () => void;
}

export const useThemeStore = create<ThemeState>((set) => ({
  isDarkMode: false,
  glassmorphismEnabled: true,
  blurIntensity: 50,
  transparencyLevel: 0.6,
  glowEffect: true,
  
  toggleDarkMode: () => set((state) => ({ isDarkMode: !state.isDarkMode })),
  toggleGlassmorphism: () => set((state) => ({ glassmorphismEnabled: !state.glassmorphismEnabled })),
  setBlurIntensity: (val) => set({ blurIntensity: val }),
  setTransparencyLevel: (val) => set({ transparencyLevel: val }),
  toggleGlowEffect: () => set((state) => ({ glowEffect: !state.glowEffect })),
}));
