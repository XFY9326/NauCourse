package tool.xfy9326.naucourses.network.clients.utils

import kotlin.random.Random

/**
 * User Agent池
 * @author XFY9326
 */
object UAPool {
    private val UA = arrayOf(
        "Mozilla/5.0 (Windows NT 6.3; WOW64; Trident/7.0; rv:11.0) like Gecko",
        "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.95 Safari/537.36",
        "Mozilla/5.0 (Windows; U; Windows NT 5.2) Gecko/2008070208 Firefox/3.0.1",
        "Mozilla/5.0 (Windows; U; Windows NT 5.1) Gecko/20070309 Firefox/2.0.0.3",
        "Mozilla/5.0 (Windows; U; Windows NT 5.1) Gecko/20070803 Firefox/1.5.0.12",
        "Opera/9.27 (Windows NT 5.2; U; zh-cn)",
        "Mozilla/5.0 (Macintosh; PPC Mac OS X; U; en) Opera 8.0",
        "Opera/8.0 (Macintosh; PPC Mac OS X; U; en)",
        "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.12) Gecko/20080219",
        "Firefox/2.0.0.12 Navigator/9.0.0.6",
        "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; Win64; x64; Trident/4.0)",
        "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; Trident/4.0)",
        "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.1 (KHTML, like Gecko)",
        "Chrome/26.0.1410.43 Safari/537.1",
        "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:21.0) Gecko/20100101 Firefox/21.0",
        "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.1 (KHTML, like Gecko)",
        "Chrome/21.0.1180.92 Safari/537.1 LBBROWSER",
        "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; WOW64; Trident/6.0; BIDUBrowser 2.x)",
        "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/536.11 (KHTML, like Gecko)",
        "Chrome/20.0.1132.11 TaoBrowser/3.0 Safari/536.11"
    )

    /**
     * 获取随机的UA
     * @return UA
     */
    fun getRandomUA() = UA[Random(System.currentTimeMillis()).nextInt(0, UA.size)]
}