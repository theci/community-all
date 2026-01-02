import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "DDD3 Admin",
  description: "DDD3 관리자 페이지",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko">
      <body>{children}</body>
    </html>
  );
}
