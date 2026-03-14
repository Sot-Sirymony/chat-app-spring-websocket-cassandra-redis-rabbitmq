"use client";

import { useEffect, useState } from "react";
import ProtectedLayout from "@/components/ProtectedLayout";
import { useAuth } from "@/contexts/AuthContext";
import {
  apiFetch,
  type RiskyUserEntry,
  type RiskyRoomEntry,
  type AlertStatus,
} from "@/lib/api";

export default function AnalyticsPage() {
  const { token, isAdmin } = useAuth();
  const [riskyUsers, setRiskyUsers] = useState<RiskyUserEntry[]>([]);
  const [riskyRooms, setRiskyRooms] = useState<RiskyRoomEntry[]>([]);
  const [alertStatus, setAlertStatus] = useState<AlertStatus | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!token || !isAdmin) {
      setLoading(false);
      return;
    }
    setLoading(true);
    setError("");
    const base = { token };
    Promise.all([
      apiFetch<RiskyUserEntry[]>("/api/analytics/risky-users?limit=20", base),
      apiFetch<RiskyRoomEntry[]>("/api/analytics/risky-rooms?limit=20", base),
      apiFetch<AlertStatus>("/api/analytics/alert-status", base),
    ])
      .then(([users, rooms, alert]) => {
        setRiskyUsers(users);
        setRiskyRooms(rooms);
        setAlertStatus(alert);
      })
      .catch((err) =>
        setError(err instanceof Error ? err.message : "Failed to load analytics")
      )
      .finally(() => setLoading(false));
  }, [token, isAdmin]);

  if (!isAdmin) {
    return (
      <ProtectedLayout>
        <div className="max-w-4xl mx-auto">
          <p className="text-red-600 font-medium">
            Access denied. Analytics is available only to administrators.
          </p>
        </div>
      </ProtectedLayout>
    );
  }

  return (
    <ProtectedLayout>
      <div className="max-w-4xl mx-auto space-y-6">
        <h1 className="text-xl font-bold">Analytics</h1>
        {error && (
          <div className="text-red-600 bg-red-50 p-2 rounded text-sm">
            {error}
          </div>
        )}

        {loading ? (
          <p className="text-gray-500">Loading…</p>
        ) : (
          <>
            {alertStatus != null && (
              <section className="border border-gray-200 rounded p-4">
                <h2 className="text-lg font-semibold mb-2">Alert status</h2>
                <div className="flex flex-wrap gap-4 text-sm">
                  <span>
                    Approval backlog:{" "}
                    <strong>{alertStatus.approvalBacklog}</strong>
                  </span>
                  {alertStatus.approvalBacklogAlert && (
                    <span className="text-amber-600 font-medium">
                      Backlog alert (&gt;20 pending)
                    </span>
                  )}
                </div>
              </section>
            )}

            <section>
              <h2 className="text-lg font-semibold mb-2">Risky users</h2>
              <div className="border border-gray-200 rounded overflow-hidden">
                {riskyUsers.length === 0 ? (
                  <p className="px-3 py-4 text-gray-500 text-sm">
                    No risky user data.
                  </p>
                ) : (
                  <table className="w-full text-left text-sm">
                    <thead className="bg-gray-100">
                      <tr>
                        <th className="px-3 py-2">Username</th>
                        <th className="px-3 py-2">Deny count</th>
                      </tr>
                    </thead>
                    <tbody>
                      {riskyUsers.map((u) => (
                        <tr key={u.username} className="border-t border-gray-100">
                          <td className="px-3 py-2">{u.username}</td>
                          <td className="px-3 py-2">{u.denyCount}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                )}
              </div>
            </section>

            <section>
              <h2 className="text-lg font-semibold mb-2">Risky rooms</h2>
              <div className="border border-gray-200 rounded overflow-hidden">
                {riskyRooms.length === 0 ? (
                  <p className="px-3 py-4 text-gray-500 text-sm">
                    No risky room data.
                  </p>
                ) : (
                  <table className="w-full text-left text-sm">
                    <thead className="bg-gray-100">
                      <tr>
                        <th className="px-3 py-2">Room ID</th>
                        <th className="px-3 py-2">Deny count</th>
                      </tr>
                    </thead>
                    <tbody>
                      {riskyRooms.map((r) => (
                        <tr key={r.roomId} className="border-t border-gray-100">
                          <td className="px-3 py-2">{r.roomId}</td>
                          <td className="px-3 py-2">{r.denyCount}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                )}
              </div>
            </section>
          </>
        )}
      </div>
    </ProtectedLayout>
  );
}
