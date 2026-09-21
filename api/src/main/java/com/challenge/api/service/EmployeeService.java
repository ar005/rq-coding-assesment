package com.challenge.api.service;

import com.challenge.api.exception.EmployeeNotFoundException;
import com.challenge.api.model.Employee;
import com.challenge.api.model.EmployeeImpl;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmployeeService {

    private final Map<UUID, Employee> employeeStore = new ConcurrentHashMap<>();

    public EmployeeService() {
        seedMockEmployees();
        log.info("Seeded {} mock employees", employeeStore.size());
    }

    public List<Employee> getAllEmployees() {
        List<Employee> employees = List.copyOf(employeeStore.values());
        log.debug("Fetched {} employees from store", employees.size());
        return employees;
    }

    public Employee getEmployeeByUuid(UUID uuid) {
        Employee employee = employeeStore.get(uuid);
        if (employee == null) {
            log.warn("No employee found for uuid: {}", uuid);
            throw new EmployeeNotFoundException("No employee found for uuid: " + uuid);
        }
        log.debug("Found employee for uuid: {}", uuid);
        return employee;
    }

    @Retryable(retryFor = RuntimeException.class, maxAttempts = 3, backoff = @Backoff(delay = 200, multiplier = 2))
    public Employee createEmployee(
            String firstName, String lastName, Integer salary, Integer age, String jobTitle, String email) {
        Employee employee = EmployeeImpl.builder()
                .uuid(UUID.randomUUID())
                .firstName(firstName)
                .lastName(lastName)
                .fullName(firstName + " " + lastName)
                .salary(salary)
                .age(age)
                .jobTitle(jobTitle)
                .email(email)
                .contractHireDate(Instant.now())
                .build();
        employeeStore.put(employee.getUuid(), employee);
        log.info("Created employee {} with uuid {}", employee.getFullName(), employee.getUuid());
        return employee;
    }

    private void seedMockEmployees() {
        createEmployee("Ada", "Lovelace", 120_000, 32, "Software Engineer", "ada.lovelace@example.com");
        createEmployee("Alan", "Turing", 135_000, 35, "Principal Engineer", "alan.turing@example.com");
        createEmployee("Grace", "Hopper", 128_000, 40, "Engineering Manager", "grace.hopper@example.com");
    }
}
