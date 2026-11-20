package com.zhuldyz.habittracker.controller;

import java.time.LocalDateTime;
import com.zhuldyz.habittracker.dto.HabitResponse;
import com.zhuldyz.habittracker.exception.HabitNotFoundException;
import com.zhuldyz.habittracker.service.HabitService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = HabitController.class)
@AutoConfigureMockMvc(addFilters = false)
class HabitControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean HabitService service;
    private HabitResponse response() { return new HabitResponse(1L, "Water", "2 liters", 7, LocalDateTime.of(2026, 4, 20, 12, 0)); }

    @Test void existingHabitReturns200AndBody() throws Exception {
        when(service.getById(1L)).thenReturn(response());
        mvc.perform(get("/api/habits/1")).andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Water"));
    }
    @Test void missingHabitReturns404() throws Exception {
        when(service.getById(99L)).thenThrow(new HabitNotFoundException(99L));
        mvc.perform(get("/api/habits/99")).andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value("Habit not found: 99"));
    }
    @Test void validCreateReturns201AndLocation() throws Exception {
        when(service.create(any())).thenReturn(response());
        mvc.perform(post("/api/habits").contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"Water\",\"description\":\"2 liters\",\"target\":7}"))
            .andExpect(status().isCreated()).andExpect(header().string("Location", "/api/habits/1"))
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test void fractionalTargetReturns400() throws Exception {
        String body = """
            {"name":"Water","target":1.5}
            """;
        mvc.perform(post("/api/habits").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest());
        mvc.perform(put("/api/habits/1").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test void patchReturns405WithAllowedMethods() throws Exception {
        mvc.perform(patch("/api/habits/1"))
            .andExpect(status().isMethodNotAllowed())
            .andExpect(header().string(HttpHeaders.ALLOW,
                org.hamcrest.Matchers.containsString("PUT")));
    }
}
