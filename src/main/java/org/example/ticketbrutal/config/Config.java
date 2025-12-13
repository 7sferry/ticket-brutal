package org.example.ticketbrutal.config;

import org.example.ticketbrutal.core.RedisKeys;
import org.example.ticketbrutal.core.TicketBookSubscriber;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.ObjectMapper;

/************************
 * Made by [MR Ferry™]  *
 * on Desember 2025     *
 ************************/

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

}
