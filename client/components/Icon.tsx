// Material Symbols 아이콘 (구글 폰트, layout.tsx에서 로드) 통일해서 쓰기 위한 래퍼.
// 이모지(🚫)나 화살표 텍스트(▲, ←) 대신 여기서 아이콘 이름만 넘겨서 씀.
// 아이콘 이름 목록: https://fonts.google.com/icons
export default function Icon({
  name,
  className = "",
  filled = false,
}: {
  name: string;
  className?: string;
  filled?: boolean;
}) {
  return (
    <span
      className={`material-symbols-outlined select-none ${className}`}
      style={filled ? { fontVariationSettings: "'FILL' 1" } : undefined}
      aria-hidden="true"
    >
      {name}
    </span>
  );
}
