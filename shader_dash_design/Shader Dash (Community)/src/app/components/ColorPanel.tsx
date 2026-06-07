import { useState } from 'react';

interface ColorPanelProps {
  onColorsChange: (colors: [string, string, string]) => void;
}

const ColorPanel = ({ onColorsChange }: ColorPanelProps) => {
  // Default colors that create nice spectral effects
  const [color1, setColor1] = useState('#0000ff'); // Pure blue for default hue
  const [color2, setColor2] = useState('#ff00ff'); // High saturation magenta  
  const [color3, setColor3] = useState('#ffffff'); // White for full brightness

  const handleColorChange = (colorIndex: number, newColor: string) => {
    let updatedColors: [string, string, string];
    
    if (colorIndex === 0) {
      setColor1(newColor);
      updatedColors = [newColor, color2, color3];
    } else if (colorIndex === 1) {
      setColor2(newColor);
      updatedColors = [color1, newColor, color3];
    } else {
      setColor3(newColor);
      updatedColors = [color1, color2, newColor];
    }
    
    onColorsChange(updatedColors);
  };

  const presets = [
    { name: 'Original', colors: ['#0000ff', '#ff00ff', '#ffffff'] },
    { name: 'Warm Shift', colors: ['#ff4500', '#ff1493', '#ffebcd'] },
    { name: 'Cool Shift', colors: ['#00ffff', '#4169e1', '#f0f8ff'] },
    { name: 'Vibrant', colors: ['#ff0080', '#ff0080', '#ffffff'] },
    { name: 'Muted', colors: ['#708090', '#696969', '#d3d3d3'] },
    { name: 'Sunset', colors: ['#ff6347', '#ff4500', '#ffd700'] },
    { name: 'Ocean', colors: ['#008b8b', '#20b2aa', '#e0ffff'] }
  ];

  const applyPreset = (colors: string[]) => {
    setColor1(colors[0]);
    setColor2(colors[1]);
    setColor3(colors[2]);
    onColorsChange([colors[0], colors[1], colors[2]]);
  };

  return (
    <div className="bg-white/90 backdrop-blur-sm border border-gray-300 rounded-lg p-4 shadow-lg">
      <h3 className="text-sm font-medium mb-3 text-gray-800">Spectral Controls</h3>
      
      <div className="space-y-3 mb-4">
        <div className="flex items-center gap-2">
          <input
            type="color"
            value={color1}
            onChange={(e) => handleColorChange(0, e.target.value)}
            className="w-8 h-8 rounded border border-gray-300 cursor-pointer"
          />
          <span className="text-xs text-gray-600">Hue Shift</span>
        </div>
        
        <div className="flex items-center gap-2">
          <input
            type="color"
            value={color2}
            onChange={(e) => handleColorChange(1, e.target.value)}
            className="w-8 h-8 rounded border border-gray-300 cursor-pointer"
          />
          <span className="text-xs text-gray-600">Saturation</span>
        </div>
        
        <div className="flex items-center gap-2">
          <input
            type="color"
            value={color3}
            onChange={(e) => handleColorChange(2, e.target.value)}
            className="w-8 h-8 rounded border border-gray-300 cursor-pointer"
          />
          <span className="text-xs text-gray-600">Brightness</span>
        </div>
      </div>

      <div>
        <h4 className="text-xs font-medium mb-2 text-gray-700">Presets</h4>
        <div className="space-y-1">
          {presets.map((preset) => (
            <button
              key={preset.name}
              onClick={() => applyPreset(preset.colors)}
              className="w-full text-left text-xs px-2 py-1 rounded hover:bg-gray-100 transition-colors text-gray-700"
            >
              {preset.name}
            </button>
          ))}
        </div>
      </div>
    </div>
  );
};

export default ColorPanel;