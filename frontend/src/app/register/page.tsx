"use client";

import { useState } from "react";
import Link from "next/link";
import { getApiUrl } from "@/lib/api";
import { useI18n } from "@/contexts/I18nContext";

export default function RegisterPage() {
  const { t } = useI18n();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [confirm, setConfirm] = useState("");
  const [error, setError] = useState("");
  const [done, setDone] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError("");
    if (password !== confirm) {
      setError("Passwords do not match");
      return;
    }
    if (password.length < 5) {
      setError("Password must be at least 5 characters");
      return;
    }
    if (username.length < 5 || username.length > 15) {
      setError("Username must be 5–15 characters");
      return;
    }
    try {
      const url = getApiUrl("/api/auth/register");
      const res = await fetch(url, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password }),
      });
      const data = await res.json().catch(() => ({}));
      if (res.ok || res.status === 201) {
        setDone(true);
        return;
      }
      setError((data as { error?: string }).error || `Registration failed (${res.status})`);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Registration failed");
    }
  }

  if (done) {
    return (
      <main className="min-h-screen flex items-center justify-center p-4">
        <div className="text-center">
          <p className="text-green-600 font-medium">Account created. You can sign in now.</p>
          <Link href="/" className="text-blue-600 hover:underline mt-2 inline-block">
            {t("general.back_to_login")}
          </Link>
        </div>
      </main>
    );
  }

  return (
    <main className="min-h-screen flex items-center justify-center p-4">
      <div className="w-full max-w-sm">
        <h1 className="text-2xl font-bold text-center mb-6">{t("new.account.title")}</h1>
        <form onSubmit={handleSubmit} className="space-y-4">
          {error && (
            <div className="text-red-600 text-sm bg-red-50 p-2 rounded">{error}</div>
          )}
          <div>
            <label htmlFor="username" className="block text-sm font-medium text-gray-700 mb-1">
              {t("new.account.your.username")}
            </label>
            <input
              id="username"
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              className="w-full border border-gray-300 rounded px-3 py-2"
              required
              minLength={5}
              maxLength={15}
            />
          </div>
          <div>
            <label htmlFor="password" className="block text-sm font-medium text-gray-700 mb-1">
              {t("new.account.your.password")}
            </label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full border border-gray-300 rounded px-3 py-2"
              required
              minLength={5}
            />
          </div>
          <div>
            <label htmlFor="confirm" className="block text-sm font-medium text-gray-700 mb-1">
              Confirm password
            </label>
            <input
              id="confirm"
              type="password"
              value={confirm}
              onChange={(e) => setConfirm(e.target.value)}
              className="w-full border border-gray-300 rounded px-3 py-2"
              required
            />
          </div>
          <button
            type="submit"
            className="w-full bg-blue-600 text-white py-2 rounded font-medium hover:bg-blue-700"
          >
            {t("new.account.create")}
          </button>
          <p className="text-center text-sm text-gray-500">
            <Link href="/" className="text-blue-600 hover:underline">
              {t("login.signin")}
            </Link>
          </p>
        </form>
      </div>
    </main>
  );
}
