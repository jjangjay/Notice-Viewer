package jjangjay.edelive.controller;

import jjangjay.edelive.model.TestVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

@RestController
public class TestRestController {

//    @RequestMapping(value="/testValue", method = RequestMethod.GET)
    @GetMapping("/testValue")
    public String getTestValue(){
        String TestValue = "레스트컨트롤러 테스트";
        return TestValue;
    }

    @RequestMapping("/test")
    public ModelAndView test() {
        ModelAndView mav = new ModelAndView("test");
        mav.addObject("name", "goddaehee");

        List<String> testList = new ArrayList<String>();
        testList.add("a");
        testList.add("b");
        testList.add("c");

        mav.addObject("list", testList);
        return mav;
    }
}
/*
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Index</title>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
    <script>
        $.ajax({
            type: "GET",
            url: "/testValue",
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
*/