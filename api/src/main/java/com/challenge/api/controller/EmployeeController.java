package com.challenge.api.controller;

import com.challenge.api.model.Employee;
import com.challenge.api.model.dto.CreateEmployeeRequest;
import com.challenge.api.service.EmployeeService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/employee")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public List<Employee> getAllEmployees() {
        log.info("Received request to get all employees");
        List<Employee> employees = employeeService.getAllEmployees();
        log.debug("Returning {} employees", employees.size());
        return employees;
    }

    @GetMapping("/{uuid}")
    public Employee getEmployeeByUuid(@PathVariable UUID uuid) {
        log.info("Received request to get employee by uuid: {}", uuid);
        return employeeService.getEmployeeByUuid(uuid);
    }

    @PostMapping
    public Employee createEmployee(@Valid @RequestBody CreateEmployeeRequest requestBody) {
        log.info("Received request to create employee: {} {}", requestBody.firstName(), requestBody.lastName());
        Employee created = employeeService.createEmployee(
                requestBody.firstName(),
                requestBody.lastName(),
                requestBody.salary(),
                requestBody.age(),
                requestBody.jobTitle(),
                requestBody.email());
        log.info("Created employee with uuid: {}", created.getUuid());
        return created;
    }
}
