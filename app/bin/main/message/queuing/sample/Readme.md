# Message queuing

## Naming conventions

| Type | Variables | Name |
|------|-----------|------|
|Exchanges|ClassName|CLASS_NAME|
|Exchanges|AppId, ClassName, (RoutingKey)|APPID_CLASS_NAME[_ROUTINGKEY]|

---

## Exchange types
**Fanout** - Used when you want to let everyone get a copy of the event.

**Direct** - Used when you want to decide based on a routing key who should get the event.

**Topic** - Used when you want to decide based on a pattern who should get the event.

---

```java
@EventAnnotation(queueType = QueueType.Direct)
public class TestEvent implements Event {
    String text;
}

@BudbeeService
public interface TestInterface {
    @Subscribe(routingKey = "${TestEvent.RoutingKey}")
    EventResponse test(TestEvent b);

}

public class Test implements TestInterface {   
    public EventResponse test(TestEvent b) {

    }
}
```
Application.properties:

`spring.application.name = demo`

`TestEvent.routingKey=sv`

This will create an exchange called `TEST_EVENT` and a queue called `DEMO_TEST_EVENT_SV`



## Run demo

1. http://localhost:9090/connect
2. http://localhost:9090/publishroute?route=en
3. http://localhost:9090/publishroute?route=sv
4. http://localhost:9090/publishroute?route=dk
5. http://localhost:9090/consume
6. http://localhost:9090/disconnect
7. http://localhost:9090/publishroute?route=sv
8. http://localhost:9090/connect
9. http://localhost:9090/consume



## Todo:
Does not implement DLQ.
"If a queue is declared with the x-dead-letter-exchange property messages which is either rejected, nacked or the TTL for a message expires will be sent to the specified dead-letter-exchange, and if you specify x-dead-letter-routing-key the routing key of the message with be changed when dead lettered."
https://www.cloudamqp.com/docs/delayed-messages.html