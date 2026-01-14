package com.example.oms.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.Valid;
import java.util.List;

@Getter
@Setter
public class OrderCreateRequest {

    private Long userId;
    @NotEmpty
    @Valid
    private List<OrderItemRequest> items;
}
