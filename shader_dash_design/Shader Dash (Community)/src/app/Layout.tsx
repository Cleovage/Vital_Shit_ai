import { useRef } from "react";
import { Outlet, Link, useLocation } from "react-router";
import { Activity, BarChart3, Bot, BotMessageSquare, Home, User } from "lucide-react";
import { AnimatePresence, motion } from "motion/react";

const navItems = [
  { path: "/app/chat", icon: Bot, label: "Vita" },
  { path: "/app/health", icon: Activity, label: "Vitals" },
  { path: "/app", icon: Home, label: "Today" },
  { path: "/app/analytics", icon: BarChart3, label: "Trends" },
  { path: "/app/profile", icon: User, label: "You" },
];

const navIndex: Record<string, number> = {
  "/app/chat": 0,
  "/app/health": 1,
  "/app": 2,
  "/app/analytics": 3,
  "/app/profile": 4,
};

function getIndex(pathname: string) {
  for (const [path, idx] of Object.entries(navIndex)) {
    if (path === "/app" ? pathname === "/app" || pathname === "/app/" : pathname.startsWith(path)) return idx;
  }
  return 2;
}

const screenVariants = {
  enter: (dir: number) => ({ opacity: 0, x: dir * 48 }),
  center: { opacity: 1, x: 0 },
  exit: (dir: number) => ({ opacity: 0, x: dir * -48 }),
};

export function Layout() {
  const location = useLocation();
  const prevIndexRef = useRef(getIndex(location.pathname));
  const directionRef = useRef(1);

  const currentIndex = getIndex(location.pathname);
  const prevIndex = prevIndexRef.current;
  if (prevIndex !== currentIndex) {
    directionRef.current = currentIndex > prevIndex ? 1 : -1;
    prevIndexRef.current = currentIndex;
  }

  const isActive = (path: string) =>
    path === "/app"
      ? location.pathname === "/app" || location.pathname === "/app/"
      : location.pathname.startsWith(path);

  return (
    <>
      <div className="flex flex-1 flex-col overflow-hidden px-5 pt-6 pb-28">
        <AnimatePresence mode="wait" custom={directionRef.current}>
          <motion.div
            key={location.pathname}
            custom={directionRef.current}
            variants={screenVariants}
            initial="enter"
            animate="center"
            exit="exit"
            transition={{ duration: 0.28, ease: [0.32, 0.72, 0, 1] }}
            className="flex flex-1 flex-col overflow-y-auto [scrollbar-width:none] [&::-webkit-scrollbar]:hidden"
          >
            <Outlet />
          </motion.div>
        </AnimatePresence>
      </div>

      <div className="fixed bottom-6 left-1/2 -translate-x-1/2 z-50 px-5 mx-auto w-full max-w-xl pointer-events-none">
        {/* Soft shadow bloom behind nav */}
        <div className="absolute inset-x-16 bottom-0 h-12 rounded-full bg-black/10 blur-2xl pointer-events-none" />

        <div className="relative flex items-center justify-between rounded-full border border-black/[0.07] bg-white/85 p-2 shadow-[0_12px_40px_rgba(0,0,0,0.08),0_2px_8px_rgba(0,0,0,0.04),inset_0_1px_0_rgba(255,255,255,0.95)] backdrop-blur-3xl pointer-events-auto">
          {navItems.map((item) => {
            const Icon = item.icon;
            const active = isActive(item.path);
            return (
              <Link
                key={item.path}
                to={item.path}
                className={`group flex flex-1 flex-col items-center gap-1.5 rounded-full px-1 py-3 transition-all duration-300 ${
                  active
                    ? "bg-[#0f172a] text-white shadow-[0_8px_24px_rgba(15,23,42,0.22)] scale-105"
                    : "text-black/45 hover:bg-black/[0.05] hover:text-black/70"
                }`}
              >
                <div className="relative flex items-center justify-center">
                  <Icon
                    className={`relative z-10 ${active ? "h-5 w-5 scale-110" : "h-6 w-6 group-hover:scale-110 transition-transform duration-300"}`}
                    strokeWidth={active ? 2.5 : 2}
                  />
                </div>
                {active && (
                  <span className="text-[10px] font-extrabold leading-none tracking-[-0.03em]">
                    {item.label}
                  </span>
                )}
              </Link>
            );
          })}
        </div>

        {!location.pathname.includes("/chat") && (
          <Link
            to="/app/chat"
            className="absolute -top-16 right-5 flex h-14 w-14 items-center justify-center rounded-full bg-gradient-to-br from-[#1e293b] to-[#0f172a] text-white shadow-[0_14px_38px_rgba(0,0,0,0.20),inset_0_1px_0_rgba(255,255,255,0.12)] backdrop-blur-2xl transition-all hover:-translate-y-1 hover:scale-110 hover:shadow-[0_20px_48px_rgba(0,0,0,0.28)] pointer-events-auto"
          >
            <BotMessageSquare className="h-6 w-6" />
          </Link>
        )}
      </div>
    </>
  );
}
