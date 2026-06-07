import { RouterProvider } from "react-router";
import { router } from "./routes";

// App Entrypoint
export default function App() {
  return <RouterProvider router={router} />;
}
