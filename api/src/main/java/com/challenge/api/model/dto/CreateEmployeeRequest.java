package com.challenge.api.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateEmployeeRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotNull @Positive Integer salary,
        @NotNull @Positive Integer age,
        @NotBlank String jobTitle,
        @NotBlank String email) {}
