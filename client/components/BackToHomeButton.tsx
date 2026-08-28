import Link from "next/link";
import Icon from "@/components/Icon";

export default function BackToHomeButton() {
  return (
    <Link
      href="/"
      className="mb-4 inline-flex items-center gap-1 text-label-sm font-medium uppercase tracking-wide text-gray-500 hover:text-black"
    >
      <Icon name="arrow_back" className="text-base" />
      처음으로
    </Link>
  );
}
