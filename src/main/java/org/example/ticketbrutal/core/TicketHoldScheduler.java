package org.example.ticketbrutal.core;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Set;

/************************
 * Made by [MR Ferry™]  *
 * on Desember 2025     *
 ************************/

@Component
@RequiredArgsConstructor
public class TicketHoldScheduler {

	private final RedisOperations<String, Object> redis;
	private final TicketService service;

	@Scheduled(fixedDelay = 5000)
	public void releaseExpiredHolds() {
		long now = Instant.now().toEpochMilli();

		Set<Object> expired = redis.opsForZSet()
				.rangeByScore(
						RedisKeys.BOOK_ZSET,
						0,
						now
				);

		if (expired == null || expired.isEmpty()) return;

		for (Object bookId : expired) {
			service.rollback(Long.parseLong(bookId.toString()));
			redis.opsForZSet()
					.remove(RedisKeys.BOOK_ZSET, bookId);
		}
	}
}
