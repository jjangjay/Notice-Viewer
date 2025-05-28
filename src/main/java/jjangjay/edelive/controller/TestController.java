/*
package jjangjay.edelive.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TestController {
    @RequestMapping(value = "/home")
    public String home(){
        return "index.html";
    }

    @ResponseBody
    @RequestMapping("/valueTest")
    public String valueTest(){
        String value = "테스트 String";
        return value;
    }
}

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