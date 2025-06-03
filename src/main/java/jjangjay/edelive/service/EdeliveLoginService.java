package jjangjay.edelive.service;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

@Service
public class EdeliveLoginService {

    private static final String LOGIN_URL = "https://hive.cju.ac.kr/security/login";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";

    public Connection loginToEdelive(String userId, String userPassword) throws Exception {
        // 로그인 요청 데이터 설정
        Connection.Response loginResponse = Jsoup.connect(LOGIN_URL)
                .userAgent(USER_AGENT)
                .method(Connection.Method.POST)
                .data("loginType", "1")
                .data("loginKind", "1")
                .data("type", "http")
                .data("currentMenuId", "900001")
                .data("j_username", userId)
                .data("j_password", userPassword)
                .execute();

        // 로그인 성공 여부 확인
        if (loginResponse.statusCode() == 200) {
            System.out.println("로그인 성공!");
            System.out.println(loginResponse.body());
            return loginResponse.parse().connection();
        } else {
            throw new Exception("로그인 실패: " + loginResponse.statusCode());
        }
    }

    public String getClassPageContent(Connection loginConnection, String classUrl) throws Exception {
        Document doc = loginConnection
                .url(classUrl)
                .get();

        return doc.html();
    }
}
