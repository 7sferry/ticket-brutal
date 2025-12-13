package org.example.ticketbrutal.core;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/************************
 * Made by [MR Ferry™]  *
 * on Desember 2025     *
 ************************/

@Component
@RequiredArgsConstructor
public class TicketBookConsumer implements StreamListener<String, MapRecord<String, String, String>>{

	private final StringRedisTemplate redisTemplate;

	@Override
	public void onMessage(MapRecord<String, String, String> message){

		Map<String, String> data = message.getValue();
		String type = data.get("type");

		String eventId = data.get("eventId");
		long ticketId = Long.parseLong(data.get("ticketId"));
		int quantity = Integer.parseInt(data.get("quantity"));

		System.out.println("Processing ticket release: " + ticketId);

		redisTemplate.opsForStream().acknowledge(
				RedisKeys.TICKET_SERVICES_GROUP,
				message
		);
	}
}
