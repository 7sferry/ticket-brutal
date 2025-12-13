package org.example.ticketbrutal.core;

import lombok.RequiredArgsConstructor;
import org.example.ticketbrutal.entity.TicketBooking;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/************************
 * Made by [MR Ferry™]  *
 * on Desember 2025     *
 ************************/

@Service
@RequiredArgsConstructor
public class TicketBookEventPublisher{

	private final StringRedisTemplate redisTemplate;

	public void publish(TicketBooking booking) {
		Map<String, String> payload = new HashMap<>();
		payload.put("type", RedisKeys.TICKET_BOOK_CHANNEL);
		payload.put("eventId", booking.getEventId());
		payload.put("ticketId", String.valueOf(booking.getId()));
		payload.put("quantity", String.valueOf(booking.getBookedQuantity()));

		redisTemplate.opsForStream()
				.add(RedisKeys.TICKET_BOOK_CHANNEL, payload);
	}

}
