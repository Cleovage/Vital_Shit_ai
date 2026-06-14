import React, { useState } from 'react';
import { View, Text, StyleSheet, TextInput, FlatList, KeyboardAvoidingView, Platform, TouchableOpacity, Dimensions } from 'react-native';
import { useThemeStore } from '../store/useThemeStore';
import { Colors } from '../theme/colors';
import { GlassCard } from '../components/GlassCard';
import { ArrowUp, Bot, History, Settings, CheckCircle2, XCircle } from 'lucide-react-native';
import CircularProgress from 'react-native-circular-progress-indicator';

// Dummy initial messages
const initialMessages = [
  { id: '1', text: "Hello! I'm **VitaAI**, your elite health coach. How can I help you optimize your wellness today?", isUser: false },
];

export const ChatScreen = () => {
  const { isDarkMode } = useThemeStore();
  const theme = isDarkMode ? Colors.dark : Colors.light;
  const [messages, setMessages] = useState(initialMessages);
  const [inputText, setInputText] = useState('');

  const handleSend = () => {
    if (!inputText.trim()) return;
    
    const newUserMsg = { id: Date.now().toString(), text: inputText, isUser: true };
    setMessages(prev => [...prev, newUserMsg]);
    setInputText('');

    // Simulate AI response with interactive cards
    setTimeout(() => {
      const responseText = inputText.toLowerCase();
      let aiMsg: any = { id: (Date.now() + 1).toString(), isUser: false };
      
      if (responseText.includes('water') || responseText.includes('drink')) {
        aiMsg.text = "I can log that for you. Please confirm.";
        aiMsg.action = { type: 'hydration', volume: 250 };
      } else if (responseText.includes('step')) {
        aiMsg.text = "You are doing great! Here is your current progress:\n<viz>";
        aiMsg.viz = { type: 'progress', current: 7500, goal: 10000, label: "Steps" };
      } else {
        aiMsg.text = "That's a great point! Remember to **stay consistent** and keep tracking your *daily habits*. Let me know if you want to log anything!";
      }

      setMessages(prev => [...prev, aiMsg]);
    }, 1000);
  };

  const renderMessage = ({ item }: { item: any }) => {
    const isUser = item.isUser;
    
    return (
      <View style={[styles.messageRow, isUser ? styles.messageRowUser : styles.messageRowAi]}>
        {!isUser && (
          <View style={styles.avatar}>
            <Bot color={theme.primary} size={20} />
          </View>
        )}
        
        <View style={{ maxWidth: '80%' }}>
          {isUser ? (
            <View style={[styles.userBubble, { backgroundColor: theme.primary }]}>
              <Text style={styles.userText}>{item.text}</Text>
            </View>
          ) : (
            <View>
              <Text style={[styles.aiName, { color: theme.textSecondary }]}>VitaAI Coach</Text>
              <MarkdownText text={item.text} color={theme.text} />
              
              {item.action && <ActionCard action={item.action} />}
              {item.viz && <VizCard viz={item.viz} />}
            </View>
          )}
        </View>
      </View>
    );
  };

  return (
    <KeyboardAvoidingView 
      style={[styles.container, { backgroundColor: theme.background }]} 
      behavior={Platform.OS === 'ios' ? 'padding' : undefined}
    >
      <View style={styles.header}>
        <View style={styles.headerTitleRow}>
          <Bot color={theme.text} size={24} />
          <Text style={[styles.headerTitle, { color: theme.text }]}>VitaAI</Text>
        </View>
        <History color={theme.textSecondary} size={24} />
      </View>

      <FlatList
        data={messages}
        keyExtractor={item => item.id}
        renderItem={renderMessage}
        contentContainerStyle={styles.messageList}
        showsVerticalScrollIndicator={false}
      />

      <View style={styles.inputArea}>
        <GlassCard style={styles.inputCard}>
          <View style={styles.inputRow}>
            <TextInput
              style={[styles.input, { color: theme.text }]}
              placeholder="Message VitaAI..."
              placeholderTextColor={theme.textSecondary}
              value={inputText}
              onChangeText={setInputText}
              multiline
            />
            <TouchableOpacity 
              style={[styles.sendButton, { backgroundColor: inputText.trim() ? theme.primary : theme.border }]}
              onPress={handleSend}
              disabled={!inputText.trim()}
            >
              <ArrowUp color={inputText.trim() ? '#FFF' : theme.textSecondary} size={20} />
            </TouchableOpacity>
          </View>
        </GlassCard>
        <Text style={[styles.disclaimer, { color: theme.textSecondary }]}>
          VitaAI Coach can make mistakes. Verify important health stats.
        </Text>
      </View>
    </KeyboardAvoidingView>
  );
};

// Extremely basic Markdown parser for RN
const MarkdownText = ({ text, color }: { text: string, color: string }) => {
  const parts = text.split(/(\*\*.*?\*\*|\*.*?\*)/g);
  
  return (
    <Text style={[styles.markdownText, { color }]}>
      {parts.map((part, index) => {
        if (part.startsWith('**') && part.endsWith('**')) {
          return <Text key={index} style={{ fontWeight: '900' }}>{part.slice(2, -2)}</Text>;
        }
        if (part.startsWith('*') && part.endsWith('*')) {
          return <Text key={index} style={{ fontStyle: 'italic' }}>{part.slice(1, -1)}</Text>;
        }
        return <Text key={index}>{part.replace('<viz>', '')}</Text>;
      })}
    </Text>
  );
};

const ActionCard = ({ action }: { action: any }) => {
  const { isDarkMode } = useThemeStore();
  const theme = isDarkMode ? Colors.dark : Colors.light;
  const [status, setStatus] = useState<'pending' | 'confirmed' | 'refused'>('pending');

  return (
    <GlassCard style={styles.actionCard}>
      <Text style={[styles.actionTitle, { color: theme.text }]}>Log {action.type}</Text>
      <Text style={{ color: theme.textSecondary, marginBottom: 16 }}>{action.volume} ml of water</Text>
      
      {status === 'pending' ? (
        <View style={styles.actionRow}>
          <TouchableOpacity style={[styles.actionBtn, { backgroundColor: theme.primary }]} onPress={() => setStatus('confirmed')}>
            <Text style={{ color: '#FFF', fontWeight: 'bold' }}>Confirm</Text>
          </TouchableOpacity>
          <TouchableOpacity style={[styles.actionBtn, { borderColor: '#EF4444', borderWidth: 1 }]} onPress={() => setStatus('refused')}>
            <Text style={{ color: '#EF4444', fontWeight: 'bold' }}>Refuse</Text>
          </TouchableOpacity>
        </View>
      ) : (
        <View style={{ flexDirection: 'row', alignItems: 'center' }}>
          {status === 'confirmed' ? <CheckCircle2 color="#10B981" /> : <XCircle color="#EF4444" />}
          <Text style={{ marginLeft: 8, color: status === 'confirmed' ? '#10B981' : '#EF4444', fontWeight: 'bold' }}>
            {status === 'confirmed' ? 'Logged successfully' : 'Action cancelled'}
          </Text>
        </View>
      )}
    </GlassCard>
  );
};

const VizCard = ({ viz }: { viz: any }) => {
  const { isDarkMode } = useThemeStore();
  const theme = isDarkMode ? Colors.dark : Colors.light;

  return (
    <GlassCard style={styles.vizCard}>
      <View style={{ flexDirection: 'row', alignItems: 'center' }}>
        <CircularProgress
          value={viz.current}
          radius={30}
          maxValue={viz.goal}
          activeStrokeColor={theme.primary}
          inActiveStrokeColor={theme.border}
          showProgressValue={false}
        />
        <View style={{ marginLeft: 16 }}>
          <Text style={[styles.vizTitle, { color: theme.text }]}>{viz.label}</Text>
          <Text style={{ color: theme.textSecondary }}>{viz.current} / {viz.goal}</Text>
        </View>
      </View>
    </GlassCard>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: 20,
    paddingTop: 60,
  },
  headerTitleRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  headerTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    marginLeft: 12,
  },
  messageList: {
    padding: 20,
    paddingBottom: 40,
  },
  messageRow: {
    flexDirection: 'row',
    marginBottom: 24,
    alignItems: 'flex-start',
  },
  messageRowUser: {
    justifyContent: 'flex-end',
  },
  messageRowAi: {
    justifyContent: 'flex-start',
  },
  avatar: {
    width: 32,
    height: 32,
    borderRadius: 16,
    backgroundColor: 'rgba(59, 130, 246, 0.2)',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 12,
  },
  userBubble: {
    padding: 16,
    borderRadius: 24,
    borderBottomRightRadius: 4,
  },
  userText: {
    color: '#FFF',
    fontSize: 16,
  },
  aiName: {
    fontSize: 14,
    fontWeight: 'bold',
    marginBottom: 4,
  },
  markdownText: {
    fontSize: 16,
    lineHeight: 24,
  },
  actionCard: {
    marginTop: 12,
    padding: 16,
  },
  actionTitle: {
    fontSize: 16,
    fontWeight: 'bold',
    marginBottom: 4,
  },
  actionRow: {
    flexDirection: 'row',
    gap: 12,
  },
  actionBtn: {
    flex: 1,
    padding: 12,
    borderRadius: 12,
    alignItems: 'center',
  },
  vizCard: {
    marginTop: 12,
    padding: 16,
  },
  vizTitle: {
    fontSize: 16,
    fontWeight: 'bold',
  },
  inputArea: {
    padding: 16,
    paddingBottom: Platform.OS === 'ios' ? 24 : 16,
  },
  inputCard: {
    borderRadius: 32,
    padding: 4,
  },
  inputRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  input: {
    flex: 1,
    minHeight: 40,
    maxHeight: 120,
    paddingHorizontal: 16,
    fontSize: 16,
  },
  sendButton: {
    width: 40,
    height: 40,
    borderRadius: 20,
    alignItems: 'center',
    justifyContent: 'center',
  },
  disclaimer: {
    textAlign: 'center',
    fontSize: 12,
    marginTop: 12,
  }
});
