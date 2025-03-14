package com.example.resolver;

import com.example.model.Message;
import org.reactivestreams.Publisher;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Sinks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Controller
public class GraphQLResolver {

    private final Sinks.Many<Message> messageSink = Sinks.many().multicast().onBackpressureBuffer();
    private final Sinks.Many<List<Message>> messagesListSink = Sinks.many().multicast().onBackpressureBuffer();
    private final List<Message> messageStore = Collections.synchronizedList(new ArrayList<>());

    @QueryMapping
    public String hello() {
        System.out.println("Resolver hello chamado");
        return "Hello, GraphQL!";
    }

    @QueryMapping
    public List<Message> messages() {
        System.out.println("Resolver messages chamado");
        List<Message> currentMessages = new ArrayList<>(messageStore);
        messagesListSink.tryEmitNext(currentMessages);
        return currentMessages;
    }

    @MutationMapping
    public Message sendMessage(@Argument String content) {
        System.out.println("Resolver sendMessage chamado com content: " + content);
        Message message = new Message(UUID.randomUUID().toString(), content);
        messageStore.add(message);

        Sinks.EmitResult messageResult = messageSink.tryEmitNext(message);
        System.out.println("Emitindo para messageSink: " + messageResult);
        Sinks.EmitResult listResult = messagesListSink.tryEmitNext(new ArrayList<>(messageStore));
        System.out.println("Emitindo para messagesListSink na mutation: " + listResult);

       /* messageSink.tryEmitNext(message);
        messagesListSink.tryEmitNext(new ArrayList<>(messageStore));*/
        return message;
    }

    @SubscriptionMapping
    public Publisher<Message> messageAdded() {
        System.out.println("Resolver messageAdded chamado");
        return messageSink.asFlux();
    }

    @SubscriptionMapping
    public Publisher<List<Message>> messagesListed() {
        System.out.println("Resolver messagesListed chamado");
        return messagesListSink.asFlux();
    }
}