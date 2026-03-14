"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/contexts/AuthContext";
import { setOn401 } from "@/lib/authCallbacks";

/**
 * Registers global 401 handler: logout and redirect to login.
 * Rendered once inside AuthProvider so API layer can trigger redirect on Unauthorized.
 */
export default function AuthRedirectHandler() {
  const { logout } = useAuth();
  const router = useRouter();

  useEffect(() => {
    setOn401(() => {
      logout();
      router.replace("/");
    });
    return () => setOn401(null);
  }, [logout, router]);

  return null;
}
