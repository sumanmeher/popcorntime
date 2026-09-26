package com.popcorntime.org.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Value("${rabbitmq.email.queue.name}")
    private String emailQueueName;
    @Value("${rabbitmq.email.exchange.name}")
    private String emailExchangeName;
    @Value("${rabbitmq.email.routing.key}")
    private String emailRoutingKey;

    @Value("${rabbitmq.payment.details.unsaved.queue.name}")
    private String unsavedPaymentQueue;
    @Value("${rabbitmq.payment.details.unsaved.exchange.name}")
    private String unsavedPaymentExchange;
    @Value("${rabbitmq.payment.details.unsaved.routing.key}")
    private String unsavedPaymentRoutingKey;

    @Bean
    public Queue emailQueue() {
        return new Queue(emailQueueName, true);
    }

    @Bean
    public DirectExchange emailExchange() {
        return new DirectExchange(emailExchangeName);
    }

    @Bean
    public Binding emailBinding(Queue emailQueue, DirectExchange emailExchange) {
        return BindingBuilder.bind(emailQueue).to(emailExchange).with(emailRoutingKey);
    }

    @Bean
    public Queue unsavedPaymentQueue() {
        return new Queue(unsavedPaymentQueue, true);
    }

    @Bean
    public DirectExchange unsavedPaymentExchange() {
        return new DirectExchange(unsavedPaymentExchange);
    }

    @Bean
    public Binding unsavedPaymentBinding(Queue unsavedPaymentQueue, DirectExchange unsavedPaymentExchange) {
        return BindingBuilder.bind(unsavedPaymentQueue).to(unsavedPaymentExchange).with(unsavedPaymentRoutingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter);
        return rabbitTemplate;
    }
}
