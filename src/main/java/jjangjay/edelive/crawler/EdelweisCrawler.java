package jjangjay.edelive.crawler;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class EdelweisCrawler {
    private static final String EDELWEIS_URL = "https://hive.cju.ac.kr";
    private static final String LOGIN_URL = "https://hive.cju.ac.kr/security/login";
    private static final String MYPAGE_URL = "https://hive.cju.ac.kr/usr/member/stu/dash/detail.do";
    private static final String CLASSROOM_URL = "https://hive.cju.ac.kr/usr/classroom/main.do?currentMenuId=&courseApplySeq=%s&courseActiveSeq=%s";

    public static String login(String userId, String userPassword) {
        try {
            // 로그인 요청 데이터 설정
            Connection.Response loginResponse = Jsoup.connect(LOGIN_URL)
                    .method(Connection.Method.POST)
                    .data("loginType", "1")
                    .data("loginKind", "1")
                    .data("type", "http")
                    .data("currentMenuId", "900001")
                    .data("j_username", userId)
                    .data("j_password", userPassword)
                    .execute();

            // 로그인 성공 여부 확인
            if (loginResponse.statusCode() != 200) {
                throw new Exception("로그인 실패. 에델바이스 서버에서 올바른 응답을 주지 않았습니다.");
            }

            String JSESSIONID = loginResponse.cookie("JSESSIONID");
            if (JSESSIONID == null || JSESSIONID.isEmpty()) {
                throw new Exception("로그인에 성공하였지만, 서버에서 올바르게 처리하지 못했습니다.");
            }
            LoginStatus loginStatus = EdelweisCrawler.checkLoginStatus(JSESSIONID);
            if (!loginStatus.status()) {
                throw new Exception(loginStatus.message());
            }

            System.out.println("로그인 성공! JSESSIONID: " + JSESSIONID);

            return JSESSIONID;
        } catch (Exception e) {
            e.printStackTrace();
            return null; // 로그인 실패 시 null 반환
        }
    }

    public static LoginStatus checkLoginStatus(String JSESSIONID) {
        try {
            // 마이페이지 접근을 통해 로그인 상태 확인
            Connection.Response checkLogin = getData(MYPAGE_URL, JSESSIONID);
            if (checkLogin.statusCode() != 200) {
                return new LoginStatus(false, "로그인 상태 확인 실패. JSESSIONID가 유효하지 않습니다.");
            }

            Document myPageParsed = checkLogin.parse();
            if (!(myPageParsed.html().contains("로그아웃") && myPageParsed.html().contains("학기선택") && myPageParsed.html().contains("포트폴리오 작성"))) {
                return new LoginStatus(false, "로그인 상태가 아닙니다. JSESSIONID가 유효하지 않습니다.");
            }
            return new LoginStatus(true, "");
        } catch (Exception e) {
            e.printStackTrace();
            return new LoginStatus(false, "로그인 상태 확인 중 오류가 발생했습니다.");
        }
    }

    public static ArrayList<Class> getClassList(String JSESSIONID) throws Exception {
        Connection.Response response = getData(MYPAGE_URL, JSESSIONID);

        System.out.println("수업 목록을 가져오는 중...");
        if (response.statusCode() != 200) {
            throw new Exception("수업 목록을 가져오는 데 실패했습니다. JSESSIONID가 유효하지 않습니다.");
        }

        System.out.println("수업 목록을 가져오는 데 성공했습니다. 응답 코드: " + response.statusCode());
        Document parsedMyPage = response.parse();
        Elements classLists = parsedMyPage.select("#content > div.capacity > div > div.section.capa_dev.study > div.row > div:nth-child(1) > div.fx_box.sm > table > tbody:nth-child(4) > tr");
        if (classLists.isEmpty()) {
            throw new Exception("수업 목록이 비어있습니다. 현재 수업이 없습니다.");
        }

        System.out.println("수업 목록을 파싱 중...");
        ArrayList<Class> classList = new ArrayList<>();
        for (Element classItem : classLists) {
            try {
                String className = classItem.select("td:nth-child(1)").text(); // 없으면 걍 ""
                String classNumber = classItem.select("td:nth-child(2)").text();
                String classType = classItem.select("td:nth-child(3)").text();
                float credit = Float.parseFloat(classItem.select("td:nth-child(4)").text()); // 얘는 해석하다가 터짐.

                String courseActiveSeq = classItem.select("td:nth-child(1) > a").attr("onclick").replace("\n", "")
                        .replaceAll("^doClassroom\\(\\{.*'courseActiveSeq':'(\\d+)',.*'courseApplySeq':'(\\d+)',.*'ltType':'(\\w+)',.*}\\);$", "$1");
                String courseApplySeq = classItem.select("td:nth-child(1) > a").attr("onclick").replace("\n", "")
                        .replaceAll("^doClassroom\\(\\{.*'courseActiveSeq':'(\\d+)',.*'courseApplySeq':'(\\d+)',.*'ltType':'(\\w+)',.*}\\);$", "$2");
                String ltType = classItem.select("td:nth-child(1) > a").attr("onclick").replace("\n", "")
                        .replaceAll("^doClassroom\\(\\{.*'courseActiveSeq':'(\\d+)',.*'courseApplySeq':'(\\d+)',.*'ltType':'(\\w+)',.*}\\);$", "$3");

                classList.add(new Class(className, credit, classType, classNumber, courseActiveSeq, courseApplySeq, ltType));
            } catch (Exception ignored) {
            }
        }
        System.out.println("수업 목록을 성공적으로 가져왔습니다. 총 " + classList.size() + "개의 수업이 있습니다.");
        for (Class cls : classList) {
            System.out.println("-수업명: " + cls.className() + ", 학점: " + cls.credit() + ", 유형: " + cls.classType() +
                    ", 번호: " + cls.classNumber() + ", courseActiveSeq: " + cls.courseActiveSeq() +
                    ", courseApplySeq: " + cls.courseApplySeq() + ", ltType: " + cls.ltType());
        }
        System.out.println();

        return classList;
    }

    public static Connection.Response getClassroom(String JSESSIONID, String courseActiveSeq, String courseApplySeq) throws Exception {
        Connection.Response response = getData(CLASSROOM_URL.formatted(courseApplySeq, courseActiveSeq), JSESSIONID);

        if (response.statusCode() != 200) {
            throw new Exception("수업 정보를 가져오는 데 실패했습니다. JSESSIONID가 유효하지 않습니다.");
        }
        System.out.println("수업 정보를 가져오는 데 성공했습니다. 응답 코드: " + response.statusCode());
        Document parsedClass = response.parse();
        Elements announcement = parsedClass.select("#content > div > div.d_sub.hideMeta > div.d_post > div.notice > div.inner > ul > li");
        System.out.println("수업 공지사항을 파싱 중...");
        if (announcement.isEmpty()) {
            throw new Exception("수업 공지사항이 비어있습니다. 현재 공지사항이 없습니다.");
        }
        System.out.println("수업 공지사항을 성공적으로 가져왔습니다. 총 " + announcement.size() + "개의 공지사항이 있습니다.");
        for (Element ann : announcement) {
            String title = ann.select("a").text();
            String date = ann.select("span").text();
            System.out.println("-공지사항 제목: " + title + ", 날짜: " + date);
        }
        System.out.println();

        ArrayList<Board> boardList = getBoardList(parsedClass);
        System.out.println("수업 게시판 정보를 성공적으로 가져왔습니다. 총 " + boardList.size() + "개의 게시판이 있습니다.");
        for (Board board : boardList) {
            System.out.println("-게시판 URL: " + board.url() + ", 게시글 ID: " + board.articleId() + ", 유형: " + board.type());
        }

        getPostList(EDELWEIS_URL + boardList.get(0).url(), JSESSIONID, boardList.get(0).articleId(), courseActiveSeq, courseApplySeq, boardList.get(0).type());

        return response;
    }

    public static ArrayList<Board> getBoardList(Document parsedClass) {
        ArrayList<Board> boardList = new ArrayList<>();

        for (String cssSelector : Arrays.asList(
                "#content > div > div.d_sub.hideMeta > div.d_post > div.notice > div.top > a",
                "#content > div > div.d_sub.hideMeta > div.d_post > div.data > div.top > a"
        )) {
            List<String> announcementBoard = Arrays.asList(
                    parsedClass.select(cssSelector).attr("onclick")
                            .replace("\n", "").replace(" ", "")
                            .replace("\t", "")
                            .split("More\\('")[1].split("'\\);")[0]
                            .strip().split("','") // 여기 ', ' 에서 띄어쓰기 하나땜에 생긴 오류임
            );

            String boardUrl = announcementBoard.get(0);
            String boardArticleId = announcementBoard.get(1);
            String boardType = announcementBoard.get(2);
            boardList.add(new Board(boardUrl, boardArticleId, boardType));
        }
        System.out.println("게시판 목록을 파싱 중...");
        if (boardList.isEmpty()) {
            throw new RuntimeException("게시판 목록이 비어있습니다. 현재 게시판이 없습니다.");
        }
        System.out.println("게시판 목록을 성공적으로 가져왔습니다. 총 " + boardList.size() + "개의 게시판이 있습니다.");
        for (Board board : boardList) {
            System.out.println("-게시판 URL: " + board.url() + ", 게시글 ID: " + board.articleId() + ", 유형: " + board.type());
        }
        System.out.println();

        return boardList;
    }

    public static ArrayList<post> getPostList(String edelweisUrl, String JSESSIONID, String srchBoardSeq, String courseActiveSeq, String courseApplySeq, String iconType) throws Exception {
        ArrayList<post> postList = new ArrayList<>();
        Connection.Response response = postData(edelweisUrl, JSESSIONID, new HashMap<>() {{
            put("currentPage", "1");
            put("perPage", "1000");
            put("orderby", "0");
            put("srchBoardSeq", srchBoardSeq);
            put("courseActiveSeq", courseActiveSeq);
            put("courseApplySeq", courseApplySeq);
//            put("iconType", iconType);
        }});

        if (response.statusCode() != 200) {
            throw new Exception("게시판 정보를 가져오는 데 실패했습니다. JSESSIONID가 유효하지 않습니다.");
        }

        System.out.println("게시판 정보를 가져오는 데 성공했습니다. 응답 코드: " + response.statusCode());
        Document parsedBoard = response.parse();
        Elements posts = parsedBoard.select("body > div.page_frame > div.frm_ct > ul > li");
        for (Element post : posts) {
            String url = edelweisUrl.replace("list.do", "detail.do");
            String postTitle = post.select("div.con > a").text();
            String bbsSeq = post.select("div.con > a").attr("onclick")
                    .replace("\n", "").replace(" ", "")
                    .replace("\t", "")
                    .replaceAll(".*'bbsSeq':'(\\d+)'.*", "$1");
            String writer = post.select("div.info > span.writer").text();
            String postDate = post.select("div.info > span.date").text();
            postList.add(new post(url, postTitle, bbsSeq, writer, postDate));
        }
        System.out.println("게시판 정보를 성공적으로 가져왔습니다. 총 " + postList.size() + "개의 게시글이 있습니다.");
        for (post post : postList) {
            System.out.println("-게시글 URL: " + post.url() + ", 제목: " + post.title() +
                    ", 게시글 ID: " + post.bbsSeq() + ", 작성자: " + post.writer() + ", 날짜: " + post.date());
        }
        System.out.println();
        return postList;
    }

    public static Connection.Response getData(String edelweisUrl, String JSESSIONID) throws Exception {
        return Jsoup.connect(edelweisUrl)
                .cookie("JSESSIONID", JSESSIONID)
                .execute();
    }

    public static Connection.Response postData(String edelweisUrl, String JSESSIONID, java.util.Map<String, String> data) throws Exception {
        return Jsoup.connect(edelweisUrl)
                .method(Connection.Method.POST)
                .data(data)
                .cookie("JSESSIONID", JSESSIONID)
                .execute();
    }

    public record LoginStatus(boolean status, String message) {}
    public record Class(String className, float credit, String classType, String classNumber, String courseActiveSeq, String courseApplySeq, String ltType) {}
    public record Board(String url, String articleId, String type) {}
    public record post(String url, String title, String bbsSeq, String writer, String date) {}
}