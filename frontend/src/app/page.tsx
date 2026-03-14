"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/contexts/AuthContext";
import { useI18n } from "@/contexts/I18nContext";
import LoginForm from "@/components/LoginForm";

export default function HomePage() {
  const { isAuthenticated } = useAuth();
  const { t, locale, setLocale } = useI18n();
  const router = useRouter();

  useEffect(() => {
    if (isAuthenticated) router.replace("/chat");
  }, [isAuthenticated, router]);

  return (
    <main className="min-h-screen flex items-center justify-center p-4">
      <div className="w-full max-w-sm">
        <div className="flex items-center justify-between mb-4">
          <h1 className="text-2xl font-bold">{t("app.title")}</h1>
          <span className="text-sm text-gray-500">
            <button type="button" onClick={() => setLocale("en")} className={locale === "en" ? "font-medium text-blue-600" : "hover:underline"}>EN</button>
            {" | "}
            <button type="button" onClick={() => setLocale("pt")} className={locale === "pt" ? "font-medium text-blue-600" : "hover:underline"}>PT</button>
          </span>
        </div>
        <LoginForm />
      </div>
    </main>
  );
}
