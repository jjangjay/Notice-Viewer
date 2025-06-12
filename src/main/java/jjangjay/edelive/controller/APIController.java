package jjangjay.edelive.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jjangjay.edelive.crawler.EdelweisCrawler;
import org.jsoup.Connection;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class APIController {
    @PostMapping("/api/login")
    public String login(HttpServletResponse response, @RequestBody Map<String, Object> param) {
        response.setHeader("Content-Type", "application/json; charset=UTF-8");
        String userId = (String) param.get("userId");
        String userPw = (String) param.get("userPw");

        // 입력값 검증
        if (userId == null || userPw == null || userId.isEmpty() || userPw.isEmpty()) {
            return "{\"status\":false,\"message\":\"아이디와 비밀번호를 모두 입력해주세요.\"}";
        }

        String JSESSIONID = EdelweisCrawler.login(userId, userPw);
        if (JSESSIONID == null || JSESSIONID.isEmpty()) {
            return "{\"status\":false,\"message\":\"로그인 실패: JSESSIONID가 비어있습니다.\"}";
        }

        try {
            ArrayList<jjangjay.edelive.crawler.EdelweisCrawler.Class> classList = EdelweisCrawler.getClassList(JSESSIONID);
            Connection.Response classRoom = EdelweisCrawler.getClassroom(JSESSIONID, classList.get(2));
            ArrayList<EdelweisCrawler.Board> boardList = EdelweisCrawler.getBoardList(classRoom.parse());
            EdelweisCrawler.Board board = boardList.get(0);
//            public static ArrayList<EdelweisCrawler.Post> getPostList(String edelweisUrl, String JSESSIONID, String srchBoardSeq, EdelweisCrawler.Class classItem) throws Exception {
            EdelweisCrawler.Post postList = EdelweisCrawler.getPostList(board.url(), JSESSIONID, board.articleId(), classList.get(2)).get(0);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // JSESSIONID를 쿠키로 설정
        response.addCookie(new Cookie("JSESSIONID", JSESSIONID));
        return "{\"status\":true,\"message\":\"\"}";
    }

    @PostMapping("/api/class/list")
    public String classList(HttpServletRequest request, HttpServletResponse response, @RequestBody Map<String, Object> param) {
        response.setHeader("Content-Type", "application/json; charset=UTF-8");

        String JSESSIONID = findJSESSIONID(request.getCookies());
        if (JSESSIONID == null || JSESSIONID.isEmpty()) {
            return "{\"status\":false,\"message\":\"로그인 되어있지 않습니다. 다시 로그인해주세요.\"}";
        }
        if (!EdelweisCrawler.checkLoginStatus(JSESSIONID).status()) {
            return "{\"status\":false,\"message\":\"로그인이 만료되었습니다. 다시 로그인해주세요.\"}";
        }

        try {
            ArrayList<EdelweisCrawler.Class> classList = EdelweisCrawler.getClassList(JSESSIONID);

            return "{\"status\":true,\"message\":\"\",\"classList\":" + classList.toString() + "}";
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"status\":false,\"message\":\"클래스 목록을 가져오는 중 오류가 발생했습니다.\"}";
        }
    }

    private String findJSESSIONID(@Nullable Cookie[] cookies) {
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equalsIgnoreCase("JSESSIONID")) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
