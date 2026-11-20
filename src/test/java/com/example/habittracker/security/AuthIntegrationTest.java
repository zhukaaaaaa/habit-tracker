package com.example.habittracker.security;

import java.util.UUID;
import java.util.Map;
import com.example.habittracker.dto.*;
import com.example.habittracker.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "spring.datasource.url=jdbc:h2:mem:auth-tests;DB_CLOSE_DELAY=-1")
@AutoConfigureTestRestTemplate
class AuthIntegrationTest {
    private static final String PASSWORD = "learn-java-21";
    @Autowired TestRestTemplate http;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder encoder;

    private String username() { return "test_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12); }
    private void register(String name) {
        var response = http.postForEntity("/api/auth/register", new RegisterRequest(name, PASSWORD, name + "@example.test"), RegisterResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }
    private String login(String name) {
        var response = http.postForEntity("/api/auth/login", new AuthRequest(name, PASSWORD), AuthResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return response.getBody().token();
    }
    private <T> HttpEntity<T> authorized(T body, String token) {
        var headers = new HttpHeaders(); headers.setBearerAuth(token);
        return new HttpEntity<>(body, headers);
    }
    private Long create(String token) {
        var response = http.exchange("/api/habits", HttpMethod.POST,
            authorized(new HabitRequest("Читать", null, 7), token), HabitResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody().id();
    }

    @Test void registrationStoresBcryptHash() {
        String name = username(); register(name);
        var saved = users.findByUsername(name).orElseThrow();
        assertThat(saved.getPassword()).isNotEqualTo(PASSWORD);
        assertThat(encoder.matches(PASSWORD, saved.getPassword())).isTrue();
    }
    @Test void validLoginReturnsUsableToken() {
        String name = username(); register(name);
        String token = login(name);
        assertThat(token.split("\\.")).hasSize(3);
        assertThat(http.exchange("/api/habits", HttpMethod.GET, authorized(null, token), String.class).getStatusCode()).isEqualTo(HttpStatus.OK);
    }
    @Test void wrongPasswordReturns401() {
        String name = username(); register(name);
        assertThat(http.postForEntity("/api/auth/login", new AuthRequest(name, "wrong-password"), String.class).getStatusCode())
            .isEqualTo(HttpStatus.UNAUTHORIZED);
    }
    @Test void missingTokenReturns401() {
        assertThat(http.getForEntity("/api/habits", String.class).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
    @Test void invalidTokenReturns401Json() {
        var response = http.exchange("/api/habits", HttpMethod.GET, authorized(null, "invalid.token.signature"), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getHeaders().getContentType()).isNotNull();
        assertThat(response.getBody()).contains("401");
    }
    @Test void usersCannotReadChangeDeleteOrCompleteEachOthersHabits() {
        String alice = username(), bob = username(); register(alice); register(bob);
        String aliceToken = login(alice), bobToken = login(bob);
        Long aliceHabit = create(aliceToken), bobHabit = create(bobToken);
        var aliceList = http.exchange("/api/habits", HttpMethod.GET, authorized(null, aliceToken), HabitResponse[].class);
        assertThat(aliceList.getBody()).extracting(HabitResponse::id).containsExactly(aliceHabit);
        var bobList = http.exchange("/api/habits", HttpMethod.GET, authorized(null, bobToken), HabitResponse[].class);
        assertThat(bobList.getBody()).extracting(HabitResponse::id).containsExactly(bobHabit);
        String path = "/api/habits/" + aliceHabit;
        for (String suffix : new String[]{"", "/records", "/stats"}) {
            assertThat(http.exchange(path + suffix, HttpMethod.GET, authorized(null, bobToken), String.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
        assertThat(http.exchange(path, HttpMethod.PUT, authorized(new HabitRequest("Чужая", null, 1), bobToken), String.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(http.exchange(path, HttpMethod.DELETE, authorized(null, bobToken), String.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(http.exchange(path + "/records", HttpMethod.POST, authorized(null, bobToken), String.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        var daily = http.exchange("/api/stats/daily", HttpMethod.GET, authorized(null, bobToken), DailyStatsResponse.class);
        assertThat(daily.getBody().habits()).extracting(DailyStatsResponse.HabitStatus::id).containsExactly(bobHabit);
    }
    @Test void duplicateUsernameReturns409() {
        String name = username(); register(name);
        var response = http.postForEntity("/api/auth/register", new RegisterRequest(name, PASSWORD, null), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }
    @Test void completionCanOnlyBeRecordedOncePerDay() {
        String name = username(); register(name); String token = login(name);
        String path = "/api/habits/" + create(token) + "/records";
        assertThat(http.exchange(path, HttpMethod.POST, authorized(null, token), String.class).getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(http.exchange(path, HttpMethod.POST, authorized(null, token), String.class).getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }
    @Test void invalidHabitReturns400() {
        String name = username(); register(name); String token = login(name);
        var response = http.exchange("/api/habits", HttpMethod.POST, authorized(new HabitRequest(" ", null, 0), token), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
