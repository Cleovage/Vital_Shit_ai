import { initializeApp } from 'firebase/app';
import { getFirestore } from 'firebase/firestore';
import { getAuth } from 'firebase/auth';

// TODO: Replace with your actual Firebase config from google-services.json
const firebaseConfig = {
  apiKey: "AIzaSyCReR35SLbUAh2KQfyrgV54KbmwZdDw7yY",
  authDomain: "wellbeing-app-45862.firebaseapp.com",
  projectId: "wellbeing-app-45862",
  storageBucket: "wellbeing-app-45862.firebasestorage.app",
  messagingSenderId: "867362317050",
  appId: "1:867362317050:android:91e1e2f8d9a92925839f43"
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);

// Export services
export const db = getFirestore(app);
export const auth = getAuth(app);
