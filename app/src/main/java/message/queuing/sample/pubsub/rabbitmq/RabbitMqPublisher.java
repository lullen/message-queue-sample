package message.queuing.sample.pubsub.rabbitmq;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.stereotype.Component;
import message.queuing.sample.proxy.Empty;
import message.queuing.sample.proxy.Response;
import message.queuing.sample.pubsub.EventPublisher;
import message.queuing.sample.pubsub.models.Event;

@Component
public class RabbitMqPublisher implements EventPublisher {

    private RabbitMq rabbit;
    private Gson json;

    public RabbitMqPublisher(RabbitMq rabbit) {
        this.rabbit = rabbit;
        this.json = new GsonBuilder().create();
    }

    @Override
    public Response<Empty> publish(Event event) throws IOException  {
        return publish(event, "");
    }

    @Override
    public Response<Empty> publish(Event event, String routingKey) throws IOException {
        var body = json.toJson(event).getBytes(StandardCharsets.UTF_8);
        System.out.println("Publishing with key " + routingKey);
        rabbit.getConnection().createChannel().basicPublish(getExchangeName(event.getClass()), routingKey, null, body);
        return new Response<Empty>(new Empty());
    }

    
    private String getExchangeName(Class<?> eventType) {
        var eventName = eventType.getSimpleName();
        return camelToSnake(eventName);
    }
    
    private static String camelToSnake(String str) {
        // Regular Expression
        String regex = "([a-z])([A-Z]+)";

        // Replacement string
        String replacement = "$1_$2";

        // Replace the given regex with replacement string and convert it to upper case.
        str = str.replaceAll(regex, replacement).toUpperCase();
        return str;
    }
    
}
