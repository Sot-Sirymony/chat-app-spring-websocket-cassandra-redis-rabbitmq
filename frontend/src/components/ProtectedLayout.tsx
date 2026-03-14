"use client";

import { useEffect, useState } from "react";
import { useRouter, usePathname } from "next/navigation";
import Link from "next/link";
import { useAuth } from "@/contexts/AuthContext";
import { useI18n } from "@/contexts/I18nContext";

export default function ProtectedLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const { isAuthenticated, username, logout, isAdmin } = useAuth();
  const { t, locale, setLocale } = useI18n();
  const [langOpen, setLangOpen] = useState(false);
  const router = useRouter();
  const pathname = usePathname();

  useEffect(() => {
    if (typeof window === "undefined") return;
    if (!isAuthenticated) {
      router.replace("/?redirect=" + encodeURIComponent(pathname || "/chat"));
    }
  }, [isAuthenticated, router, pathname]);

  if (!isAuthenticated) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <p className="text-gray-500">Redirecting to login…</p>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex flex-col">
      <header className="bg-white border-b border-gray-200 px-4 py-2 flex items-center justify-between">
        <nav className="flex items-center gap-4">
          <Link href="/chat" className="text-blue-600 hover:underline font-medium">
            {t("menu.chatrooms")}
          </Link>
          <Link href="/approvals" className="text-blue-600 hover:underline font-medium">
            {t("approvals.title")}
          </Link>
          {isAdmin && (
            <Link href="/analytics" className="text-blue-600 hover:underline font-medium">
              {t("analytics.title")}
            </Link>
          )}
        </nav>
        <div className="flex items-center gap-3">
          <div className="relative">
            <button
              type="button"
              onClick={() => setLangOpen((o: boolean) => !o)}
              className="text-sm text-gray-600 hover:text-gray-800"
            >
              {t("menu.language")}: {locale === "en" ? t("menu.language.english") : t("menu.language.portuguese")}
            </button>
            {langOpen && (
              <>
                <div className="fixed inset-0 z-10" onClick={() => setLangOpen(false)} aria-hidden />
                <div className="absolute right-0 top-full mt-1 bg-white border rounded shadow z-20 py-1 min-w-[120px]">
                  <button type="button" onClick={() => { setLocale("en"); setLangOpen(false); }} className="block w-full text-left px-3 py-1 text-sm hover:bg-gray-100">
                    {t("menu.language.english")}
                  </button>
                  <button type="button" onClick={() => { setLocale("pt"); setLangOpen(false); }} className="block w-full text-left px-3 py-1 text-sm hover:bg-gray-100">
                    {t("menu.language.portuguese")}
                  </button>
                </div>
              </>
            )}
          </div>
          <span className="text-sm text-gray-600">{username}</span>
          <button type="button" onClick={logout} className="text-sm text-gray-500 hover:text-gray-700">
            {t("menu.leave.chatroom")}
          </button>
        </div>
      </header>
      <main className="flex-1 p-4">{children}</main>
    </div>
  );
}
