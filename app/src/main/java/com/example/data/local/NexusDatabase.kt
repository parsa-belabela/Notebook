package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.NexusNoteDao
import com.example.data.local.entity.FolderEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.NoteLinkEntity
import com.example.data.local.entity.NoteTagCrossRef
import com.example.data.local.entity.NoteVersionEntity
import com.example.data.local.entity.TagEntity

@Database(
    entities = [
        NoteEntity::class,
        FolderEntity::class,
        TagEntity::class,
        NoteTagCrossRef::class,
        NoteLinkEntity::class,
        NoteVersionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class NexusDatabase : RoomDatabase() {

    abstract fun noteDao(): NexusNoteDao

    companion object {
        @Volatile
        private var INSTANCE: NexusDatabase? = null

        fun getDatabase(context: Context): NexusDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NexusDatabase::class.java,
                    "nexus_note_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
