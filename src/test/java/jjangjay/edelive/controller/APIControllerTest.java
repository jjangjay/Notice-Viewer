package jjangjay.edelive.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jjangjay.edelive.crawler.EdelweisCrawler;
import org.jsoup.Connection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class APIControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // 실제와 유사한 JSESSIONID 형식으로 변경
    private static final String MOCK_JSESSIONID = "58B39F221041EC437281DCB05ECFF789";

    // 테스트용 사용자 인증 정보
    private static final String TEST_USER_ID = "testuser123";
    private static final String TEST_USER_PASSWORD = "Password123!";

    @Nested
    @DisplayName("로그인 API 테스트")
    class LoginApiTest {
        private MockedStatic<EdelweisCrawler> mockedCrawler;

        @BeforeEach
        void setUp() {
            // 테스트 시작 전 EdelweisCrawler의 static 메소드를 모킹
            mockedCrawler = mockStatic(EdelweisCrawler.class);
        }

        @AfterEach
        void tearDown() {
            // 테스트 종료 후 모킹 해제
            mockedCrawler.close();
        }

        @Test
        @DisplayName("성공적인 로그인 테스트")
        void loginSuccess() throws Exception {
            // Given
            Map<String, Object> loginParams = new HashMap<>();
            loginParams.put("userId", TEST_USER_ID);
            loginParams.put("userPw", TEST_USER_PASSWORD);

            // login() 메소드가 유효한 JSESSIONID를 반환하도록 모킹
                    .thenReturn(MOCK_JSESSIONID);

            // checkLoginStatus가 성공 상태를 반환하도록 모킹
            mockedCrawler.when(() -> EdelweisCrawler.checkLoginStatus(anyString()))
                    .thenReturn(new EdelweisCrawler.LoginStatus(true, "로그인 성공"));

            // When & Then
            mockMvc.perform(post("/api/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginParams)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status", is(true)))
                    .andExpect(jsonPath("$.message", is("로그인 성공")))
                    .andExpect(cookie().exists("JSESSIONID"))
                    .andExpect(cookie().value("JSESSIONID", MOCK_JSESSIONID));
        }

        @Test
        @DisplayName("로그인 실패 - JSESSIONID 반환 없음")
        void loginFailureNoJsessionId() throws Exception {
            // Given
            Map<String, Object> loginParams = new HashMap<>();
            loginParams.put("userId", "wrongUser");
            loginParams.put("userPw", "wrongPassword");

            // login() 메소드가 null을 반환하도록 모킹
            mockedCrawler.when(() -> EdelweisCrawler.login(anyString(), anyString()))
                    .thenReturn(null);

            // When & Then
            mockMvc.perform(post("/api/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginParams)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is(false)))
                    .andExpect(jsonPath("$.message", is("로그인 실패: JSESSIONID가 비어있습니다.")));
        }

        @Test
        @DisplayName("로그인 실패 - 빈 JSESSIONID 반환")
        void loginFailureEmptyJsessionId() throws Exception {
            // Given
            Map<String, Object> loginParams = new HashMap<>();
            loginParams.put("userId", "wrongUser");
            loginParams.put("userPw", "wrongPassword");

            // login() 메소드가 빈 문자열을 반환하도록 모킹
            mockedCrawler.when(() -> EdelweisCrawler.login(anyString(), anyString()))
                    .thenReturn("");

            // When & Then
            mockMvc.perform(post("/api/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginParams)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is(false)))
                    .andExpect(jsonPath("$.message", is("로그인 실패: JSESSIONID가 비어있습니다.")));
        }

        @Test
        @DisplayName("로그인 실패 - 아이디 누락")
        void loginFailureMissingUserId() throws Exception {
            // Given
            Map<String, Object> loginParams = new HashMap<>();
            loginParams.put("userPw", "testPassword");
            // userId 누락

            // When & Then
            mockMvc.perform(post("/api/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginParams)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is(false)))
                    .andExpect(jsonPath("$.message", is("아이디와 비밀번호를 모두 입력해주세요.")));
        }

        @Test
        @DisplayName("로그인 실패 - 비밀번호 누락")
        void loginFailureMissingPassword() throws Exception {
            // Given
            Map<String, Object> loginParams = new HashMap<>();
            loginParams.put("userId", "testUser");
            // userPw 누락

            // When & Then
            mockMvc.perform(post("/api/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginParams)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is(false)))
                    .andExpect(jsonPath("$.message", is("아이디와 비밀번호를 모두 입력해주세요.")));
        }

        @Test
        @DisplayName("로그인 실패 - 빈 파라미터")
        void loginFailureEmptyParameters() throws Exception {
            // Given
            Map<String, Object> loginParams = new HashMap<>();
            loginParams.put("userId", "");
            loginParams.put("userPw", "");

            // When & Then
            mockMvc.perform(post("/api/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginParams)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is(false)))
                    .andExpect(jsonPath("$.message", is("아이디와 비밀번호를 모두 입력해주세요.")));
        }
    }

    @Nested
    @DisplayName("데이터 API 테스트")
    class DataApiTest {
        private MockedStatic<EdelweisCrawler> mockedCrawler;
        private Cookie jsessionidCookie;

        @BeforeEach
        void setUp() {
            // 테스트 시작 전 EdelweisCrawler의 static 메소드를 모킹
            mockedCrawler = mockStatic(EdelweisCrawler.class);
            jsessionidCookie = new Cookie("JSESSIONID", MOCK_JSESSIONID);
        }

        @AfterEach
        void tearDown() {
            // 테스트 종료 후 모킹 해제
            mockedCrawler.close();
        }

        @Test
        @DisplayName("데이터 조회 성공")
        void getDataSuccess() throws Exception {
            // Given
            // checkLoginStatus가 성공 상태를 반환하도록 모킹
            mockedCrawler.when(() -> EdelweisCrawler.checkLoginStatus(MOCK_JSESSIONID))
                    .thenReturn(new EdelweisCrawler.LoginStatus(true, ""));

            // getClassList 메소드 모킹
            ArrayList<EdelweisCrawler.Class> mockClassList = new ArrayList<>();
            mockClassList.add(new EdelweisCrawler.Class("테스트 클래스", 3.0f, "전공", "12345", "67890", "11121", "LT"));
            mockedCrawler.when(() -> EdelweisCrawler.getClassList(MOCK_JSESSIONID))
                    .thenReturn(mockClassList);

            // getClassroom 메소드 모킹
            Connection.Response mockResponse = mock(Connection.Response.class);
            mockedCrawler.when(() -> EdelweisCrawler.getClassroom(anyString(), any(EdelweisCrawler.Class.class)))
                    .thenReturn(mockResponse);

            // getBoardList 메소드 모킹
            ArrayList<EdelweisCrawler.Board> mockBoardList = new ArrayList<>();
            mockBoardList.add(new EdelweisCrawler.Board("공지사항", "/test/url", "12345", "notice"));
            mockedCrawler.when(() -> EdelweisCrawler.getBoardList(any()))
                    .thenReturn(mockBoardList);

            // getPostList 메소드 모킹
            ArrayList<EdelweisCrawler.Post> mockPostList = new ArrayList<>();
            mockPostList.add(new EdelweisCrawler.Post("/test/post/url", "테스트 공지", "98765", "작성자", "2023-12-31"));
            mockedCrawler.when(() -> EdelweisCrawler.getPostList(anyString(), any(EdelweisCrawler.Board.class), any(EdelweisCrawler.Class.class)))
                    .thenReturn(mockPostList);

            // getPostData 메소드 모킹
            EdelweisCrawler.PostData mockPostData = new EdelweisCrawler.PostData("테스트 공지", "작성자", "2023-12-31", "일반", 10, "테스트 내용입니다.");
            mockedCrawler.when(() -> EdelweisCrawler.getPostData(anyString(), anyString(), any(EdelweisCrawler.Class.class), any(EdelweisCrawler.Post.class)))
                    .thenReturn(mockPostData);

            // When & Then
            mockMvc.perform(post("/api/data")
                            .cookie(jsessionidCookie))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status", is(true)))
                    .andExpect(jsonPath("$.message", is("데이터 요청 성공")))
                    .andExpect(jsonPath("$.classData").exists());
            // 테스트 클래스 이름에 공백이 있으면 JSONPath에서 문제가 발생할 수 있으므로 경로 표현식 수정
        }

        @Test
        @DisplayName("데이터 조회 실패 - 로그인 안됨 (쿠키 없음)")
        void getDataFailureNotLoggedIn() throws Exception {
            // Given - 쿠키 없이 요청

            // When & Then
            mockMvc.perform(post("/api/data"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is(false)))
                    .andExpect(jsonPath("$.message", is("로그인 되어있지 않습니다. 다시 로그인해주세요.")));
        }

        @Test
        @DisplayName("데이터 조회 실패 - 로그인 만료")
        void getDataFailureSessionExpired() throws Exception {
            // Given
            // checkLoginStatus가 로그인 만료 상태를 반환하도록 모킹
            mockedCrawler.when(() -> EdelweisCrawler.checkLoginStatus(MOCK_JSESSIONID))
                    .thenReturn(new EdelweisCrawler.LoginStatus(false, "로그인 만료"));

            // When & Then
            mockMvc.perform(post("/api/data")
                            .cookie(jsessionidCookie))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is(false)))
                    .andExpect(jsonPath("$.message", is("로그인이 만료되었습니다. 다시 로그인해주세요.")));
        }

        @Test
        @DisplayName("데이터 조회 실패 - 클래스 목록 가져오기 예외")
        void getDataFailureClassListException() throws Exception {
            // Given
            // checkLoginStatus가 성공 상태를 반환하도록 모킹
            mockedCrawler.when(() -> EdelweisCrawler.checkLoginStatus(MOCK_JSESSIONID))
                    .thenReturn(new EdelweisCrawler.LoginStatus(true, ""));

            // getClassList에서 예외 발생하도록 모킹
            mockedCrawler.when(() -> EdelweisCrawler.getClassList(MOCK_JSESSIONID))
                    .thenThrow(new RuntimeException("테스트 예외"));

            // When & Then
            mockMvc.perform(post("/api/data")
                            .cookie(jsessionidCookie))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is(false)))
                    .andExpect(jsonPath("$.message", is("클래스 데이터를 가져오는 중 오류가 발생했습니다.")));
        }
    }
}

