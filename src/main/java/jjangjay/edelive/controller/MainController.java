package jjangjay.edelive.controller;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jjangjay.edelive.crawler.EdelweisCrawler;
import jjangjay.edelive.crawler.EdelweisCrawler였던것;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@Controller
public class MainController {
//    @Autowired
//    private EdelweisCrawler edelweisCrawler;
//
//    @PostMapping("/edelive-test")
//    public String edeliveTest(@RequestBody Map<String, Object> param) {
//        try {
//            String userId = (String) param.get("userId");
//            String userPw = (String) param.get("userPw");
//
//            // 입력값 검증
//            if (userId == null || userPw == null || userId.isEmpty() || userPw.isEmpty()) {
//                return "아이디와 비밀번호를 모두 입력해주세요.";
//            }
//
//            edelweisCrawler.crawlEdelweisClass(userId, userPw);
//            return "에델바이스 크롤링 완료! 콘솔을 확인해주세요.";
//        } catch (Exception e) {
//            return "에델바이스 크롤링 실패: " + e.getMessage();
//        }
//    }

    @GetMapping("/login")
    public String loginPage() {
        return "login.html"; // login.html 파일을 반환
    }

    @ResponseBody
    @PostMapping("/api/login")
    public String login(HttpServletResponse response, @RequestBody Map<String, Object> param) {
        response.setHeader("Content-Type", "application/json; charset=UTF-8");
        try {
            String userId = (String) param.get("userId");
            String userPw = (String) param.get("userPw");

            // 입력값 검증
            if (userId == null || userPw == null || userId.isEmpty() || userPw.isEmpty()) {
                return "{\"status\":false,\"message\": \"아이디와 비밀번호를 모두 입력해주세요.\"}";
            }

            String JSESSIONID = EdelweisCrawler.login(userId, userPw);
            if (JSESSIONID.isEmpty()) {
                return "{\"status\":false,\"message\": \"로그인 실패: JSESSIONID가 비어있습니다.\"}";
            }

            // JSESSIONID를 쿠키로 설정
            response.addCookie(new Cookie("JSESSIONID", JSESSIONID));
            return "{\"status\":true,\"message\": \"\"}";
        } catch (Exception e) {
            return "{\"status\":false,\"message\": \"" + e.getMessage() + "\"}";
        }
    }
}