package com.example.agent.`data`.local

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class ChatDao_Impl(
  __db: RoomDatabase,
) : ChatDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfChatMessage: EntityInsertAdapter<ChatMessage>
  init {
    this.__db = __db
    this.__insertAdapterOfChatMessage = object : EntityInsertAdapter<ChatMessage>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `messages` (`id`,`content`,`isUser`,`timestamp`) VALUES (nullif(?, 0),?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ChatMessage) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.content)
        val _tmp: Int = if (entity.isUser) 1 else 0
        statement.bindLong(3, _tmp.toLong())
        statement.bindLong(4, entity.timestamp)
      }
    }
  }

  public override suspend fun insertMessage(message: ChatMessage): Unit = performSuspending(__db,
      false, true) { _connection ->
    __insertAdapterOfChatMessage.insert(_connection, message)
  }

  public override fun getAllMessages(): Flow<List<ChatMessage>> {
    val _sql: String = "SELECT * FROM messages ORDER BY timestamp ASC"
    return createFlow(__db, false, arrayOf("messages")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfContent: Int = getColumnIndexOrThrow(_stmt, "content")
        val _columnIndexOfIsUser: Int = getColumnIndexOrThrow(_stmt, "isUser")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _result: MutableList<ChatMessage> = mutableListOf()
        while (_stmt.step()) {
          val _item: ChatMessage
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpContent: String
          _tmpContent = _stmt.getText(_columnIndexOfContent)
          val _tmpIsUser: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsUser).toInt()
          _tmpIsUser = _tmp != 0
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          _item = ChatMessage(_tmpId,_tmpContent,_tmpIsUser,_tmpTimestamp)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clearAll() {
    val _sql: String = "DELETE FROM messages"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
