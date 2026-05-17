package com.lubriciel.bigarrer.service

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.lubriciel.bigarrer.data.SavedCubeLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SqliteLocationService(context: Context) : LocationStorageService {

    private val dbHelper = DbHelper(context)

    override suspend fun save(location: SavedCubeLocation): Long = withContext(Dispatchers.IO) {
        dbHelper.writableDatabase.insert(TABLE, null, ContentValues().apply {
            put(COL_LAT, location.latitude)
            put(COL_LNG, location.longitude)
            put(COL_ALT, location.altitude)
            put(COL_QUAT_X, location.quatX)
            put(COL_QUAT_Y, location.quatY)
            put(COL_QUAT_Z, location.quatZ)
            put(COL_QUAT_W, location.quatW)
            put(COL_TS, location.timestamp)
        })
    }

    override suspend fun getAll(): List<SavedCubeLocation> = withContext(Dispatchers.IO) {
        val cursor = dbHelper.readableDatabase.query(
            TABLE, null, null, null, null, null, "$COL_TS ASC"
        )
        buildList {
            cursor.use { c ->
                while (c.moveToNext()) {
                    add(
                        SavedCubeLocation(
                            id        = c.getLong(c.getColumnIndexOrThrow(COL_ID)),
                            latitude  = c.getDouble(c.getColumnIndexOrThrow(COL_LAT)),
                            longitude = c.getDouble(c.getColumnIndexOrThrow(COL_LNG)),
                            altitude  = c.getDouble(c.getColumnIndexOrThrow(COL_ALT)),
                            quatX     = c.getFloat(c.getColumnIndexOrThrow(COL_QUAT_X)),
                            quatY     = c.getFloat(c.getColumnIndexOrThrow(COL_QUAT_Y)),
                            quatZ     = c.getFloat(c.getColumnIndexOrThrow(COL_QUAT_Z)),
                            quatW     = c.getFloat(c.getColumnIndexOrThrow(COL_QUAT_W)),
                            timestamp = c.getLong(c.getColumnIndexOrThrow(COL_TS)),
                        )
                    )
                }
            }
        }
    }

    override suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        dbHelper.writableDatabase.delete(TABLE, "$COL_ID = ?", arrayOf(id.toString()))
        Unit
    }

    private class DbHelper(context: Context) :
        SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE $TABLE (
                    $COL_ID     INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COL_LAT    REAL    NOT NULL,
                    $COL_LNG    REAL    NOT NULL,
                    $COL_ALT    REAL    NOT NULL,
                    $COL_QUAT_X REAL    NOT NULL,
                    $COL_QUAT_Y REAL    NOT NULL,
                    $COL_QUAT_Z REAL    NOT NULL,
                    $COL_QUAT_W REAL    NOT NULL,
                    $COL_TS     INTEGER NOT NULL
                )
                """.trimIndent()
            )
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL("DROP TABLE IF EXISTS $TABLE")
            onCreate(db)
        }
    }

    private companion object {
        const val DB_NAME   = "cube_locations.db"
        const val DB_VERSION = 1
        const val TABLE     = "cube_locations"
        const val COL_ID    = "id"
        const val COL_LAT   = "latitude"
        const val COL_LNG   = "longitude"
        const val COL_ALT   = "altitude"
        const val COL_QUAT_X = "quat_x"
        const val COL_QUAT_Y = "quat_y"
        const val COL_QUAT_Z = "quat_z"
        const val COL_QUAT_W = "quat_w"
        const val COL_TS    = "timestamp"
    }
}
