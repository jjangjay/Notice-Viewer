package jjangjay.edelive.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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

        response.addCookie(new Cookie("JSESSIONID", JSESSIONID));
        return "{\"status\":true,\"message\":\"로그인 성공\"}";
    }

//    @PostMapping("/api/class/list")
//    public String classList(HttpServletRequest request, HttpServletResponse response, @RequestBody Map<String, Object> param) {
//        response.setHeader("Content-Type", "application/json; charset=UTF-8");
//
//        String JSESSIONID = findJSESSIONID(request.getCookies());
//        if (JSESSIONID == null || JSESSIONID.isEmpty()) {
//            return "{\"status\":false,\"message\":\"로그인 되어있지 않습니다. 다시 로그인해주세요.\"}";
//        }
//        if (!EdelweisCrawler.checkLoginStatus(JSESSIONID).status()) {
//            return "{\"status\":false,\"message\":\"로그인이 만료되었습니다. 다시 로그인해주세요.\"}";
//        }
//
//        try {
//            ArrayList<EdelweisCrawler.Class> classList = EdelweisCrawler.getClassList(JSESSIONID);
//
//            return "{\"status\":true,\"message\":\"\",\"classList\":" + classList.toString() + "}";
//        } catch (Exception e) {
//            e.printStackTrace();
//            return "{\"status\":false,\"message\":\"클래스 목록을 가져오는 중 오류가 발생했습니다.\"}";
//        }
//    }
    
    @PostMapping("/api/data")
    public String data(HttpServletRequest request, HttpServletResponse response) {
        response.setHeader("Content-Type", "application/json; charset=UTF-8");

        String JSESSIONID = findJSESSIONID(request.getCookies());
        if (JSESSIONID == null || JSESSIONID.isEmpty()) {
            return "{\"status\":false,\"message\":\"로그인 되어있지 않습니다. 다시 로그인해주세요.\"}";
        }
        if (!EdelweisCrawler.checkLoginStatus(JSESSIONID).status()) {
            return "{\"status\":false,\"message\":\"로그인이 만료되었습니다. 다시 로그인해주세요.\"}";
        }

        HashMap<String, Object> data = new HashMap<>();
        try {
//            public record Class(String className, float credit, String classType, String classNumber, String courseActiveSeq, String courseApplySeq, String ltType) {}
//            public record Board(String boardName, String url, String articleId, String type) {}
//            public record Post(String url, String title, String bbsSeq, String writer, String date) {}
//            public record PostData(String title, String writer, String date, String division, Integer viewCount, String content) {}
            ArrayList<jjangjay.edelive.crawler.EdelweisCrawler.Class> classList = EdelweisCrawler.getClassList(JSESSIONID);

            HashMap<String, Object> classData = new HashMap<>();
            for (EdelweisCrawler.Class classItem : classList) {
                System.out.println("- Class: " + classItem.className() + " " + classItem);

                Connection.Response classRoom = EdelweisCrawler.getClassroom(JSESSIONID, classItem);
                ArrayList<EdelweisCrawler.Board> boardList = EdelweisCrawler.getBoardList(classRoom.parse());
                HashMap<String, Object> boardData = new HashMap<>();
                for (EdelweisCrawler.Board board : boardList) {
                    System.out.println("    └─ Board: " + board.boardName() + " " + board);

                    ArrayList<EdelweisCrawler.Post> postList = EdelweisCrawler.getPostList(JSESSIONID, board, classItem);
                    List<HashMap<String, Object>> postDataList = new ArrayList<>();
                    for (EdelweisCrawler.Post post : postList) {
                        System.out.println("        ├─ Post: " + post.title() + " " + post);
                        HashMap<String, Object> postInfo = new HashMap<>();

                        EdelweisCrawler.PostData postDataValue = EdelweisCrawler.getPostData(JSESSIONID, board.articleId(), classItem, post);
                        System.out.println("        └─ PostData: " + postDataValue.title() + " " + postDataValue.toString().replaceAll("\\n", " "));

                        HashMap<String, Object> postDataDetail = new HashMap<>();
                        postDataDetail.put("title", postDataValue.title());
                        postDataDetail.put("writer", postDataValue.writer());
                        postDataDetail.put("date", postDataValue.date());
                        postDataDetail.put("division", postDataValue.division());
                        postDataDetail.put("viewCount", postDataValue.viewCount());
                        postDataDetail.put("content", postDataValue.content());

                        postInfo.put("postData", postDataDetail);
                        postInfo.put("title", post.title());
                        postInfo.put("writer", post.writer());
                        postInfo.put("date", post.date());
                        postDataList.add(postInfo);
                    }
                    boardData.put(board.boardName(), postDataList);
                }
                classData.put(classItem.className(), boardData);
            }
            data.put("classData", classData);

        } catch (Exception e) {
            e.printStackTrace();
            return "{\"status\":false,\"message\":\"클래스 데이터를 가져오는 중 오류가 발생했습니다.\"}";
        }

        if (data.isEmpty() || !data.containsKey("classData")) { // Check if classData was actually populated
            return "{\"status\":false,\"message\":\"클래스 데이터를 가져오는 중 오류가 발생했습니다. (데이터 비어있음)\"}";
        }
        
        data.put("status", true);
        data.put("message", "데이터 요청 성공");

        ObjectMapper mapper = new ObjectMapper();
        try {
            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);
            System.out.println("JSON Data: " + json);
            return json;
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"status\":false,\"message\":\"JSON 변환 중 오류가 발생했습니다.\"}";
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
