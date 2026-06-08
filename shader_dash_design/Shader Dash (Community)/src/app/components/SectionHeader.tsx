export function SectionHeader({ title, subtitle }: { title: string; subtitle?: string }) {
  return (
    <div className="px-0.5 pt-2 pb-1">
      <p className="text-[11px] font-bold uppercase tracking-[0.18em] text-black/35">{title}</p>
      {subtitle ? <p className="mt-0.5 text-[13px] text-black/45">{subtitle}</p> : null}
    </div>
  );
}
