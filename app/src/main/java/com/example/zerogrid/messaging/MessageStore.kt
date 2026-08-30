package com.example.zerogrid.messaging

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

/**
 * Persistent message store for ZeroGrid DM conversations.
 * Keyed by peer Node ID. Backed by SharedPreferences as JSON arrays.
 * Messages survive app restarts and mesh reconnections.
 */
class MessageStore private constructor(context: Context) {

    companion object {
        private const val TAG = "MessageStore"
        private const val PREFS_NAME = "zerogrid_messages"
        private const val CONV_KEY_PREFIX = "conv_"
        private const val MAX_MESSAGES_PER_PEER = 200

        @Volatile
        private var INSTANCE: MessageStore? = null

        fun getInstance(context: Context): MessageStore {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MessageStore(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Load all stored messages for a specific peer. */
    fun getConversation(peerId: String): List<StoredMessage> {
        return try {
            val json = prefs.getString("$CONV_KEY_PREFIX$peerId", null) ?: return emptyList()
            val arr = JSONArray(json)
            (0 until arr.length()).mapNotNull { i ->
                try {
                    val obj = arr.getJSONObject(i)
                    StoredMessage(
                        id = obj.optString("id"),
                        senderId = obj.optString("senderId"),
                        recipientId = obj.optString("recipientId"),
                        text = obj.optString("text"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        hopCount = obj.optInt("hopCount", 0),
                        isMine = obj.optBoolean("isMine", false)
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading conversation for $peerId", e)
            emptyList()
        }
    }

    /** Append a new message to a peer's conversation, persisting to disk. */
    fun appendMessage(peerId: String, msg: StoredMessage) {
        try {
            val current = getConversation(peerId).toMutableList()
            // Avoid exact duplicate (same id)
            if (current.any { it.id == msg.id }) return
            current.add(msg)
            // Prune oldest if over limit
            val pruned = if (current.size > MAX_MESSAGES_PER_PEER) {
                current.takeLast(MAX_MESSAGES_PER_PEER)
            } else current
            val arr = JSONArray()
            pruned.forEach { m ->
                arr.put(JSONObject().apply {
                    put("id", m.id)
                    put("senderId", m.senderId)
                    put("recipientId", m.recipientId)
                    put("text", m.text)
                    put("timestamp", m.timestamp)
                    put("hopCount", m.hopCount)
                    put("isMine", m.isMine)
                })
            }
            prefs.edit().putString("$CONV_KEY_PREFIX$peerId", arr.toString()).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error appending message for $peerId", e)
        }
    }

    /**
     * Load all peer IDs that have at least one stored message.
     * Returns them ordered by the timestamp of the most recent message (newest first).
     */
    fun getAllConversationPeerIds(): List<String> {
        return try {
            prefs.all.keys
                .filter { it.startsWith(CONV_KEY_PREFIX) }
                .mapNotNull { key ->
                    val peerId = key.removePrefix(CONV_KEY_PREFIX)
                    val conv = getConversation(peerId)
                    if (conv.isEmpty()) null else Pair(peerId, conv.last().timestamp)
                }
                .sortedByDescending { it.second }
                .map { it.first }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading all conversation peer IDs", e)
            emptyList()
        }
    }

    /** Get last message for a peer (for preview in DM list). */
    fun getLastMessage(peerId: String): StoredMessage? = getConversation(peerId).lastOrNull()
}

data class StoredMessage(
    val id: String,
    val senderId: String,
    val recipientId: String,
    val text: String,
    val timestamp: Long,
    val hopCount: Int,
    val isMine: Boolean
)
