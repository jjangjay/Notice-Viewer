package jjangjay.edelive.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jjangjay.edelive.crawler.EdelweisCrawler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class APIController {
    @PostMapping("/api/login")
    public String login(HttpServletResponse response, @RequestBody Map<String, Object> param) {
        response.setHeader("Content-Type", "application/json; charset=UTF-8");
        try {
            String userId = (String) param.get("userId");
            String userPw = (String) param.get("userPw");

            // 입력값 검증
            if (userId == null || userPw == null || userId.isEmpty() || userPw.isEmpty()) {
                return "{\"status\":false,\"message\":\"아이디와 비밀번호를 모두 입력해주세요.\"}";
            }

            String JSESSIONID = EdelweisCrawler.login(userId, userPw);
            if (JSESSIONID.isEmpty()) {
                return "{\"status\":false,\"message\":\"로그인 실패: JSESSIONID가 비어있습니다.\"}";
            }

            // JSESSIONID를 쿠키로 설정
            response.addCookie(new Cookie("JSESSIONID", JSESSIONID));
            return "{\"status\":true,\"message\":\"\"}";
        } catch (Exception e) {
            return "{\"status\":false,\"message\":\"" + e.getMessage() + "\"}";
        }
    }

    @PostMapping("/api/class/list")
    public String classList(HttpServletResponse response, @RequestBody Map<String, Object> param) {
        // TODO.
    }
}
