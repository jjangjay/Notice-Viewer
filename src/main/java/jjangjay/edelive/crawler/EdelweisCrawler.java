package jjangjay.edelive.crawler;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class EdelweisCrawler {
    private static final String LOGIN_URL = "https://hive.cju.ac.kr/security/login";
    private static final String MYPAGE_URL = "https://hive.cju.ac.kr/usr/member/stu/dash/detail.do";

    public static String login(String userId, String userPassword) throws Exception {
        // 로그인 요청 데이터 설정
        Connection.Response loginResponse = Jsoup.connect(LOGIN_URL)
                .method(Connection.Method.POST)
                .data("loginType", "1")
                .data("loginKind", "1")
                .data("type", "http")
                .data("currentMenuId", "900001")
                .data("j_username", userId)
                .data("j_password", userPassword)
                .execute();

        // 로그인 성공 여부 확인
        if (loginResponse.statusCode() != 200) {
            throw new Exception("로그인 실패. 에델바이스 서버에서 올바른 응답을 주지 않았습니다.");
        }

        String JSESSIONID = loginResponse.cookie("JSESSIONID");
        if (JSESSIONID == null || JSESSIONID.isEmpty()) {
            throw new Exception("로그인에 성공하였지만, 서버에서 올바르게 처리하지 못했습니다.");
        }
        LoginStatus loginStatus = EdelweisCrawler.checkLoginStatus(JSESSIONID);
        if (!loginStatus.status()) {
            throw new Exception(loginStatus.message());
        }

        System.out.println("로그인 성공! JSESSIONID: " + JSESSIONID);

        return JSESSIONID;
    }

    private record LoginStatus(boolean status, String message) {}
    public static LoginStatus checkLoginStatus(String JSESSIONID) throws Exception {
        // 마이페이지 접근을 통해 로그인 상태 확인
        Connection.Response checkLogin = Jsoup.connect(MYPAGE_URL)
                .cookie("JSESSIONID", JSESSIONID)
                .execute();

        if (checkLogin.statusCode() != 200) {
            return new LoginStatus(false, "로그인 상태 확인 실패. JSESSIONID가 유효하지 않습니다.");
        }

        Document myPageParsed = checkLogin.parse();
        if (!myPageParsed.html().contains("로그아웃")) {
            return new LoginStatus(false, "로그인 상태가 아닙니다. JSESSIONID가 유효하지 않습니다.");
        }
        return new LoginStatus(true, "");
    }

    public static String getData(String edelweisUrl, String JSESSIONID) throws Exception {
        Document doc = Jsoup.connect(edelweisUrl)
                .cookie("JSESSIONID", JSESSIONID)
                .get();

        return doc.html();
    }
}
