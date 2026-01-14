package com.example.oms.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

@Getter
@Setter
public class OrderItemRequest {

    private Long productId;

    private String productName;

    @NotNull
    @Min(1)
    private Integer quantity;

    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal price;

    @AssertTrue(message = "productId or (productName and price) must be provided")
    public boolean isValidItem() {
        return productId != null || (StringUtils.hasText(productName) && price != null);
    }
}
