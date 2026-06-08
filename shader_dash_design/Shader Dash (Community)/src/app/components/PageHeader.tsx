export function PageHeader({ title, kicker }: { title: string; kicker: string }) {
  return (
    <header className="mb-6 flex items-start justify-between pt-4">
      <div>
        <p className="text-[13px] font-semibold uppercase tracking-[0.34em] text-black/40">{kicker}</p>
        <h1 className="mt-1.5 text-[40px] font-semibold leading-none tracking-[-0.05em] text-[#0f172a]">{title}</h1>
      </div>
      <div className="flex items-center gap-2 rounded-full border border-black/[0.08] bg-white/80 px-4 py-2.5 text-[13px] font-bold text-cyan-600 backdrop-blur-2xl shadow-[0_4px_16px_rgba(6,182,212,0.10)]">
        <span className="relative flex h-2.5 w-2.5">
          <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-cyan-400 opacity-75" />
          <span className="relative inline-flex h-2.5 w-2.5 rounded-full bg-cyan-500" />
        </span>
        AI Sync
      </div>
    </header>
  );
}
