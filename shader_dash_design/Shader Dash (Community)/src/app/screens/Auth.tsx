import { useState } from "react";
import { useNavigate } from "react-router";
import { motion, AnimatePresence } from "motion/react";
import { Bot, Mail, Lock, ArrowRight, User } from "lucide-react";

export function Auth() {
  const navigate = useNavigate();
  const [isLogin, setIsLogin] = useState(true);
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    // Simulate auth delay
    setTimeout(() => {
      setIsLoading(false);
      navigate("/app");
    }, 1500);
  };

  return (
    <div className="relative flex h-full w-full flex-col overflow-hidden bg-transparent px-6 py-12">
      {/* Background Ambient Glows */}
      <div className="absolute left-0 top-0 h-[500px] w-[500px] -translate-x-1/2 -translate-y-1/2 rounded-full bg-cyan-300/10 blur-[100px] pointer-events-none" />
      <div className="absolute bottom-0 right-0 h-[400px] w-[400px] translate-x-1/3 translate-y-1/3 rounded-full bg-blue-300/10 blur-[80px] pointer-events-none" />

      {/* Header */}
      <motion.div
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.8, ease: "easeOut" }}
        className="relative z-10 flex flex-col items-center mt-8 mb-10"
      >
        <div className="mb-4 flex h-16 w-16 items-center justify-center rounded-[1.25rem] bg-white/60 shadow-[0_8px_16px_rgba(15,23,42,0.05),inset_0_1px_1px_rgba(255,255,255,1)] backdrop-blur-xl ring-1 ring-white/50">
          <Bot className="h-8 w-8 text-cyan-500" strokeWidth={1.5} />
        </div>
        <h1 className="text-2xl font-semibold tracking-tight text-slate-900">
          {isLogin ? "Welcome back" : "Create your account"}
        </h1>
        <p className="mt-1.5 text-sm font-medium text-slate-500">
          {isLogin ? "Enter your details to sign in" : "Start your mindful health journey"}
        </p>
      </motion.div>

      {/* Form Card */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.8, delay: 0.1, ease: "easeOut" }}
        className="relative z-10 w-full rounded-[28px] bg-white/40 p-1 shadow-[0_8px_32px_rgba(15,23,42,0.04),inset_0_1px_1px_rgba(255,255,255,1)] backdrop-blur-2xl ring-1 ring-white/50"
      >
        <div className="rounded-[24px] bg-white/60 p-6">
          <form onSubmit={handleSubmit} className="flex flex-col gap-4">
            
            <AnimatePresence mode="popLayout">
              {!isLogin && (
                <motion.div
                  initial={{ opacity: 0, height: 0 }}
                  animate={{ opacity: 1, height: "auto" }}
                  exit={{ opacity: 0, height: 0 }}
                  transition={{ duration: 0.3 }}
                  className="flex flex-col gap-1.5 overflow-hidden"
                >
                  <label className="text-[13px] font-medium text-slate-700 ml-1">Name</label>
                  <div className="relative flex items-center">
                    <User className="absolute left-3.5 h-4 w-4 text-slate-400" />
                    <input
                      type="text"
                      placeholder="Jane Doe"
                      required={!isLogin}
                      className="w-full rounded-2xl bg-white/80 py-3.5 pl-10 pr-4 text-sm font-medium text-slate-900 shadow-sm ring-1 ring-slate-200/50 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-cyan-500/50 transition-all"
                    />
                  </div>
                </motion.div>
              )}
            </AnimatePresence>

            <div className="flex flex-col gap-1.5">
              <label className="text-[13px] font-medium text-slate-700 ml-1">Email</label>
              <div className="relative flex items-center">
                <Mail className="absolute left-3.5 h-4 w-4 text-slate-400" />
                <input
                  type="email"
                  placeholder="jane@example.com"
                  required
                  className="w-full rounded-2xl bg-white/80 py-3.5 pl-10 pr-4 text-sm font-medium text-slate-900 shadow-sm ring-1 ring-slate-200/50 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-cyan-500/50 transition-all"
                />
              </div>
            </div>

            <div className="flex flex-col gap-1.5">
              <div className="flex items-center justify-between ml-1 mr-1">
                <label className="text-[13px] font-medium text-slate-700">Password</label>
                {isLogin && (
                  <button type="button" className="text-[12px] font-medium text-cyan-600 hover:text-cyan-700 transition-colors">
                    Forgot?
                  </button>
                )}
              </div>
              <div className="relative flex items-center">
                <Lock className="absolute left-3.5 h-4 w-4 text-slate-400" />
                <input
                  type="password"
                  placeholder="••••••••"
                  required
                  className="w-full rounded-2xl bg-white/80 py-3.5 pl-10 pr-4 text-sm font-medium text-slate-900 shadow-sm ring-1 ring-slate-200/50 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-cyan-500/50 transition-all"
                />
              </div>
            </div>

            <button
              type="submit"
              disabled={isLoading}
              className="mt-2 flex w-full items-center justify-center gap-2 rounded-2xl bg-slate-900 py-3.5 text-sm font-medium text-white shadow-[0_8px_16px_rgba(15,23,42,0.15)] transition-all hover:bg-slate-800 active:scale-[0.98] disabled:opacity-70"
            >
              {isLoading ? (
                <motion.div
                  animate={{ rotate: 360 }}
                  transition={{ duration: 1, repeat: Infinity, ease: "linear" }}
                  className="h-4 w-4 rounded-full border-2 border-white/30 border-t-white"
                />
              ) : (
                <>
                  {isLogin ? "Sign In" : "Create Account"}
                  <ArrowRight className="h-4 w-4" />
                </>
              )}
            </button>
          </form>

          <div className="mt-6 flex items-center gap-3">
            <div className="h-px flex-1 bg-slate-200" />
            <span className="text-[11px] font-medium uppercase tracking-wider text-slate-400">or continue with</span>
            <div className="h-px flex-1 bg-slate-200" />
          </div>

          <div className="mt-6 grid grid-cols-2 gap-3">
            <button type="button" className="flex items-center justify-center gap-2 rounded-xl bg-white py-3 text-[13px] font-medium text-slate-700 shadow-sm ring-1 ring-slate-200/50 transition-all hover:bg-slate-50 active:scale-[0.98]">
              <svg className="h-4 w-4" viewBox="0 0 24 24">
                <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4" />
                <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853" />
                <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05" />
                <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335" />
              </svg>
              Google
            </button>
            <button type="button" className="flex items-center justify-center gap-2 rounded-xl bg-white py-3 text-[13px] font-medium text-slate-700 shadow-sm ring-1 ring-slate-200/50 transition-all hover:bg-slate-50 active:scale-[0.98]">
              <svg className="h-4 w-4" fill="currentColor" viewBox="0 0 24 24">
                <path d="M16.365 7.14c-1.393-.057-2.89.815-3.642 1.832-.634.852-1.127 2.05-1.028 3.167 1.5.093 2.92-1.042 3.633-1.895.66-.82 1.164-2.015.966-3.104zm-1.077 4.143c-.027 3.38 2.85 4.468 2.88 4.484-.022.066-455 1.542-1.077 3.197-1.378 1.95-2.802 1.93-3.86 1.93-1.09 0-2.585-.75-3.882-.75-1.32 0-2.868.766-3.883.766-1.11 0-2.637-.05-4.14-2.22-3.076-4.41-2.64-11.082.384-12.638 1.48-.766 3.003-.782 3.882-.782 1.05 0 2.457.75 3.86.75 1.34 0 2.654-.78 4.02-.78 1.637 0 2.98.543 3.738 1.306-2.51 1.258-2.106 4.707.08 5.735z" />
              </svg>
              Apple
            </button>
          </div>
        </div>
      </motion.div>

      {/* Footer Toggle */}
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ duration: 0.8, delay: 0.2, ease: "easeOut" }}
        className="mt-auto flex justify-center pb-8"
      >
        <button
          onClick={() => setIsLogin(!isLogin)}
          className="text-[14px] font-medium text-slate-500 transition-colors hover:text-slate-900"
        >
          {isLogin ? (
            <>Don't have an account? <span className="text-cyan-600">Sign Up</span></>
          ) : (
            <>Already have an account? <span className="text-cyan-600">Sign In</span></>
          )}
        </button>
      </motion.div>
    </div>
  );
}