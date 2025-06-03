package jjangjay.edelive.crawler;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;  // 이 import가 중요!
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component
public class JsoupUtils {

    // Connection 객체를 반환하는 메소드
    public static Connection getJsoupConnection(String url) throws Exception {
        return Jsoup.connect(url);
    }

    // Elements 객체를 반환하는 메소드
    public static Elements getJsoupElements(Connection connection, String url, String query) throws Exception {
        Connection conn = !ObjectUtils.isEmpty(connection) ? connection :
                getJsoupConnection(url);
        Elements result = null;

        result = conn.get().select(query);

        return result;
    }
}
