package kr.co.jness.momi.eclean.database

import androidx.room.*
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import kr.co.jness.momi.eclean.model.*

@Dao
abstract class WordFilterDao {

    /**
     * 모든 데이터를 저장한다.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertAllWordFilter(wordFilter: List<WordFilterVO>) : Completable

    /**
     * 모든 데이터를 지운다.
     */
    @Query("DELETE FROM word_filter")
    abstract fun deleteWordFilter() : Completable


    /**
     * 모든 데이터를 가져온다.
     */
    @Query("SELECT * FROM word_filter")
    abstract fun loadAllWordFilter(): Single<List<WordFilterVO>>

    /**
     * version 이하의 filepath항목이 있는지 확인한다.
     */
    @Query("SELECT COUNT(id) FROM word_filter WHERE filepath = :filepath and version < :version LIMIT 1")
    abstract fun getOldCountByPathVersion(filepath: String, version : Int): Single<Int>

    /**
     * version 이하의 filepath항목을 삭제한다.
     */
    @Query("DELETE FROM word_filter WHERE filepath = :filepath and version < :version")
    abstract fun deleteOldByPath(filepath: String, version : Int): Completable

    /**
     * 저장된 값이 있는지 확인한다.
     */
    @Query("SELECT COUNT(id) FROM word_filter WHERE version = :version LIMIT 1")
    abstract fun existData(version : Int): Single<Int>

}