package jjangjay.edelive.crawler;

import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

@Component
public class CrawlingUtils {

    public void testCrawling() {
        try {
            String viewerUrl = "https://www.google.com";
            String query = "title";

            Elements selects = JsoupUtils.getJsoupElements(null, viewerUrl, query);

            System.out.println(selects.get(0).text());
            System.out.println(selects.get(0).html());
            System.out.println(selects.get(0).children());
            System.out.println(selects.get(0).parent());
            System.out.println(selects.get(0).parent().previousElementSibling());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
