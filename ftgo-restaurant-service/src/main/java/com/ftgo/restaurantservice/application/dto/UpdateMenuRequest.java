package com.ftgo.restaurantservice.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class UpdateMenuRequest {
    @NotEmpty(message = "Menu items cannot be empty")
    @Valid
    private List<MenuItemDTO> menuItems;
}

