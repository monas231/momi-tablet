package kr.co.jness.momi.eclean.model

import kr.co.jness.momi.eclean.common.UsedTimeStatus
import kr.co.jness.momi.eclean.utils.MomiUtils

class RuleInfoManager(ruleInfo: RuleInfoVO, todayOfWeek: Int) {

    companion object {
        const val NIGHT_TIME_START = "2200"
        const val NIGHT_TIME_END = "0600"
    }

    /**
     * true : 차단
     */
    val blockVideo = ruleInfo.blockVideo == "1"
    /**
     * true : 차단
     */
    val blockSite = ruleInfo.blockSite == "1"
    /**
     * true : 차단
     */
    val blockProxy = ruleInfo.blockProxy == "1"
    /**
     * true : 차단
     */
    val blockGameSite = ruleInfo.blockGameSite == "1"
    /**
     * true : 차단
     */
    val blockSchoolSite = ruleInfo.blockSchoolSite == "1"
    /**
     * true : 차단
     */
    val blockApp = ruleInfo.blockApp == "1"
    /**
     * true : 차단
     */
    val blockSchoolApp = ruleInfo.blockSchoolApp == "1"
    /**
     * 초
     */
    val syncTime = ruleInfo.syncTime.toLong()
    /**
     * true : 적용
     */
    val useNight = ruleInfo.useNight == "1"
    /**
     * true : 적용
     */
    val useClassTime = ruleInfo.useClassTime == "1"

    val blockExtensions = ruleInfo.blockFileType
        ?.map { it.type } ?: listOf()
    val allowedUrls = ruleInfo.allowInfos
        ?.filter { it.type == 1 }
        ?.map { it.info } ?: listOf()
    val allowedApps = ruleInfo.allowInfos
        ?.filter { it.type == 2 } ?: listOf()
    val orderedClassTimes = ruleInfo.classTimes
        ?.groupBy { it.day }
        ?.toSortedMap(compareBy { it }) ?: mapOf<Int, List<ClassTimes>>()
    val todayClassTimeMillis by lazy {
        orderedClassTimes[todayOfWeek]?.map {
            val startHour = it.start.substring(0, 2).toInt()
            val startMinute = it.start.substring(2, 4).toInt()
            val startTime = MomiUtils.getTimeInMillis(it.day, startHour, startMinute)

            val endHour = it.end.substring(0, 2).toInt()
            val endMinute = it.end.substring(2, 4).toInt()
            val endTime = MomiUtils.getTimeInMillis(it.day, endHour, endMinute)

            Pair(startTime, endTime)
        }
    }
    val nightTimeMillis by lazy {
        val startHour = NIGHT_TIME_START.substring(0, 2).toInt()
        val startMinute = NIGHT_TIME_START.substring(2, 4).toInt()
        val startTime = MomiUtils.getTimeInMillis(todayOfWeek, startHour, startMinute)

        val endHour = NIGHT_TIME_END.substring(0, 2).toInt()
        val endMinute = NIGHT_TIME_END.substring(2, 4).toInt()
        val endTime = MomiUtils.getTimeInMillis(todayOfWeek + 1, endHour, endMinute)

        Pair(startTime, endTime)
    }

    fun getCurrentTimeStatus(): UsedTimeStatus {
        val current = System.currentTimeMillis()
        return when {
            useClassTime && todayClassTimeMillis != null -> {
                val result = todayClassTimeMillis?.any {
                    // 현재시간이 수업 시작시간과 종료시간 사이이면 true.
                    it.first < current && it.second > current
                } ?: false
                if (result) UsedTimeStatus.CLASS else UsedTimeStatus.FREE
            }
            useNight -> {
                if (nightTimeMillis.first < current && nightTimeMillis.second > current) {
                    UsedTimeStatus.NIGHT
                }
                else {
                    UsedTimeStatus.FREE
                }
            }
            else -> UsedTimeStatus.FREE
        }

    }

}