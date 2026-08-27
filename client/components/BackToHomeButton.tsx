import Link from "next/link";

export default function BackToHomeButton() {
  return (
    <Link
      href="/"
      className="mb-4 inline-flex items-center gap-1 text-sm text-gray-500 hover:text-black"
    >
      ← 처음으로
    </Link>
  );
}
