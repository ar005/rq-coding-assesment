package com.challenge.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.challenge.api.exception.EmployeeNotFoundException;
import com.challenge.api.model.Employee;
import com.challenge.api.model.EmployeeImpl;
import com.challenge.api.service.EmployeeService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Test
    void getAllEmployees_returnsListFromService() throws Exception {
        Employee employee = EmployeeImpl.builder()
                .uuid(UUID.randomUUID())
                .firstName("Ada")
                .lastName("Lovelace")
                .fullName("Ada Lovelace")
                .salary(120_000)
                .age(32)
                .jobTitle("Software Engineer")
                .email("ada.lovelace@example.com")
                .contractHireDate(Instant.now())
                .build();
        when(employeeService.getAllEmployees()).thenReturn(List.of(employee));

        mockMvc.perform(get("/api/v1/employee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].fullName").value("Ada Lovelace"));
    }

    @Test
    void getEmployeeByUuid_returns404BodyWhenServiceThrowsNotFound() throws Exception {
        UUID unknownUuid = UUID.randomUUID();
        when(employeeService.getEmployeeByUuid(unknownUuid))
                .thenThrow(new EmployeeNotFoundException("No employee found for uuid: " + unknownUuid));

        mockMvc.perform(get("/api/v1/employee/{uuid}", unknownUuid))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("No employee found for uuid: " + unknownUuid));
    }

    @Test
    void createEmployee_returns400BodyWithFieldErrorsForInvalidInput() throws Exception {
        String invalidBody =
                """
                {
                    "firstName": "",
                    "lastName": "Hamilton",
                    "salary": -5,
                    "age": 45,
                    "jobTitle": "Lead Engineer",
                    "email": "x@example.com"
                }
                """;

        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.firstName").exists())
                .andExpect(jsonPath("$.errors.salary").exists());
    }

    @Test
    void createEmployee_returns200WithCreatedEmployeeForValidInput() throws Exception {
        Employee created = EmployeeImpl.builder()
                .uuid(UUID.randomUUID())
                .firstName("Margaret")
                .lastName("Hamilton")
                .fullName("Margaret Hamilton")
                .salary(140_000)
                .age(45)
                .jobTitle("Lead Engineer")
                .email("margaret.hamilton@example.com")
                .contractHireDate(Instant.now())
                .build();
        when(employeeService.createEmployee(any(), any(), any(), any(), any(), any()))
                .thenReturn(created);

        String validBody =
                """
                {
                    "firstName": "Margaret",
                    "lastName": "Hamilton",
                    "salary": 140000,
                    "age": 45,
                    "jobTitle": "Lead Engineer",
                    "email": "margaret.hamilton@example.com"
                }
                """;

        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Margaret Hamilton"));
    }
}
