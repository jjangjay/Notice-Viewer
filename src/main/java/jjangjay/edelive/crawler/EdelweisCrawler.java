package jjangjay.edelive.crawler;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;

public class EdelweisCrawler {
    private static final String HOST_URL = "https://hive.cju.ac.kr";
    private static final String LOGIN_URL = HOST_URL + "/security/login";

    private static final String MYPAGE_URL = "/usr/member/stu/dash/detail.do";
    private static final String CLASSROOM_URL = "/usr/classroom/main.do?currentMenuId=&courseApplySeq=%s&courseActiveSeq=%s";

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

        ArrayList<Class> classList = new ArrayList<>();
        for (Element classItem : classLists) {
            try {
                String className = classItem.select("td:nth-child(1)").text();
                String classNumber = classItem.select("td:nth-child(2)").text();
                String classType = classItem.select("td:nth-child(3)").text();
                float credit = Float.parseFloat(classItem.select("td:nth-child(4)").text());

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

    public static Connection.Response getClassroom(String JSESSIONID, Class classItem) throws Exception {
        String courseActiveSeq = classItem.courseActiveSeq();
        String courseApplySeq = classItem.courseApplySeq();
        Connection.Response response = getData(CLASSROOM_URL.formatted(courseApplySeq, courseActiveSeq), JSESSIONID);

//        if (response.statusCode() != 200) {
//            throw new Exception("수업 정보를 가져오는 데 실패했습니다. JSESSIONID가 유효하지 않습니다.");
//        }
//        System.out.println("수업 정보를 가져오는 데 성공했습니다. 응답 코드: " + response.statusCode());
//        Document parsedClass = response.parse();
//
//        ArrayList<Board> boardList = getBoardList(parsedClass);
//        System.out.println("수업 게시판 정보를 성공적으로 가져왔습니다. 총 " + boardList.size() + "개의 게시판이 있습니다.");
//        for (Board board : boardList) {
//            System.out.println("-게시판 URL: " + board.url() + ", 게시글 ID: " + board.articleId() + ", 유형: " + board.type());
//        }
//
////        getPostList(EDELWEIS_URL + boardList.get(0).url(), JSESSIONID, boardList.get(0).articleId(), courseActiveSeq, courseApplySeq);
//        for (int i = 0; i < boardList.size(); i++) {
//            Board board = boardList.get(i);
//            System.out.println("게시판 " + (i + 1) + "의 게시글 목록을 가져오는 중...");
//            // 자바는 클래스(record, class, interface 등) 다 대문자로 시작. 변수 이름이 camelCase
//            ArrayList<Post> postList = getPostList(EDELWEIS_URL + board.url(), JSESSIONID, board.articleId(), courseActiveSeq, courseApplySeq);
//            System.out.println("게시판 " + (i + 1) + "의 게시글 목록을 성공적으로 가져왔습니다. 총 " + postList.size() + "개의 게시글이 있습니다.");
//            System.out.println();
//        }

        return response;
    }

    public static ArrayList<Board> getBoardList(Document parsedClass) {
        ArrayList<Board> boardList = new ArrayList<>();

        for (String cssSelector : Arrays.asList(
                "#content > div > div.d_sub.hideMeta > div.d_post > div.notice > div.top",
                "#content > div > div.d_sub.hideMeta > div.d_post > div.data > div.top"
        )) {
            List<String> announcementBoard = Arrays.asList(
                    parsedClass.select("%s > a".formatted(cssSelector)).attr("onclick")
                            .replace("\n", "").replace(" ", "")
                            .replace("\t", "")
                            .split("More\\('")[1].split("'\\);")[0]
                            .strip().split("','")
            );


            String boardName = parsedClass.select("%s > h3.title".formatted(cssSelector)).text();
            String boardUrl = announcementBoard.get(0);
            String boardArticleId = announcementBoard.get(1);
            String boardType = announcementBoard.get(2);
            boardList.add(new Board(boardName, boardUrl, boardArticleId, boardType));
        }

        String className = parsedClass.select("#header > div > div > div > h1").text();
//        System.out.println("게시판 목록을 성공적으로 가져왔습니다. 총 " + boardList.size() + "개의 게시판이 있습니다.");
        System.out.println(className + "의 게시판 목록을 성공적으로 가져왔습니다. 총 " + boardList.size() + "개의 게시판이 있습니다.");
        for (Board board : boardList) {
            System.out.println("-게시판 이름: " + board.boardName() + ", URL: " + board.url() +
                    ", 게시글 ID: " + board.articleId() + ", 유형: " + board.type());
        }
        System.out.println();

        return boardList;
    }

    public static ArrayList<Post> getPostList(String edelweisUrl, String JSESSIONID, String srchBoardSeq, Class classItem) throws Exception {
        String courseActiveSeq = classItem.courseActiveSeq();
        String courseApplySeq = classItem.courseApplySeq();

        ArrayList<Post> postList = new ArrayList<>();
        Connection.Response response = postData(edelweisUrl, JSESSIONID, Map.of(
                "currentPage", "1",
                "perPage", "1000",
                "orderby", "0",
                "srchBoardSeq", srchBoardSeq,
                "courseActiveSeq", courseActiveSeq,
                "courseApplySeq", courseApplySeq
        ));

        if (response.statusCode() != 200) {
            throw new Exception("게시판 정보를 가져오는 데 실패했습니다. JSESSIONID가 유효하지 않습니다.");
        }

        System.out.println("게시판 정보를 가져오는 데 성공했습니다. 응답 코드: " + response.statusCode());
        Document parsedBoard = response.parse();
        Elements posts = parsedBoard.select("body > div.page_frame > div.frm_ct > ul > li");
//        HashMap<String, PostData> postDataList = new HashMap<>();
        for (Element post : posts) {
            String url = edelweisUrl.replace("list.do", "detail.do");
            String postTitle = post.select("div.con > a").text();
            String bbsSeq = post.select("div.con > a").attr("onclick")
                    .replace("\n", "").replace(" ", "")
                    .replace("\t", "")
                    .replaceAll(".*'bbsSeq':'(\\d+)'.*", "$1");
            String writer = post.select("div.info > span.writer").text();
            String postDate = post.select("div.info > span.date").text();
            Post postItem = new Post(url, postTitle, bbsSeq, writer, postDate);
            postList.add(postItem);
//            postDataList.put(postItem.bbsSeq, getPost(url, JSESSIONID, srchBoardSeq, courseActiveSeq, courseApplySeq, bbsSeq));
        }

        System.out.println("게시판 정보를 성공적으로 가져왔습니다. 총 " + postList.size() + "개의 게시글이 있습니다.");
        for (int i = 0; i < postList.size(); i++) {
            Post post = postList.get(i);
            System.out.println(post);
//            PostData postData = postDataList.get(post.bbsSeq());
//            System.out.println(postData);
        }

        System.out.println();
        return postList;
    }

    public static PostData getPost(String edelweisUrl, String JSESSIONID, String srchBoardSeq, String courseActiveSeq, String courseApplySeq, String bbsSeq) throws Exception {
        Connection.Response response = postData(edelweisUrl, JSESSIONID, Map.of(
                "currentPage", "1",
                "perPage", "1000",
                "orderby", "0",
                "srchBoardSeq", srchBoardSeq,
                "courseActiveSeq", courseActiveSeq,
                "courseApplySeq", courseApplySeq,
                "bbsSeq", bbsSeq
        ));

        if (response.statusCode() != 200) {
            throw new Exception("게시글 정보를 가져오는 데 실패했습니다. JSESSIONID가 유효하지 않습니다.");
        }

//        System.out.println("게시글 정보를 가져오는 데 성공했습니다. 응답 코드: " + response.statusCode());
        Document parsedPost = response.parse();

        String postTitle = parsedPost.select("body > div.page_frame > div.frm_ct > div.col_2 > div > div.c_detail > div.top > h3").text();
        String postWriter = parsedPost.select("body > div.page_frame > div.frm_ct > div.col_2 > div > div.c_detail > div.info > div > ul > li:nth-child(1) > span:nth-child(1)").text();
        String postDate = parsedPost.select("body > div.page_frame > div.frm_ct > div.col_2 > div > div.c_detail > div.info > div > ul > li:nth-child(1) > span.part").text();

        String _tempPostDevision = parsedPost.select("body > div.page_frame > div.frm_ct > div.col_2 > div > div.c_detail > div.info > div > ul > li:nth-child(2) > span.part").text();
        String postDevision = _tempPostDevision.isEmpty() ? null : _tempPostDevision;
        String _tempViewCount = parsedPost.select("body > div.page_frame > div.frm_ct > div.col_2 > div > div.c_detail > div.info > div > ul > li:nth-child(3) > span.part").text();
        Integer postViewCount = _tempViewCount.isEmpty() ? null : Integer.parseInt(_tempViewCount);

        String postContent = parsedPost.select("body > div.page_frame > div.frm_ct > div.col_2 > div > div.c_detail > div.con.text_editor").html()
                .replace("<br>", "").replace("&nbsp;", " ").replaceAll("<[^>]+>", "").replace("\n\n", "\n"); // HTML 태그 제거

        return new PostData(postTitle, postWriter, postDate, postDevision, postViewCount, postContent);
    }

    public static Connection.Response getData(String edelweisUrl, String JSESSIONID) throws Exception {
        return Jsoup.connect(HOST_URL + edelweisUrl)
                .cookie("JSESSIONID", JSESSIONID)
                .execute();
    }

    public static Connection.Response postData(String edelweisUrl, String JSESSIONID, java.util.Map<String, String> data) throws Exception {
        return Jsoup.connect(HOST_URL + edelweisUrl)
                .method(Connection.Method.POST)
                .data(data)
                .cookie("JSESSIONID", JSESSIONID)
                .execute();
    }

    public record LoginStatus(boolean status, String message) {}
    public record Class(String className, float credit, String classType, String classNumber, String courseActiveSeq, String courseApplySeq, String ltType) {}
    public record Board(String boardName, String url, String articleId, String type) {}
    public record Post(String url, String title, String bbsSeq, String writer, String date) {}
    public record PostData(String title, String writer, String date, String devision, Integer viewCount, String content) {}
}