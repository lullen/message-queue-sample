package message.queuing.sample.api;

import java.io.IOException;
import java.io.InvalidClassException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import message.queuing.sample.interfaces.TestEvent;
import message.queuing.sample.interfaces.Work;
import message.queuing.sample.pubsub.BudbeeService;
import message.queuing.sample.pubsub.EventPublisher;
import message.queuing.sample.pubsub.ManualSubscriber;
import message.queuing.sample.pubsub.Subscriber;
import message.queuing.sample.pubsub.Ten_Times_Linear;
import message.queuing.sample.pubsub.models.EventResponse;
import message.queuing.sample.pubsub.models.ManualSubscription;

@RestController
@BudbeeService
public class TestController implements Work {
    private EventPublisher publisher;
    private ManualSubscriber manualSubscriber;
    private ApplicationContext applicationContext;

    private List<TestEvent> events = new ArrayList<>();

    public TestController(EventPublisher publisher, ManualSubscriber manualSubscriber,
            ApplicationContext applicationContext) {
        this.publisher = publisher;
        this.manualSubscriber = manualSubscriber;
        this.applicationContext = applicationContext;
    }

    @GetMapping("/service")
    public String run() {
        System.out.println(applicationContext.getId());
        return applicationContext.getId();
        // publisher.publish(new TestEvent("Ludwig", "Do work with this"));
        // return "hello";
    }

    @GetMapping("/publishroute")
    public String publishroute(@RequestParam String route) throws IOException {
        for (int i = 0; i < 1; i++) {
            publisher.publish(
                    new TestEvent(route, String.format("Message to the %s market", route)),
                    route);
        }
        return "published";
    }

    @GetMapping("/publish")
    public String publish() throws IOException {
        for (int i = 0; i < 1; i++) {
            publisher.publish(new TestEvent("No market", "Message without market"), "");
        }
        return "published";
    }

    @GetMapping("/connect")
    public String connect() throws IOException, Exception {
        var sub = new ManualSubscription(
                ManualSubscription.getMethod(TestController.class, "manual", TestEvent.class),
                "sv");
        manualSubscriber.connect(sub, TestEvent.class);
        return "Connected!";
    }

    @GetMapping("/disconnect")
    public String disconnect() throws IOException {
        manualSubscriber.disconnect(TestEvent.class, "sv");
        return "Disconnected!";
    }


    // @ManualSubscriber annotation??
    public EventResponse manual(TestEvent event) {
        System.out.println("Manual!");
        System.out.println(String.format("Doing hard work work. Name: %s, Text: %s",
                event.getName(), event.getText()));
        events.add(event);
        System.out.println("Done manual!");
        return new EventResponse();
    }

    @GetMapping("/consume")
    public List<TestEvent> consume() {
        var response = new ArrayList<TestEvent>(events);
        events = new ArrayList<TestEvent>();
        return response;
    }



    @Override
    @PostMapping("/service")
    @Subscriber(routingKey = "en", retryPolicy = Ten_Times_Linear.class, prefetch = 5, autoAck = false)
    @Subscriber(routingKey = "${doWork.routingKey}")
    public EventResponse doWork(TestEvent event) {
        System.out.println("Automatic!");
        System.out.println(String.format("Doing hard work work. Name: %s, Text: %s",
                event.getName(), event.getText()));
        System.out.println("Done automatic!");
        return new EventResponse(); // new Error(StatusCode.Exception, "errorMessage"));
    }


    @Override
    @Subscriber()
    public EventResponse noRoute(TestEvent event) throws InvalidClassException, IOException {
        System.out.println("No route!");
        System.out.println(String.format("Doing hard work work. Name: %s, Text: %s",
                event.getName(), event.getText()));
        System.out.println("Done no route!");
        // throw new InvalidClassException("Non transient fault");
        throw new IOException();
        // return new EventResponse(); // new Error(StatusCode.Exception, "errorMessage"));
    }
}
