package org.binary.bbl.controller;

import org.binary.bbl.common.GlobalExceptionHandler;
import org.binary.bbl.entity.User;
import org.binary.bbl.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void getUsers_shouldReturnUsers() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setName("Tanasat Tangudomkarn");
        user.setUsername("binarykung");
        user.setEmail("binary_kung@outlook.com");

        when(userService.getAllUsers()).thenReturn(List.of(user));

        mockMvc.perform(get("/users/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Tanasat Tangudomkarn"))
                .andExpect(jsonPath("$[0].username").value("binarykung"))
                .andExpect(jsonPath("$[0].email").value("binary_kung@outlook.com"));
    }

    @Test
    void getUserById_whenUserExists_shouldReturnUser() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setName("Tanasat Tangudomkarn");
        user.setUsername("binarykung");
        user.setEmail("binary_kung@outlook.com");

        when(userService.getUserById(1L)).thenReturn(user);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Tanasat Tangudomkarn"));
    }

    @Test
    void getUserById_whenUserDoesNotExist_shouldReturnNotFound() throws Exception {
        when(userService.getUserById(1L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("User not found"));
    }

    @Test
    void createUser_whenValidInput_shouldReturnCreated() throws Exception {
        String body = """
                {
                  "name": "Tanasat Tangudomkarn",
                  "username": "binarykung",
                  "email": "binary_kung@outlook.com",
                  "phone": "0830076641",
                  "website": "https://example2.com"
                }
                """;

        mockMvc.perform(post("/users/")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        verify(userService).createUser(any());
    }

    @Test
    void createUser_whenInvalidInput_shouldReturnBadRequest() throws Exception {
        String body = """
                {
                  "name": "",
                  "username": "",
                  "email": "not-an-email"
                }
                """;

        mockMvc.perform(post("/users/")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any());
    }

    @Test
    void createUser_whenDuplicate_shouldReturnConflict() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Username or email already exists"))
                .when(userService)
                .createUser(any());

        String body = """
                {
                  "name": "Tanasat Tangudomkarn",
                  "username": "binarykung",
                  "email": "binary_kung@outlook.com"
                }
                """;

        mockMvc.perform(post("/users/")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Username or email already exists"));
    }

    @Test
    void updateUser_whenValidInput_shouldReturnNoContent() throws Exception {
        String body = """
                {
                  "name": "Tanasat Tangudomkarn",
                  "username": "binarykung",
                  "email": "binary_kung@outlook.com"
                }
                """;

        mockMvc.perform(put("/users/1")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNoContent());

        verify(userService).updateUser(eq(1L), any());
    }

    @Test
    void deleteUser_whenUserExists_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
    }

    @Test
    void deleteUser_whenUserDoesNotExist_shouldReturnNotFound() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"))
                .when(userService)
                .deleteUser(1L);

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("User not found"));
    }
}