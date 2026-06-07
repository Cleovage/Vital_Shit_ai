import { Outlet } from "react-router";
import { Toaster } from "sonner";

export function DeviceFrame() {
  return (
    <main className="relative min-h-screen w-full bg-white flex flex-col font-sans text-[#0f172a] selection:bg-black/10 overflow-x-hidden">
      <Toaster
        theme="light"
        position="top-center"
        toastOptions={{
          style: {
            background: "rgba(255,255,255,0.95)",
            border: "1px solid rgba(0,0,0,0.08)",
            borderRadius: "18px",
            color: "#0f172a",
            backdropFilter: "blur(20px)",
            boxShadow: "0 8px 32px rgba(0,0,0,0.08)",
          },
        }}
      />

      {/* Ambient background — very subtle color blobs on pure white */}
      <div className="fixed inset-0 pointer-events-none z-0">
        {/* Base white */}
        <div className="absolute inset-0 bg-white" />

        {/* Green readiness blob — top left */}
        <div className="absolute -left-40 -top-40 h-[600px] w-[600px] rounded-full bg-emerald-400/[0.06] blur-[140px]" />

        {/* Blue sleep blob — top right */}
        <div className="absolute -right-40 top-0 h-[600px] w-[600px] rounded-full bg-blue-400/[0.06] blur-[140px]" />

        {/* Cyan AI blob — bottom center */}
        <div className="absolute bottom-0 left-1/2 -translate-x-1/2 h-[400px] w-[700px] rounded-full bg-cyan-300/[0.05] blur-[120px]" />

        {/* Very subtle warm neutral at bottom */}
        <div className="absolute bottom-0 left-0 right-0 h-1/3 bg-gradient-to-t from-slate-50/60 to-transparent" />
      </div>

      <div className="relative z-10 flex flex-1 flex-col w-full mx-auto max-w-5xl">
        <Outlet />
      </div>
    </main>
  );
}
