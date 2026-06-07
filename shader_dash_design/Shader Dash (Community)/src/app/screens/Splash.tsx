import { useEffect } from "react";
import { useNavigate } from "react-router";
import { Bot, Sparkles } from "lucide-react";
import { motion } from "motion/react";

export function Splash() {
  const navigate = useNavigate();

  useEffect(() => {
    const timer = setTimeout(() => {
      navigate("/onboarding");
    }, 3500);
    return () => clearTimeout(timer);
  }, [navigate]);

  return (
    <div className="relative flex h-full w-full flex-col items-center justify-center overflow-hidden bg-transparent">
      <motion.div
        initial={{ opacity: 0, scale: 0.8 }}
        animate={{ opacity: 1, scale: 1 }}
        transition={{ duration: 2, ease: "easeOut" }}
        className="absolute left-1/2 top-1/2 h-[520px] w-[520px] -translate-x-1/2 -translate-y-1/2 rounded-full bg-[#7bb7a0]/24 blur-[100px] pointer-events-none"
      />
      <motion.div
        initial={{ opacity: 0, scale: 0.8 }}
        animate={{ opacity: 1, scale: 1 }}
        transition={{ duration: 2, delay: 0.3, ease: "easeOut" }}
        className="absolute left-1/3 top-1/3 h-[360px] w-[360px] -translate-x-1/2 -translate-y-1/2 rounded-full bg-[#d98b55]/16 blur-[80px] pointer-events-none"
      />

      <div className="relative z-10 flex flex-col items-center px-8">
        <motion.div
          initial={{ y: 30, opacity: 0, scale: 0.85 }}
          animate={{ y: 0, opacity: 1, scale: 1 }}
          transition={{ duration: 1, type: "spring", bounce: 0.3, damping: 15 }}
        >
          <motion.div
            animate={{ y: [0, -10, 0] }}
            transition={{ duration: 4, repeat: Infinity, ease: "easeInOut" }}
            className="relative mb-8 flex h-28 w-28 items-center justify-center rounded-[2.1rem] border border-[#26352f]/10 bg-[#fffaf0]/72 shadow-[0_28px_60px_rgba(23,61,53,0.16),inset_0_1px_1px_rgba(255,255,255,1)] backdrop-blur-2xl"
          >
            <div className="absolute inset-0 rounded-[2.1rem] bg-[linear-gradient(135deg,rgba(255,250,240,.2),rgba(123,183,160,.36))] mix-blend-multiply" />
            <Bot className="h-12 w-12 text-[#173d35] relative z-10" strokeWidth={1.5} />
            <motion.div
              initial={{ opacity: 0, scale: 0, rotate: -45 }}
              animate={{ opacity: 1, scale: 1, rotate: 0 }}
              transition={{ delay: 0.7, duration: 0.6, type: "spring", bounce: 0.5 }}
              className="absolute -right-2 -top-2 flex h-9 w-9 items-center justify-center rounded-full bg-[#173d35] text-[#fffaf0] shadow-md ring-1 ring-black/5"
            >
              <Sparkles className="h-4 w-4" strokeWidth={2} />
            </motion.div>
          </motion.div>
        </motion.div>

        <motion.div
          initial={{ opacity: 0, y: 15 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.4, duration: 0.8, ease: "easeOut" }}
          className="flex flex-col items-center text-center"
        >
          <p className="mb-3 font-['Space_Mono'] text-[10px] font-bold uppercase tracking-[0.28em] text-[#776d5f]">private wellness intelligence</p>
          <h1 className="mb-2 font-['Instrument_Serif'] text-6xl font-normal tracking-[-0.06em] text-[#18201d]">
            VitaAI
          </h1>
          <motion.p
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ delay: 0.8, duration: 0.8 }}
            className="max-w-[250px] text-[15px] font-semibold leading-relaxed tracking-[-0.03em] text-[#5f6b60]"
          >
            A calmer daily read on your body, habits, and recovery.
          </motion.p>
        </motion.div>

        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 1.2, duration: 0.8 }}
          className="mt-16 flex gap-2"
        >
          {[0, 1, 2].map((i) => (
            <motion.div
              key={i}
              animate={{ opacity: [0.25, 1, 0.25], scale: [0.8, 1, 0.8] }}
              transition={{ duration: 1.5, repeat: Infinity, delay: i * 0.2, ease: "easeInOut" }}
              className="h-2 w-2 rounded-full bg-[#173d35]"
            />
          ))}
        </motion.div>
      </div>
    </div>
  );
}
