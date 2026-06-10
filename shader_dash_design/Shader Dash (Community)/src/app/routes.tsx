import { createBrowserRouter, Navigate } from "react-router";
import { DeviceFrame } from "./DeviceFrame";
import { Layout } from "./Layout";
import { Home, Health, Analytics, Profile, VitaChat } from "./screens/VitaApp";
import { Splash } from "./screens/Splash";
import { Onboarding } from "./screens/Onboarding";
import { Auth } from "./screens/Auth";
import { Products } from "./screens/Products";

export const router = createBrowserRouter([
  {
    path: "/",
    Component: DeviceFrame,
    children: [
      {
        index: true,
        Component: Splash,
      },
      {
        path: "onboarding",
        Component: Onboarding,
      },
      {
        path: "auth",
        Component: Auth,
      },
      {
        path: "app",
        Component: Layout,
        children: [
          { index: true, Component: Home },
          { path: "health", Component: Health },
          { path: "analytics", Component: Analytics },
          { path: "products", Component: Products },
          { path: "chat", Component: VitaChat },
          { path: "profile", Component: Profile },
          { path: "*", Component: () => <Navigate to="/app" replace /> },
        ],
      },
      {
        path: "*",
        Component: () => <Navigate to="/" replace />,
      }
    ]
  }
]);
