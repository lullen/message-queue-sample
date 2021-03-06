/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package message.queuing.sample;

import java.util.Arrays;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import message.queuing.sample.api.TestController;
import message.queuing.sample.pubsub.Subscriber;
import message.queuing.sample.pubsub.rabbitmq.RabbitMq;
import message.queuing.sample.pubsub.rabbitmq.RabbitMqListener;

@SpringBootApplication
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }


    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
            var rabbit = ctx.getBean(RabbitMq.class);
            rabbit.init();
            var listener = ctx.getBean(RabbitMqListener.class);
            listener.init();

            // var server = ctx.getBean(DaprServer.class);
            // server
            // .registerServices(ctx)
            // .start(5000)
            // .awaitTermination();
            // server.registerServices(List.of(HelloServer.class));
        };
    }


}
