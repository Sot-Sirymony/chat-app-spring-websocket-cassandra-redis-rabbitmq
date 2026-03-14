"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import ProtectedLayout from "@/components/ProtectedLayout";
import { useAuth } from "@/contexts/AuthContext";
import { apiFetch, type ChatRoom } from "@/lib/api";
import { useI18n } from "@/contexts/I18nContext";

export default function ChatListPage() {
  const { token, isAdmin } = useAuth();
  const { t } = useI18n();
  const [rooms, setRooms] = useState<ChatRoom[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [showCreate, setShowCreate] = useState(false);
  const [newName, setNewName] = useState("");
  const [newDesc, setNewDesc] = useState("");

  useEffect(() => {
    if (!token) return;
    apiFetch<ChatRoom[]>("/api/chatrooms", { token })
      .then(setRooms)
      .catch((err) => setError(err instanceof Error ? err.message : "Failed to load rooms"))
      .finally(() => setLoading(false));
  }, [token]);

  async function handleCreateRoom(e: React.FormEvent) {
    e.preventDefault();
    if (!token) return;
    setError("");
    try {
      const url = (process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080").replace(/\/$/, "") + "/chatroom";
      const res = await fetch(url, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          name: newName,
          description: newDesc,
          classification: "PUBLIC",
        }),
      });
      if (!res.ok) throw new Error("Create failed");
      const room = (await res.json()) as ChatRoom;
      setRooms((prev) => [...prev, room]);
      setNewName("");
      setNewDesc("");
      setShowCreate(false);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Create failed");
    }
  }

  return (
    <ProtectedLayout>
      <div className="max-w-4xl mx-auto">
        <h1 className="text-xl font-bold mb-4">{t("chat.available.chatrooms")}</h1>
        {error && (
          <div className="text-red-600 bg-red-50 p-2 rounded mb-4 text-sm">{error}</div>
        )}
        {isAdmin && (
          <div className="mb-4">
            {!showCreate ? (
              <button
                type="button"
                onClick={() => setShowCreate(true)}
                className="bg-blue-600 text-white px-4 py-2 rounded text-sm font-medium hover:bg-blue-700"
              >
                {t("menu.new.chatrooms")}
              </button>
            ) : (
              <form onSubmit={handleCreateRoom} className="flex flex-wrap gap-2 items-end">
                <input
                  type="text"
                  placeholder="Room name"
                  value={newName}
                  onChange={(e) => setNewName(e.target.value)}
                  className="border border-gray-300 rounded px-2 py-1"
                  required
                />
                <input
                  type="text"
                  placeholder="Description"
                  value={newDesc}
                  onChange={(e) => setNewDesc(e.target.value)}
                  className="border border-gray-300 rounded px-2 py-1"
                />
                <button type="submit" className="bg-blue-600 text-white px-3 py-1 rounded text-sm">
                  {t("chat.new.chatroom.create")}
                </button>
                <button
                  type="button"
                  onClick={() => setShowCreate(false)}
                  className="text-gray-600 text-sm"
                >
                  {t("chat.new.chatroom.close")}
                </button>
              </form>
            )}
          </div>
        )}
        {loading ? (
          <p className="text-gray-500">Loading rooms…</p>
        ) : (
          <div className="border border-gray-200 rounded overflow-hidden">
            <table className="w-full text-left">
              <thead className="bg-gray-100">
                <tr>
                  <th className="px-3 py-2 font-medium">{t("chat.chatrooms.name")}</th>
                  <th className="px-3 py-2 font-medium">{t("chat.chatrooms.description")}</th>
                  <th className="px-3 py-2 font-medium">{t("chat.chatrooms.connectedUsers")}</th>
                  <th className="px-3 py-2 font-medium"></th>
                </tr>
              </thead>
              <tbody>
                {rooms.map((room) => (
                  <tr key={room.id} className="border-t border-gray-100">
                    <td className="px-3 py-2">{room.name}</td>
                    <td className="px-3 py-2 text-gray-600">{room.description ?? ""}</td>
                    <td className="px-3 py-2">{room.numberOfConnectedUsers ?? room.connectedUsers?.length ?? 0}</td>
                    <td className="px-3 py-2">
                      <Link
                        href={`/chatroom/${room.id}`}
                        className="text-blue-600 hover:underline text-sm font-medium"
                      >
                        {t("chat.chatrooms.join")}
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </ProtectedLayout>
  );
}
