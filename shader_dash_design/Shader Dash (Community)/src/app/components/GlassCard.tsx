import type { ReactNode } from "react";
import { clsx, type ClassValue } from "clsx";
import { twMerge } from "tailwind-merge";

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export function GlassCard({
  children,
  className,
  onClick,
}: {
  children: ReactNode;
  className?: string;
  onClick?: () => void;
}) {
  return (
    <div
      onClick={onClick}
      className={cn(
        "rounded-[30px] border border-black/[0.07] bg-white/78 shadow-[0_4px_24px_rgba(0,0,0,0.055),inset_0_1px_0_rgba(255,255,255,1)] backdrop-blur-2xl transition-all duration-300",
        onClick &&
          "cursor-pointer hover:-translate-y-0.5 hover:bg-white/90 hover:shadow-[0_8px_40px_rgba(0,0,0,0.09),inset_0_1px_0_rgba(255,255,255,1)] active:scale-[0.985]",
        className
      )}
    >
      {children}
    </div>
  );
}
