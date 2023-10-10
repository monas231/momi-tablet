package kr.co.jness.momi.eclean.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kr.co.jness.momi.eclean.model.*

// fixme 출시버전에서는 version = 1 로 수정할 것.
@Database(entities = [ApiCacheVO::class, RuleInfoVO::class, VideoHashVO::class, LocalVideoHashVO::class, WhitelistVO::class, WordFilterVO::class, LocalWhitelistVO::class], version = 7, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    companion object {
        const val DATABASE_NAME = "mom_i_eclean.db"
    }

    abstract fun apiCacheDao(): ApiCacheDao
    abstract fun ruleInfoDao(): RuleInfoDao
    abstract fun videoHashDao(): VideoHashDao
    abstract fun localVideoHashDao(): LocalVideoHashDao
    abstract fun whitelistDao(): WhitelistDao
    abstract fun wordFilterDao(): WordFilterDao
    abstract fun localWhitelistDao(): LocalWhitelistDao

}