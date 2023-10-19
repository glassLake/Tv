package com.bytebyte6.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.bytebyte6.data.PAGE_SIZE
import com.bytebyte6.data.entity.Tv
import com.bytebyte6.data.model.TvWithPlaylists

@Dao
interface TvDao : BaseDao<Tv> {
    @Transaction
    @Query("SELECT * FROM Tv")
    fun getTvWithPlaylistss(): List<TvWithPlaylists>

    @Query("SELECT * FROM Tv LIMIT :pageSize OFFSET :offset")
    fun paging(offset: Int, pageSize: Int = PAGE_SIZE): List<Tv>

    @Query("SELECT COUNT(tvId) FROM Tv")
    fun getCount(): Int

    @Query("SELECT * FROM Tv WHERE logo =:logo")
    fun getTvsByLogo(logo: String): List<Tv>

    @Query("SELECT * FROM Tv")
    fun getTvs(): List<Tv>

    @Query("SELECT * FROM Tv WHERE tvId =:id")
    fun getTv(id: Long): Tv

    @Query("SELECT * FROM Tv WHERE url =:url")
    fun getTvByUrl(url: String): Tv?

    @Query("SELECT * FROM Tv WHERE favorite = 1 ")
    fun allFavorite(): LiveData<List<Tv>>
}