package kr.co.jness.momi.eclean.database

import androidx.lifecycle.LiveData
import androidx.room.*
import kr.co.jness.momi.eclean.model.RuleInfoVO

@Dao
abstract class RuleInfoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertRuleInfo(info: List<RuleInfoVO>)

    @Query("SELECT * FROM rule_info LIMIT 1")
    abstract fun loadRuleInfo(): LiveData<RuleInfoVO?>

    @Query("DELETE FROM rule_info")
    abstract suspend fun deleteRuleInfo()

    @Transaction
    open suspend fun setRuleInfo(info: List<RuleInfoVO>) {
        deleteRuleInfo()
        insertRuleInfo(info)
    }

}