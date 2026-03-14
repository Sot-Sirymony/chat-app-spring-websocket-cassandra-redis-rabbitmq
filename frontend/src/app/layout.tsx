import type { Metadata } from "next";
import "./globals.css";
import { AuthProvider } from "@/contexts/AuthContext";
import { I18nProvider } from "@/contexts/I18nContext";
import AuthRedirectHandler from "@/components/AuthRedirectHandler";

export const metadata: Metadata = {
  title: "Ebook Chat",
  description: "Chat application",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body className="antialiased min-h-screen bg-gray-50">
        <AuthProvider>
          <I18nProvider>
            <AuthRedirectHandler />
            {children}
          </I18nProvider>
        </AuthProvider>
      </body>
    </html>
  );
}
