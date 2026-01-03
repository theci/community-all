import type { Metadata } from "next";
import "./globals.css";
import ThemeProvider from "@/components/providers/ThemeProvider";

export const metadata: Metadata = {
  title: "Community Platform",
  description: "커뮤니티 플랫폼 - DDD 기반 프로덕션급 애플리케이션",
  keywords: ["커뮤니티", "게시판", "소셜", "플랫폼"],
  authors: [{ name: "DDD3 Team" }],
  openGraph: {
    title: "Community Platform",
    description: "커뮤니티 플랫폼 - DDD 기반 프로덕션급 애플리케이션",
    type: "website",
  },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko">
      <body className="antialiased">
        <ThemeProvider>
          {children}
        </ThemeProvider>
      </body>
    </html>
  );
}
