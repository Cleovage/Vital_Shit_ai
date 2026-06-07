import svgPaths from "./svg-6t2ihzejiy";

function Frame2147241515() {
  return (
    <div className="box-border content-stretch flex flex-row gap-2 items-start justify-center p-0 relative shrink-0">
      <div className="flex flex-col font-['Bitcount_Single:Regular',_sans-serif] justify-end leading-[0] not-italic relative shrink-0 text-[#ffffff] text-[160px] text-center text-nowrap tracking-[-4.8px]">
        <p className="adjustLetterSpacing block leading-[0.75] whitespace-pre">
          24
        </p>
      </div>
      <div className="relative shrink-0 size-12" data-name="°">
        <svg
          className="block size-full"
          fill="none"
          preserveAspectRatio="none"
          viewBox="0 0 48 48"
        >
          <path d={svgPaths.p30e51b00} fill="var(--fill-0, white)" id="Â°" />
        </svg>
      </div>
    </div>
  );
}

function Group2147221290() {
  return (
    <div className="absolute left-0 size-6 top-0">
      <svg
        className="block size-full"
        fill="none"
        preserveAspectRatio="none"
        viewBox="0 0 24 24"
      >
        <g id="Group 2147221290">
          <circle
            cx="12"
            cy="1.5"
            fill="var(--fill-0, white)"
            id="Ellipse 1"
            r="1.5"
          />
          <circle
            cx="5"
            cy="4.5"
            fill="var(--fill-0, white)"
            id="Ellipse 4"
            r="1.5"
          />
          <circle
            cx="20"
            cy="4.5"
            fill="var(--fill-0, white)"
            id="Ellipse 3"
            r="1.5"
          />
          <circle
            cx="22.5"
            cy="12"
            fill="var(--fill-0, white)"
            id="Ellipse 8"
            r="1.5"
          />
          <circle
            cx="1.5"
            cy="12"
            fill="var(--fill-0, white)"
            id="Ellipse 9"
            r="1.5"
          />
          <circle
            cx="1.5"
            cy="1.5"
            fill="var(--fill-0, white)"
            id="Ellipse 5"
            r="1.5"
            transform="matrix(1 0 0 -1 10.5 24)"
          />
          <circle
            cx="1.5"
            cy="1.5"
            fill="var(--fill-0, white)"
            id="Ellipse 6"
            r="1.5"
            transform="matrix(1 0 0 -1 3.5 21)"
          />
          <circle
            cx="1.5"
            cy="1.5"
            fill="var(--fill-0, white)"
            id="Ellipse 7"
            r="1.5"
            transform="matrix(1 0 0 -1 18.5 21)"
          />
          <circle
            cx="12"
            cy="12"
            fill="var(--fill-0, white)"
            id="Ellipse 2"
            r="6"
          />
        </g>
      </svg>
    </div>
  );
}

function Frame2147241502() {
  return (
    <div className="relative shrink-0 size-6">
      <Group2147221290 />
    </div>
  );
}

function Frame2147241498() {
  return (
    <div className="box-border content-stretch flex flex-row gap-2.5 items-center justify-start p-0 relative shrink-0">
      <Frame2147241502 />
      <div className="font-['Geist_Mono:Regular',_sans-serif] font-normal leading-[0] relative shrink-0 text-[#ffffff] text-[26px] text-left text-nowrap tracking-[-0.52px] uppercase">
        <p className="adjustLetterSpacing block leading-none whitespace-pre">
          81°
        </p>
      </div>
      <div className="font-['Geist_Mono:Regular',_sans-serif] font-normal leading-[0] relative shrink-0 text-[#ffffff] text-[26px] text-left tracking-[-0.52px] uppercase w-[78px]">
        <p className="adjustLetterSpacing block leading-none">sunny</p>
      </div>
    </div>
  );
}

function Frame2147241506() {
  return (
    <div className="box-border content-stretch flex flex-col gap-4 items-center justify-start p-0 relative shrink-0 w-full">
      <Frame2147241498 />
    </div>
  );
}

export default function Frame2147241509() {
  return (
    <div className="backdrop-blur-[58.7px] backdrop-filter bg-[rgba(255,255,255,0.24)] relative rounded-[48px] size-full">
      <div className="flex flex-col items-center relative size-full">
        <div className="box-border content-stretch flex flex-col gap-8 items-center justify-start p-[24px] relative size-full">
          <div className="font-['Geist_Mono:Regular',_sans-serif] font-normal leading-[0] relative shrink-0 text-[#ffffff] text-[26px] text-left text-nowrap tracking-[-0.52px] uppercase">
            <p className="adjustLetterSpacing block leading-none whitespace-pre">
              Atlanta, GA
            </p>
          </div>
          <Frame2147241515 />
          <Frame2147241506 />
        </div>
      </div>
    </div>
  );
}