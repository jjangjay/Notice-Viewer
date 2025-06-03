package jjangjay.edelive.crawler;

import jjangjay.edelive.service.EdeliveLoginService;
import lombok.Setter;
import org.jsoup.Connection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EdeliveCrawler {

    @Autowired
    private EdeliveLoginService loginService;

    // 설정값들
    private String userId;
    private String userPw;
    private String classUrl = "https://hive.cju.ac.kr/usr/classroom/main.do?currentMenuId=&courseApplySeq=7371561&courseActiveSeq=106467";

    public void setCredentials(String userId, String userPw) {
        this.userId = userId;
        this.userPw = userPw;
    }

    public void crawlEdeliveClass() {
        try {
            // 1. 로그인 수행
            System.out.println("에델바이스 로그인 시도 중...");
            Connection loginConnection = loginService.loginToEdelive(userId, userPw);

            // 2. 강의 페이지 접근
            System.out.println("강의 페이지 접근 중...");
            String classPageContent = loginService.getClassPageContent(loginConnection, classUrl);

            // 3. 결과 출력 (처음 500자만)
            System.out.println("=== 강의 페이지 내용 (앞 500자) ===");
            System.out.println(classPageContent.substring(0, Math.min(500, classPageContent.length())));

            System.out.println("\n=== 크롤링 완료 ===");

        } catch (Exception e) {
            System.err.println("크롤링 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
