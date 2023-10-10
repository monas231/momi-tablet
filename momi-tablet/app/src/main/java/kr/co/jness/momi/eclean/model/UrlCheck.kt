package kr.co.jness.momi.eclean.model

import android.net.Uri
import java.net.URL
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

data class UrlCheck(
    val all: String,
    val domain: String,
    val encDomain: String,
    val subDir: String?,
    val checkDomain: String
) {

    companion object {

        val postSimpleDomain = listOf("com", "net", "org")

        fun getUrlCheck(urlString: String) : UrlCheck? {
            /**
             * URL API 체크위한 파라미터 설정.
             */
            try {
                /**
                 * 1. 앱에서 http, https를 누락한 경우 prefix를 붙인다.
                 */
                val replaceUrl = if(!urlString.startsWith("http")) "https://$urlString" else urlString
                /**
                 * https://로 시작하는 경우, http://로 변경 한다.
                 */
                replaceUrl.replace("https://", "http://")

                val url = if(urlString.startsWith("http")) URL(urlString) else URL("https://$urlString")
                val protocol = url.protocol
                val authority = url.authority
                val host = url.host
                val port = url.port
                val query = url.query
                val path = url.path
                val paths = path?.split("/")

                /**
                 * 도메인에 'www.'로 시작하면 제거
                 */
                val domain = host.replace("www.", "")

                val checkDomain = domain

//                val split = domain.split(".")
//                /**
//                 * domain이 naver.com, daum.net등 2자리면 domain을 그대로 넘기고
//                 * search.naver.com, korea.co.kr등 3자리인 경우 실제 도메인 구분이 필요하다.
//                 */
//                val checkDomain = if(split.size>=3 && postSimpleDomain.contains(split.last())) {
//                    try {
//                        /**
//                         * com,net등이면 두자리만
//                         */
//                        split.filterIndexed { index, s ->
//                            index >= split.size - 2
//                        }.toList().joinToString(".")
//                    } catch (e: Exception) {
//                        domain
//                    }
//                } else {
//                    domain
//                }

                /**
                 *
                 */
                val encDomain = toMd5("http://$domain:80")
                val subDir = if(paths?.size!!>0) paths.first() else null
                return UrlCheck(domain+path, domain, encDomain, subDir, checkDomain)

            } catch (e: Exception) {
                return null
            }
        }

        fun toMd5(s: String): String {
            val MD5 = "MD5"
            try {
                // Create MD5 Hash
                val digest = MessageDigest
                    .getInstance(MD5)
                digest.update(s.toByteArray())
                val messageDigest = digest.digest()

                // Create Hex String
                val hexString = StringBuilder()
                for (aMessageDigest in messageDigest) {
                    var h = Integer.toHexString(0xFF and aMessageDigest.toInt())
                    while (h.length < 2) h = "0$h"
                    hexString.append(h)
                }
                return hexString.toString()
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            }
            return ""
        }

    }


}