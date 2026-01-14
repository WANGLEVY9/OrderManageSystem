package com.example.oms.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class UserUpdateRequest {

    private String username;

    @NotNull
    private Boolean enabled;

    private Set<String> roleCodes;
}
