package kr.co.jness.momi.eclean.common

/**
 * 시간 Type
 */
enum class UsedTimeStatus(val position: Int) {
    FREE(0), CLASS(1), NIGHT(2)
}

/**
 * 푸시 Type
 */
enum class FirebaseTopic(val topicName: String) {
    NOTICE("notice"), RULE("rule")
}