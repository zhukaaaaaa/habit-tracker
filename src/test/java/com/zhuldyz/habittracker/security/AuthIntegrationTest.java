package com.zhuldyz.habittracker.security;

import java.util.UUID;
import com.zhuldyz.habittracker.dto.AuthRequest;
import com.zhuldyz.habittracker.dto.AuthResponse;
import com.zhuldyz.habittracker.dto.DailyStatsResponse;
import com.zhuldyz.habittracker.dto.HabitRequest;
import com.zhuldyz.habittracker.dto.HabitResponse;
import com.zhuldyz.habittracker.dto.RegisterRequest;
import com.zhuldyz.habittracker.dto.RegisterResponse;
import com.zhuldyz.habittracker.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.assertj.core.api.Assertions.assertThat;

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
            authorized(new HabitRequest("Read", null, 7), token), HabitResponse.class);
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
        assertThat(http.exchange(path, HttpMethod.PUT, authorized(new HabitRequest("Someone else's habit", null, 1), bobToken), String.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(http.exchange(path, HttpMethod.DELETE, authorized(null, bobToken), String.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(http.exchange(path + "/records", HttpMethod.POST, authorized(null, bobToken), String.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        var daily = http.exchange("/api/stats/daily", HttpMethod.GET, authorized(null, bobToken), DailyStatsResponse.class);
        assertThat(daily.getBody().habits()).extracting(DailyStatsResponse.HabitStatus::id).containsExactly(bobHabit);
    }

    @Test void unsupportedAcceptReturns406ForAuthenticatedHabitRequest() {
        String name = username();
        register(name);
        String token = login(name);
        Long habit = create(token);

        var headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_XML));
        var response = http.exchange("/api/habits/" + habit, HttpMethod.GET,
            new HttpEntity<Void>(headers), Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_ACCEPTABLE);
    }
}
