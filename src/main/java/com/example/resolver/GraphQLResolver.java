package com.example.resolver;

import com.example.model.Message;
import com.example.repository.MessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Sinks;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Controller
@Slf4j
public class GraphQLResolver {

    private final Sinks.Many<Message> messageSink = Sinks.many().multicast().onBackpressureBuffer();
    private final Sinks.Many<List<Message>> messagesListSink = Sinks.many().multicast().onBackpressureBuffer();
    private final List<Message> messageStore = Collections.synchronizedList(new ArrayList<>());
    private final MessageRepository messageRepository;

    public GraphQLResolver(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @QueryMapping
    public String hello() {
        System.out.println("Resolver hello chamado");
        return "Hello, GraphQL!";
    }

    @QueryMapping
    public List<Message> messages() {

        log.info("Resolver messages chamado");
        List<Message> currentMessages = messageRepository.findAll();
        messagesListSink.tryEmitNext(currentMessages);
        return currentMessages;
    }

    @MutationMapping
    public Message sendMessage(@Argument String content) {
        log.info("Resolver sendMessage chamado com content: {}", content);
        Message message = new Message();
        message.setId(UUID.randomUUID().toString());
        message.setContent(content);
        message.setTimestamp(LocalDateTime.now().toString());

        Message result = messageRepository.save(message);

        Sinks.EmitResult messageResult = messageSink.tryEmitNext(result);
        log.info("Emitindo para messageSink: {} ",result);

        return message;
    }

    @SubscriptionMapping
    public Publisher<Message> messageAdded() {
        log.info("Resolver messageAdded chamado");
        return messageSink.asFlux();
    }

    @SubscriptionMapping
    public Publisher<List<Message>> messagesListed() {
        log.info("esolver messagesListed chamado");
        return messagesListSink.asFlux();
    }
}