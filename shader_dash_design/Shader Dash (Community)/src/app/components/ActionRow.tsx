import { Link } from "react-router";
import { ChevronRight, type LucideIcon } from "lucide-react";
import { GlassCard } from "./GlassCard";

type ActionRowStyle = "default" | "destructive";

export function ActionRow({
  icon: Icon,
  title,
  subtitle,
  to,
  onClick,
  style = "default",
}: {
  icon: LucideIcon;
  title: string;
  subtitle: string;
  to?: string;
  onClick?: () => void;
  style?: ActionRowStyle;
}) {
  const isDestructive = style === "destructive";
  const titleClass = isDestructive ? "text-red-500" : "text-[#0f172a]";
  const iconWrapClass = isDestructive ? "bg-red-500/10 text-red-500" : "bg-black/[0.04] text-[#0f172a]";
  const chevronClass = isDestructive ? "text-red-400" : "text-black/40";

  const content = (
    <GlassCard className="p-5 transition-all duration-300 hover:scale-[1.01] hover:shadow-[0_10px_28px_rgba(0,0,0,0.07)] active:scale-[0.98]">
      <div className="flex items-center gap-4">
        <div className={`flex h-14 w-14 items-center justify-center rounded-[20px] shadow-sm backdrop-blur-sm ${iconWrapClass}`}>
          <Icon className="h-6 w-6" />
        </div>
        <div className="flex-1">
          <p className={`text-[16px] font-semibold tracking-[-0.03em] ${titleClass}`}>{title}</p>
          <p className="mt-0.5 text-[14px] text-black/50">{subtitle}</p>
        </div>
        <div className={`flex h-8 w-8 items-center justify-center rounded-full ${isDestructive ? "bg-red-500/10" : "bg-black/[0.04]"}`}>
          <ChevronRight className={`h-5 w-5 ${chevronClass}`} />
        </div>
      </div>
    </GlassCard>
  );

  if (to) {
    return (
      <Link to={to} className="block">
        {content}
      </Link>
    );
  }

  return (
    <button type="button" onClick={onClick} className="w-full text-left">
      {content}
    </button>
  );
}
