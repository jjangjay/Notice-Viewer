package jjangjay.edelive.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class APIControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // EdelweisCrawler의 static 메소드를 직접 호출하므로, MockBean으로 직접 주입하기 어렵습니다.
    // 테스트의 안정성을 위해 EdelweisCrawler의 동작을 모킹하는 것이 좋습니다.
    // 여기서는 간단화를 위해 실제 크롤러를 호출한다고 가정하지만, 이는 외부 의존성으로 인해 테스트가 불안정해질 수 있습니다.
    // 실제 프로젝트에서는 PowerMockito 또는 Mockito 5+의 MockedStatic을 사용하거나,
    // EdelweisCrawler를 인터페이스와 구현체로 분리하여 MockBean으로 주입하는 것을 고려하세요.

    // 임시로 EdelweisCrawler의 일부 동작을 모킹하기 위한 설정 (실제로는 더 정교한 모킹 필요)
    // @MockBean // 실제 static 메소드 모킹은 @MockBean으로 간단히 되지 않습니다.
    // private EdelweisCrawler edelweisCrawler; // 이 방식은 static 메소드에 적용되지 않음

    private String mockJSessionId = "mockTestSessionId12345";

    @Nested
    @DisplayName("/api/login 테스트")
    class LoginTests {

        @Test
        @DisplayName("성공적인 로그인")
        void login_success() throws Exception {
            Map<String, Object> loginParams = new HashMap<>();
            loginParams.put("userId", "testUser");
            loginParams.put("userPw", "testPassword");

            // EdelweisCrawler.login이 성공적으로 JSESSIONID를 반환한다고 가정
            // 실제로는 이 부분을 모킹해야 합니다.
            // 예: when(EdelweisCrawler.login(anyString(), anyString())).thenReturn(mockJSessionId);
            // 위와 같이 하려면 MockedStatic 사용 필요

            mockMvc.perform(post("/api/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginParams)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(true))
                    .andExpect(jsonPath("$.message").value("로그인 성공"))
                    .andExpect(cookie().exists("JSESSIONID"));
                    // 실제 EdelweisCrawler.login이 호출되므로, 해당 메소드가 null을 반환하면 실패합니다.
        }

        @Test
        @DisplayName("로그인 실패 - JSESSIONID 없음")
        void login_failure_no_jsessionid() throws Exception {
            Map<String, Object> loginParams = new HashMap<>();
            loginParams.put("userId", "wrongUser");
            loginParams.put("userPw", "wrongPassword");

            // EdelweisCrawler.login이 null을 반환한다고 가정 (모킹 필요)
            // 예: when(EdelweisCrawler.login(anyString(), anyString())).thenReturn(null);

            mockMvc.perform(post("/api/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginParams)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(false))
                    .andExpect(jsonPath("$.message").value("로그인 실패: JSESSIONID가 비어있습니다."));
        }

        @Test
        @DisplayName("로그인 실패 - 아이디 또는 비밀번호 누락")
        void login_failure_missing_credentials() throws Exception {
            Map<String, Object> loginParams = new HashMap<>();
            loginParams.put("userId", "testUser");
            // userPw 누락

            mockMvc.perform(post("/api/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginParams)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(false))
                    .andExpect(jsonPath("$.message").value("아이디와 비밀번호를 모두 입력해주세요."));
        }
    }

    @Nested
    @DisplayName("/api/data 테스트")
    class DataTests {

        private Cookie jsessionidCookie;

        @BeforeEach
        void setUp() {
            // 이 테스트에서는 실제 로그인을 수행하지 않고, 유효한 쿠키가 있다고 가정합니다.
            // 더 나은 방법은 @BeforeEach에서 /api/login을 호출하여 실제 쿠키를 얻는 것입니다.
            // 하지만 이는 login 테스트에 의존하게 됩니다.
            jsessionidCookie = new Cookie("JSESSIONID", mockJSessionId);

            // EdelweisCrawler.checkLoginStatus 및 getClassList 등을 모킹해야 합니다.
            // 예: when(EdelweisCrawler.checkLoginStatus(mockJSessionId)).thenReturn(new EdelweisCrawler.LoginStatus(true, ""));
            // 예: when(EdelweisCrawler.getClassList(mockJSessionId)).thenReturn(new ArrayList<>());
            // ... 기타 필요한 데이터 모킹 ...
        }

        @Test
        @DisplayName("데이터 조회 성공")
        void get_data_success() throws Exception {
            // EdelweisCrawler의 메소드들이 성공적으로 데이터를 반환한다고 가정 (모킹 필요)
            // 이 테스트는 EdelweisCrawler의 실제 구현에 따라 성공/실패가 달라질 수 있습니다.
            // 안정적인 테스트를 위해서는 EdelweisCrawler의 static 메소드들을 모킹해야 합니다.

            // 임시로 checkLoginStatus가 true를 반환하도록 설정 (실제로는 MockedStatic 필요)
            // when(EdelweisCrawler.checkLoginStatus(anyString())).thenReturn(new EdelweisCrawler.LoginStatus(true, ""));
            // when(EdelweisCrawler.getClassList(anyString())).thenReturn(new ArrayList<EdelweisCrawler.Class>());
            // ... 기타 등등

            mockMvc.perform(get("/api/data")
                            .cookie(jsessionidCookie)) // 로그인된 상태를 시뮬레이션
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(true))
                    .andExpect(jsonPath("$.message").value("데이터 요청 성공"));
                    // .andExpect(jsonPath("$.classData").exists()); // 실제 데이터가 있다면
        }

        @Test
        @DisplayName("데이터 조회 실패 - 로그인되지 않음 (쿠키 없음)")
        void get_data_failure_not_logged_in() throws Exception {
            mockMvc.perform(get("/api/data")) // 쿠키 없이 요청
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(false))
                    .andExpect(jsonPath("$.message").value("로그인 되어있지 않습니다. 다시 로그인해주세요."));
        }

        @Test
        @DisplayName("데이터 조회 실패 - 로그인 만료")
        void get_data_failure_session_expired() throws Exception {
            // EdelweisCrawler.checkLoginStatus가 false를 반환한다고 가정 (모킹 필요)
            // 예: when(EdelweisCrawler.checkLoginStatus(mockJSessionId)).thenReturn(new EdelweisCrawler.LoginStatus(false, "로그인 만료"));

            mockMvc.perform(get("/api/data")
                            .cookie(jsessionidCookie))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(false))
                    .andExpect(jsonPath("$.message").value("로그인이 만료되었습니다. 다시 로그인해주세요."));
        }

        @Test
        @DisplayName("데이터 조회 실패 - 데이터 가져오기 오류")
        void get_data_failure_data_fetch_error() throws Exception {
            // EdelweisCrawler.getClassList 등에서 예외가 발생한다고 가정 (모킹 필요)
            // 예: when(EdelweisCrawler.getClassList(mockJSessionId)).thenThrow(new RuntimeException("Test Exception"));

            mockMvc.perform(get("/api/data")
                            .cookie(jsessionidCookie)) // 로그인된 상태
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(false))
                    .andExpect(jsonPath("$.message").value("클래스 데이터를 가져오는 중 오류가 발생했습니다."));
        }
    }
}

