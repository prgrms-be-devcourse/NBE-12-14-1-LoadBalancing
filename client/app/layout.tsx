import type { Metadata } from "next";
import { Inter, Geist_Mono } from "next/font/google";
import "./globals.css";
import { CartProvider } from "@/context/CartContext";
import CartBar from "@/components/CartBar";

// 디자인(zz/monochrome_kiosk) 기준 폰트. weight는 개별 요소에서 font-bold 등으로 지정
const inter = Inter({
  variable: "--font-inter",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "Kiosk",
  description: "카페 무인 주문 웹",
};

export default function RootLayout({ children }: LayoutProps<"/">) {
  return (
    <html
      lang="ko"
      className={`${inter.variable} ${geistMono.variable} h-full antialiased`}
    >
      <body className="min-h-full flex flex-col font-sans">
        {/* React 19 스타일시트 리소스: precedence를 줘야 렌더 위치 상관없이 <head>로 자동 호이스팅/중복제거됨.
            <html> 바로 아래(=<body> 밖)에 두면 잘못된 HTML 중첩이라 hydration 에러가 나서 <body> 안으로 옮김.
            이 파일이 App Router 루트 레이아웃이라 모든 라우트에 공통 적용됨 -
            eslint-plugin-next의 no-page-custom-font는 Pages Router의 pages/_document.js 전용 룰이라 여기선 오탐임 */}
        {/* eslint-disable-next-line @next/next/no-page-custom-font */}
        <link
          href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200&display=swap"
          rel="stylesheet"
          precedence="default"
        />
        <CartProvider>
          {children}
          <CartBar />
        </CartProvider>
      </body>
    </html>
  );
}
