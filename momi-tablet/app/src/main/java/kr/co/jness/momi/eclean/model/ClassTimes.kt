package kr.co.jness.momi.eclean.model

data class ClassTimes(
    /**
     * 요일 정보 (1: 일 ~ 7: 토)
     */
    val day: Int = 0,
    /**
     * 수업 시작 시간 (HHMM)
     */
    val start : String,
    /**
     * 수업 종료 시간 (HHMM)
     */
    val end : String
)