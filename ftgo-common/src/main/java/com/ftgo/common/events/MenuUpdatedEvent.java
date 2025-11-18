package com.ftgo.common.events;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class MenuUpdatedEvent extends DomainEvent {
    private String restaurantId;
    private List<MenuItemInfo> menuItems;

    public MenuUpdatedEvent(String restaurantId, List<MenuItemInfo> menuItems) {
        super(restaurantId, "Restaurant");
        this.restaurantId = restaurantId;
        this.menuItems = menuItems;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class MenuItemInfo {
        private String menuItemId;
        private String name;
        private String price;
        private String currency;
    }
}

