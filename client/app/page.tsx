import Link from "next/link";

export default function Home() {
  return (
    <Link
      href="/menu"
      className="flex h-screen w-screen items-center justify-center bg-black text-white"
    >
      <span className="text-5xl font-bold">눌르셈 ㅋ</span>
    </Link>
  );
}
