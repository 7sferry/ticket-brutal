package org.example.ticketbrutal.core;

import lombok.RequiredArgsConstructor;
import org.example.ticketbrutal.entity.TicketBooking;
import org.example.ticketbrutal.entity.TicketBooking.Status;
import org.example.ticketbrutal.entity.TicketEvent;
import org.example.ticketbrutal.repo.TicketBookingRepository;
import org.example.ticketbrutal.repo.TicketEventRepository;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

/************************
 * Made by [MR Ferry™]  *
 * on Desember 2025     *
 ************************/

@Service
@RequiredArgsConstructor
public class TicketService {

	private final TicketBookingRepository bookingRepository;
	private final TicketEventRepository eventRepository;
	private final RedisOperations<String, Object> redis;
	private final TicketBookEventPublisher eventPublisher;

	private static final Duration BOOKING_DURATION = Duration.ofMinutes(5);

	@Transactional
	public TicketBooking book(String eventId, int bookedQty) {
		// 1. Check stock from Redis
		int stock = getAvailableQuantity(eventId);
		if(stock < bookedQty){
			throw new RuntimeException("Not enough tickets");
		}

		// 2. Decrease stock
		redis.opsForZSet()
				.incrementScore(RedisKeys.STOCK_ZSET, eventId, -bookedQty);

		// 3. Save booking in DB
		TicketBooking booking = new TicketBooking();
		booking.setEventId(eventId);
		booking.setBookedQuantity(bookedQty);
		booking.setStatus(TicketBooking.Status.PENDING);
		booking.setExpireAt(Instant.now().plus(BOOKING_DURATION));

		bookingRepository.save(booking);

		// 4. Add to Redis delayed queue
		redis.opsForZSet().add(
				RedisKeys.BOOK_ZSET,
				String.valueOf(booking.getId()),
				booking.getExpireAt().toEpochMilli()
		);
		eventPublisher.publish(booking);

		return booking;
	}

	@Transactional
	public TicketBooking getBooking(long bookId){
		TicketBooking booking = bookingRepository.findById(bookId).orElse(null);
		if(booking != null){
			if(booking.getStatus() != Status.EXPIRED && booking.getExpireAt().isBefore(Instant.now())){
				booking.setStatus(TicketBooking.Status.EXPIRED);
				return bookingRepository.save(booking);
			}
		}
		return booking;
	}

	@Transactional
	public void confirm(long bookId) {
		TicketBooking booking = bookingRepository.findById(bookId)
				.orElseThrow();

		if (booking.getStatus() != TicketBooking.Status.PENDING) {
			throw new RuntimeException("Invalid booking status");
		}

		booking.setStatus(TicketBooking.Status.CONFIRMED);
		bookingRepository.save(booking);
		eventRepository.findById(booking.getEventId()).ifPresent(event -> {
			event.setUnrestrictedQuantity(event.getUnrestrictedQuantity() - booking.getBookedQuantity());
			eventRepository.save(event);
		});

		// Remove from expiration queue
		redis.opsForZSet()
				.remove(RedisKeys.BOOK_ZSET, String.valueOf(bookId));
	}

	@Transactional
	public void rollback(long bookId) {
		TicketBooking booking = bookingRepository.findById(bookId)
				.orElse(null);

		if (booking == null || booking.getStatus() != TicketBooking.Status.PENDING) {
			return;
		}

		booking.setStatus(TicketBooking.Status.EXPIRED);
		bookingRepository.save(booking);

		// Return stock
		redis.opsForZSet()
				.incrementScore(
						RedisKeys.STOCK_ZSET,
						booking.getEventId(),
						booking.getBookedQuantity()
				);
	}

	@Transactional
	public void stockIn(TicketEvent ticketEvent){
		eventRepository.save(ticketEvent);
		redis.opsForZSet().add(RedisKeys.STOCK_ZSET, ticketEvent.getId(), ticketEvent.getUnrestrictedQuantity());
	}

	@Transactional
	public int getAvailableQuantity(String eventId){
		Double score = redis.opsForZSet().score(RedisKeys.STOCK_ZSET, eventId);
		if(score != null){
			return score.intValue();
		}
		int integer = eventRepository.findById(eventId).map(TicketEvent::getUnrestrictedQuantity).orElseThrow();
		redis.opsForZSet().add(RedisKeys.STOCK_ZSET, eventId, integer);
		return integer;
	}

}
