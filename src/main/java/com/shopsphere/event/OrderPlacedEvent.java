package com.shopsphere.event;

import com.shopsphere.dto.OrderResponse;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OrderPlacedEvent extends ApplicationEvent { //makes this spring application event

    private final OrderResponse orderResponse;
    public OrderPlacedEvent(Object source, OrderResponse orderResponse) {
        super(source);
        this.orderResponse = orderResponse;
    }
}
