package kr.co.jness.momi.eclean.api

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import kr.co.jness.momi.eclean.dto.*
import kr.co.jness.momi.eclean.model.*
import okhttp3.ResponseBody
import retrofit2.http.*

interface EcleanApi {

    /**
     * 앱 설치
     */
    @FormUrlEncoded
    @POST("/ProgramService/install_app.jns")
    fun notifyInstallApp(@Field("device_serial") device_serial: String, @Field("version") version: String): Single<AppInstallDto>


    /**
     * 정책정보
     */
    @FormUrlEncoded
    @POST("/ProgramService/rule_info.jns")
    fun requestRuleInfo(@Field("device_id") device_id: Long, @Field("school_id") school_id: Int?): Single<RuleInfoDto>

    /**
     * Ping
     */
    @FormUrlEncoded
    @POST("/ProgramService/ping.jns")
    fun sendPing(
        @Field("device_id") device_id: Long,
        @Field("version") version: String
    ): Single<PingDto>

    /**
     * 차단 동영상
     * index 값은 0 부터 시작하는 것으로 추정.
     */
//    @FormUrlEncoded
//    @POST("/ProgramService/check_video_info.jns")
//    fun checkVideo(
//        @Field("video_update_index") video_update_index: Int
//    ): Single<BlockVideoDto>

    /**
     * url의 파일을 받아온다.
     */
    @GET
    fun getFileDownload(@Url url: String): Single<ResponseBody>

    /**
     * FCM TOKEN 저장
     */
    @FormUrlEncoded
    @POST("/ProgramService/reg_fcm.jns")
    fun registerFCMToken(
        @Field("device_id") device_id: Long,
        @Field("school_id") school_id: Int?,
        @Field("token") token: String
    ): Single<BaseDto>

    /**
     * FCM 발송 테스트
     */
    @FormUrlEncoded
    @POST("/ProgramService/send_fcm.jns")
    fun testFCM(
        @Field("title") title: String,
        @Field("msg") msg: String,
        /**
         * 송신 topic. token 으로 보낼 경우 공백.
         */
        @Field("topic") topic: String? = null,
        /**
         * 수신 device token. topic 으로 보낼 경우 공백.
         */
        @Field("token") token: String? = null
    ): Completable

    /**
     * 집중시간 차단 이력 전송
     * device_id: 디바이스 ID
     * school_id: 학교 ID
     * block_info: 차단 정보 (ex> 어플 패키지명, 사이트(도메인) 등)
     * ymd: 차단일정보 (YYYYMMDD)
     */
    @FormUrlEncoded
    @POST("/ProgramService/block_time.jns")
    fun blockTime(
        @Field("device_id") deviceId: Long,
        @Field("school_id") schoolId: Int?,
        @Field("block_info") blockInfo: String,
        @Field("ymd") ymd: String
    ): Single<BaseResponseDto<BlockInfo>>

    /**
     * 파일 확장자 차단 로그 전송
     * device_id: 디바이스 ID
     * school_id: 학교 ID
     * block_info: 차단 된 파일 확장자 (ex> pdf, avi 등)
     * ymd: 차단일정보 (YYYYMMDD)
     */
    @FormUrlEncoded
    @POST("/ProgramService/block_file.jns")
    fun blockFile(
        @Field("device_id") deviceId: Long,
        @Field("school_id") schoolId: Int?,
        @Field("block_info") blockInfo: String,
        @Field("ymd") ymd: String
    ): Single<BaseResponseDto<BlockInfo>>

    /**
     * 동영상 차단 HASH 확인
     * device_id: 디바이스 ID
     * school_id: 학교 ID
     * block_info: 차단 된 파일 확장자 (ex> pdf, avi 등)
     * ymd: 차단일정보 (YYYYMMDD)
     */
    @FormUrlEncoded
    @POST("/ProgramService/check_video.jns")
    fun checkVideo(
        @Field("device_id") deviceId: Long,
        @Field("school_id") schoolId: Int?,
        @Field("hash") hash: String,
        @Field("ymd") ymd: String
    ): Single<BaseResponseDto<BlockInfo>>

    /**
     * 차단사이트
     */
    @FormUrlEncoded
    @POST("/ProgramService/check_url.jns")
    fun checkUrl(
        @Field("device_id") device_id: Long,
        @Field("school_id") school_id: Int?,
        @Field("domain") domain: String,
        @Field("enc_domain") enc_domain: String,
        @Field("sub_dir") sub_dir: String?,
        @Field("all") all: String,
        @Field("ymd") ymd: String
    ): Single<BaseResponseDto<BlockInfo>>

    /**
     * 차단 어플리케이션
     */
    @FormUrlEncoded
    @POST("/ProgramService/check_app.jns")
    fun checkApp(
        @Field("device_id") device_id: Long,
        @Field("school_id") school_id: Int?,
        @Field("package") pkg: String,
        @Field("ymd") ymd: String
    ): Single<BaseResponseDto<BlockInfo>>

    /**
     * 공지사항
     * school_id: 학교 ID
     */
    @FormUrlEncoded
    @POST("/WebService/notice.jns")
    fun getNotice(
        @Field("school_id") schoolId: Int?
    ): Single<BaseResponseDto<NoticeVO>>


    /**
     * 라이선스 체크
     * device_id: 디바이스 ID
     * school_id: 학교 ID크
     * key: license key
     */
    @FormUrlEncoded
    @POST("/ProgramService/check_licence.jns")
    fun checkLicense(
        @Field("device_id") device_id: Long,
        @Field("key") key: String,
        @Field("s_id") s_id: Long?,
        @Field("s_nm") s_nm: String?,
        @Field("mail") mail: String?,
        @Field("phone") phone: String?
    ): Single<BaseResponseDto<AppLicense>>

    /**
     * 공지사항
     * device_id: 디바이스 ID
     * school_id: 학교 ID크
     */
    @FormUrlEncoded
    @POST("/ProgramService/notice_info.jns")
    fun noticeInfo(
        @Field("device_id") device_id: Long,
        @Field("school_id") schoolId: Int?
    ): Single<BaseResponseDto<NoticeInfoVO>>

    /**
     * 파일 업데이트
     * device_id: 디바이스 ID
     * school_id: 학교 ID크
     */
    @FormUrlEncoded
    @POST("/ProgramService/check_file.jns")
    fun checkFile(
        @Field("device_id") device_id: Long,
        @Field("school_id") schoolId: Int?
    ): Single<BaseResponseDto<CheckFile>>

    /**
     * 키워드 차단 정보
     * device_id: 디바이스 ID
     * school_id: 학교 ID크
     */
    @FormUrlEncoded
    @POST("/ProgramService/keyword_info.jns")
    fun checkKeyword(
        @Field("device_id") device_id: Long,
        @Field("school_id") schoolId: Int?
    ): Single<BaseResponseDto<KeyWordDto>>

    /**
     * 학교 검색
     * device_id: 디바이스 ID
     * name: 검색할 이름
     */
    @FormUrlEncoded
    @POST("/ProgramService/search_school.jns")
    fun searchSchool(
        @Field("device_id") device_id: Long,
        @Field("name") name: String
    ): Single<BaseResponseDto<SchoolDto>>


    /**
     * 문의하기
     * device_id: 디바이스 ID
     * name: 고객이름
     * type: 고객구분 (1:교사, 2:학부모, 3:학생, 4:기타)
     * mail: 고객 메일
     * phone: 고객 연락처
     * q_type: 문의 항목 (1:맘아이구매, 2:조달청, 3:기타)
     * text: 문의내용
     */
    @FormUrlEncoded
    @POST("/ProgramService/question.jns")
    fun sendQuestion(
        @Field("device_id") device_id: Long,
        @Field("name") name: String?,
        @Field("type") type: Int?,
        @Field("mail") mail: String?,
        @Field("phone") phone: String?,
        @Field("q_type") q_type: Int?,
        @Field("text") text: String?
    ): Single<BaseResponseDto<Void>>

}