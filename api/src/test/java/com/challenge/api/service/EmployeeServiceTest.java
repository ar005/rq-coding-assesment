package com.challenge.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.challenge.api.exception.EmployeeNotFoundException;
import com.challenge.api.model.Employee;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EmployeeServiceTest {

    private EmployeeService employeeService;

    @BeforeEach
    void setUp() {
        employeeService = new EmployeeService();
    }

    @Test
    void getAllEmployees_returnsSeededMockEmployeesOnStartup() {
        List<Employee> employees = employeeService.getAllEmployees();

        assertThat(employees).hasSize(3);
        assertThat(employees)
                .extracting(Employee::getFullName)
                .containsExactlyInAnyOrder("Ada Lovelace", "Alan Turing", "Grace Hopper");
    }

    @Test
    void getAllEmployees_returnsAnImmutableSnapshot() {
        List<Employee> employees = employeeService.getAllEmployees();

        assertThat(employees).hasSize(3);
        assertThatThrownBy(employees::clear).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void createEmployee_generatesUuidAndFullNameAndHireDate() {
        Employee created = employeeService.createEmployee(
                "Margaret", "Hamilton", 140_000, 45, "Lead Engineer", "margaret.hamilton@example.com");

        assertThat(created.getUuid()).isNotNull();
        assertThat(created.getFirstName()).isEqualTo("Margaret");
        assertThat(created.getLastName()).isEqualTo("Hamilton");
        assertThat(created.getFullName()).isEqualTo("Margaret Hamilton");
        assertThat(created.getSalary()).isEqualTo(140_000);
        assertThat(created.getAge()).isEqualTo(45);
        assertThat(created.getJobTitle()).isEqualTo("Lead Engineer");
        assertThat(created.getEmail()).isEqualTo("margaret.hamilton@example.com");
        assertThat(created.getContractHireDate()).isNotNull();
        assertThat(created.getContractTerminationDate()).isNull();
    }

    @Test
    void createEmployee_assignsDifferentUuidsToEachCall() {
        Employee first = employeeService.createEmployee("A", "One", 100_000, 30, "Engineer", "a.one@example.com");
        Employee second = employeeService.createEmployee("B", "Two", 100_000, 30, "Engineer", "b.two@example.com");

        assertThat(first.getUuid()).isNotEqualTo(second.getUuid());
    }

    @Test
    void createEmployee_addsToStoreSoItIsReturnedByGetAllEmployees() {
        int sizeBeforeCreate = employeeService.getAllEmployees().size();

        Employee created = employeeService.createEmployee(
                "Katherine", "Johnson", 130_000, 50, "Mathematician", "katherine.johnson@example.com");

        List<Employee> employees = employeeService.getAllEmployees();
        assertThat(employees).hasSize(sizeBeforeCreate + 1);
        assertThat(employees).extracting(Employee::getUuid).contains(created.getUuid());
    }

    @Test
    void getEmployeeByUuid_returnsMatchingEmployeeWhenPresent() {
        Employee created = employeeService.createEmployee(
                "Grace", "Chisholm", 110_000, 38, "Engineer", "grace.chisholm@example.com");

        Employee found = employeeService.getEmployeeByUuid(created.getUuid());

        assertThat(found).isEqualTo(created);
    }

    @Test
    void getEmployeeByUuid_throwsWhenNoEmployeeMatches() {
        UUID unknownUuid = UUID.randomUUID();

        assertThatThrownBy(() -> employeeService.getEmployeeByUuid(unknownUuid))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessageContaining(unknownUuid.toString());
    }
}
