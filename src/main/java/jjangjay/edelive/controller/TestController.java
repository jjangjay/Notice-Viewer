package jjangjay.edelive.controller;
/*
import jjangjay.edelive.crawler.CrawlingUtils;
import jjangjay.edelive.model.TestVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TestController {
    @RequestMapping("/thymeleafTest")
    public String thymeleafTest(Model model) {
        TestVo testModel = new TestVo("qwe", "ㅇㅅㅇ") ;
        model.addAttribute("testModel", testModel);
        return "thymeleaf/thymeleafTest";
    }

    @Autowired
    private CrawlingUtils crawlingUtils;

    // 크롤링 테스트 메서드 추가
    @ResponseBody
    @RequestMapping("/crawling-test")
    public String crawlingTest() {
        try {
            crawlingUtils.testCrawling();
            return "크롤링 테스트 완료! 콘솔을 확인해주세요.";
        } catch (Exception e) {
            return "크롤링 에러: " + e.getMessage();
        }
    }
//    @RequestMapping(value = "/home")
//    public String home(){
//        return "index.html";
//    }
//
//    @ResponseBody
//    @RequestMapping("/valueTest")
//    public String valueTest(){
//        String value = "테스트 String";
//        return value;
//    }
}
/*
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>Index</title>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
    <script>
        $.ajax({
            type: "GET",
            url: "/valueTest",
            success: (data) => {
                console.log(data);
                $('#contents').html(data);
            }
        });
    </script>
</head>
<body>
<h1>Hello World!</h1>
<div id="contents">
</div>
</body>
</html>
*/