package org.example.ticketbrutal.config;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.example.ticketbrutal.core.RedisKeys;
import org.example.ticketbrutal.core.TicketBookConsumer;
import org.example.ticketbrutal.core.TicketBookSubscriber;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions;
import tools.jackson.databind.ObjectMapper;

import java.net.InetAddress;
import java.time.Duration;

/************************
 * Made by [MR Ferry™]  *
 * on Desember 2025     *
 ************************/

@Slf4j
@Configuration
public class Config {

	@Bean
	public RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory, TicketBookSubscriber subscriber) {

		RedisMessageListenerContainer container =
				new RedisMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);

		container.addMessageListener(
				subscriber,
				new ChannelTopic(RedisKeys.TICKET_BOOK_CHANNEL)
		);

		return container;
	}

	@Bean
	RedisOperations<String, Object> redisTemplate(RedisConnectionFactory factory, ObjectMapper mapper){
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(factory);
		GenericJacksonJsonRedisSerializer jsonSerializer = new GenericJacksonJsonRedisSerializer(mapper);
		StringRedisSerializer stringSerializer = new StringRedisSerializer();
		template.setKeySerializer(stringSerializer);
		template.setValueSerializer(jsonSerializer);
		template.setHashKeySerializer(stringSerializer);
		template.setHashValueSerializer(jsonSerializer);
		return template;
	}

	@Bean
	public StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamContainer(RedisConnectionFactory factory, TicketBookConsumer consumer) {

		StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options = StreamMessageListenerContainerOptions
				.builder()
				.pollTimeout(Duration.ofSeconds(2))
				.errorHandler(t -> log.error("Stream listener error", t))
				.build();

		StreamMessageListenerContainer<String, MapRecord<String, String, String>> container =
				StreamMessageListenerContainer.create(factory, options);

		container.receive(
				Consumer.from(RedisKeys.TICKET_SERVICES_GROUP, getName()),
				StreamOffset.create(RedisKeys.TICKET_BOOK_CHANNEL, ReadOffset.lastConsumed()),
				consumer
		);

		container.start();
		return container;
	}

	@SneakyThrows
	private static String getName(){
		return InetAddress.getLocalHost().getHostName();
	}

}
