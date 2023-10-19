package com.bytebyte6.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bytebyte6.data.dao.*
import com.bytebyte6.data.entity.*

@Database(
    entities = [
        Tv::class,
        TvFts::class,
        Language::class,
        Country::class,
        Category::class,
        Playlist::class,
        PlaylistTvCrossRef::class,
        User::class,
        UserPlaylistCrossRef::class
    ], version = 1, exportSchema = true
)
@TypeConverters(TypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tvDao(): TvDao
    abstract fun languageDao(): LanguageDao
    abstract fun categoryDao(): CategoryDao
    abstract fun countryDao(): CountryDao
    abstract fun tvFtsDao(): TvFtsDao
    abstract fun userDao(): UserDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun playlistTvCrossRefDao(): PlaylistTvCrossRefDao
    abstract fun userPlaylistCrossRefDao(): UserPlaylistCrossRefDao
}

