"use client";

import React, { createContext, useCallback, useContext, useEffect, useState } from "react";
import en from "@/messages/en.json";
import pt from "@/messages/pt.json";

export type Locale = "en" | "pt";

const LOCALE_KEY = "ebook-chat-locale";

const messages: Record<Locale, Record<string, string>> = { en, pt };

type I18nContextValue = {
  locale: Locale;
  setLocale: (l: Locale) => void;
  t: (key: string) => string;
};

const I18nContext = createContext<I18nContextValue | null>(null);

export function I18nProvider({ children }: { children: React.ReactNode }) {
  const [locale, setLocaleState] = useState<Locale>("en");

  useEffect(() => {
    const stored = (typeof window !== "undefined" && localStorage.getItem(LOCALE_KEY)) as Locale | null;
    if (stored === "en" || stored === "pt") setLocaleState(stored);
  }, []);

  const setLocale = useCallback((l: Locale) => {
    setLocaleState(l);
    if (typeof window !== "undefined") localStorage.setItem(LOCALE_KEY, l);
  }, []);

  const t = useCallback((key: string) => messages[locale][key] ?? key, [locale]);

  return (
    <I18nContext.Provider value={{ locale, setLocale, t }}>
      {children}
    </I18nContext.Provider>
  );
}

export function useI18n(): I18nContextValue {
  const ctx = useContext(I18nContext);
  if (!ctx) throw new Error("useI18n must be used within I18nProvider");
  return ctx;
}
