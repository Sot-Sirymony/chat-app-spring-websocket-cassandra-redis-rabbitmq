"use client";

import React, { createContext, useCallback, useContext, useEffect, useState } from "react";

const API_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";
const TOKEN_KEY = "ebook-chat-token";

type AuthContextValue = {
  token: string | null;
  username: string | null;
  isAdmin: boolean;
  isAuthenticated: boolean;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
  setToken: (t: string | null) => void;
};

const AuthContext = createContext<AuthContextValue | null>(null);

function parseJwtPayload(token: string): { sub?: string; roles?: string } {
  try {
    const base64 = token.split(".")[1];
    if (!base64) return {};
    const json = atob(base64);
    return JSON.parse(json) as { sub?: string; roles?: string };
  } catch {
    return {};
  }
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [token, setTokenState] = useState<string | null>(null);
  const [username, setUsername] = useState<string | null>(null);
  const [isAdmin, setIsAdmin] = useState(false);

  const setToken = useCallback((t: string | null) => {
    setTokenState(t);
    if (t) {
      if (typeof window !== "undefined") localStorage.setItem(TOKEN_KEY, t);
      const payload = parseJwtPayload(t);
      setUsername(payload.sub ?? null);
      const roles = (payload.roles ?? "").split(",").map((r) => r.trim());
      setIsAdmin(roles.some((r) => r === "ROLE_ADMIN"));
    } else {
      if (typeof window !== "undefined") localStorage.removeItem(TOKEN_KEY);
      setUsername(null);
      setIsAdmin(false);
    }
  }, []);

  useEffect(() => {
    if (typeof window === "undefined") return;
    const stored = localStorage.getItem(TOKEN_KEY);
    if (stored) setToken(stored);
  }, [setToken]);

  const login = useCallback(
    async (user: string, password: string) => {
      const url = `${API_URL.replace(/\/$/, "")}/api/auth/token`;
      let res: Response;
      try {
        res = await fetch(url, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ username: user, password }),
        });
      } catch (e) {
        const msg =
          e instanceof TypeError && (e.message === "Failed to fetch" || e.message === "Load failed")
            ? `Cannot reach server at ${API_URL}. Is the backend running? (e.g. mvn spring-boot:run in ebook-chat)`
            : e instanceof Error
              ? e.message
              : "Network error";
        throw new Error(msg);
      }
      const data = await res.json().catch(() => ({}));
      if (!res.ok) {
        throw new Error((data as { error?: string }).error || "Login failed");
      }
      const t = (data as { token?: string }).token;
      if (!t) throw new Error("No token in response");
      setToken(t);
    },
    [setToken]
  );

  const logout = useCallback(() => {
    setToken(null);
  }, [setToken]);

  const value: AuthContextValue = {
    token,
    username,
    isAdmin,
    isAuthenticated: !!token,
    login,
    logout,
    setToken,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}
