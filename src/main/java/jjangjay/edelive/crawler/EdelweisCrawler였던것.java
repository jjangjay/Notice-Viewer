package jjangjay.edelive.crawler;

import org.springframework.stereotype.Component;

@Component
public class EdelweisCrawler였던것 {

    // 설정값들
    private String classUrl = "https://hive.cju.ac.kr/usr/classroom/main.do?currentMenuId=&courseApplySeq=7371561&courseActiveSeq=106467";

    public void crawlEdelweisClass(String userId, String userPw) throws Exception {
        // 1. 로그인 수행
        System.out.println("에델바이스 로그인 시도 중...");
        String JSESSIONID = EdelweisCrawler.login(userId, userPw);
        if (JSESSIONID.isEmpty()) {
            throw new Exception("로그인 실패: JSESSIONID가 비어있습니다.");
        }
        System.out.println("로그인 성공! JSESSIONID: " + JSESSIONID);

        // 2. 강의 페이지 접근
        System.out.println("강의 페이지 접근 중...");
        String classPageContent = EdelweisCrawler.getData(classUrl, JSESSIONID);

        // 3. 결과 출력 (처음 500자만)
        System.out.println("=== 강의 페이지 내용 (앞 500자) ===");
        System.out.println(classPageContent.substring(0, Math.min(500, classPageContent.length())));

        System.out.println("\n=== 크롤링 완료 ===");
    }

    public static String login(String userId, String userPw) throws Exception {
        return EdelweisCrawler.login(userId, userPw);
    }
}
