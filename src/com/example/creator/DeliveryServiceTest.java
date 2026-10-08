package com.example.creator;
import com.example.creator.delivery.DeliveryService;
public final class DeliveryServiceTest {
    public static void main(String[] args) {
        var service = new DeliveryService(null);
        var result = service.deliver(new DeliveryService.DeliveryRequest("a", "s", ""));
        if (!"FAILED".equals(result.status())) throw new AssertionError(result);
        System.out.println("PASS blank content is rejected before delivery");
    }
}
