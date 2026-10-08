package com.example.creator;
import com.example.creator.config.ServiceConfig;
import com.example.creator.delivery.DeliveryService;
import com.example.creator.infrai.InfraiClient;
public final class Main {
    public static void main(String[] args) {
        DeliveryService service = new DeliveryService(new InfraiClient(ServiceConfig.fromEnvironment()));
        var result = service.deliver(new DeliveryService.DeliveryRequest("asset-42", "subscriber-7", "processing-error"));
        System.out.println(result.status() + ": " + result.detail());
    }
}
