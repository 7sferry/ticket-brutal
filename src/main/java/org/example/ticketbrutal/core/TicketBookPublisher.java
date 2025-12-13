package org.example.ticketbrutal.core;

import lombok.RequiredArgsConstructor;
import org.example.ticketbrutal.entity.TicketBooking;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.stereotype.Service;

/************************
 * Made by [MR Ferry™]  *
 * on Desember 2025     *
 ************************/

@Service
@RequiredArgsConstructor
public class TicketBookPublisher{

	private final RedisOperations<String, Object> redisTemplate;

	public void publish(TicketBooking booking) {
		redisTemplate.convertAndSend(RedisKeys.TICKET_BOOK_CHANNEL, booking);
	}

}
