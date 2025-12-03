package edu.dosw.rideci.application.port.out;

public interface EventPublisher {
    void publish(Object event, String routingKey);
}