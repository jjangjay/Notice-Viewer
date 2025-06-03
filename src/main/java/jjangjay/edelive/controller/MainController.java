package jjangjay.edelive.controller;


import jjangjay.edelive.crawler.EdeliveCrawler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class MainController {
    @Autowired
    private EdeliveCrawler edeliveCrawler;

    @ResponseBody
    @PostMapping("/edelive-test")
    public String edeliveTest(String userId, String userPw) {
        try {
            if (userId == null || userPw == null || userId.isEmpty() || userPw.isEmpty()) {
                return "아이디와 비밀번호를 모두 입력해주세요.";
            }

            edeliveCrawler.setCredentials(userId, userPw);
            edeliveCrawler.crawlEdeliveClass();
            return "에델바이스 크롤링 완료! 콘솔을 확인해주세요.";
        } catch (Exception e) {
            return "에델바이스 크롤링 실패: " + e.getMessage();
        }
    }
}
