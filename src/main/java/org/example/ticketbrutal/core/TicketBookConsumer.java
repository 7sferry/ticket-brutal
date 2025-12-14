package org.example.ticketbrutal.core;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.RedisStreamCommands.XAddOptions;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.hash.JacksonHashMapper;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/************************
 * Made by [MR Ferry™]  *
 * on Desember 2025     *
 ************************/

@Component
@RequiredArgsConstructor
public class TicketBookConsumer implements StreamListener<String, MapRecord<String, String, String>>{

	private static final AtomicInteger counter = new AtomicInteger();
	private static final long MAX_RETRIES = 3;
	public static final String RETRY = "retry";
	private final StringRedisTemplate redisTemplate;

	@Override
	public void onMessage(MapRecord<String, String, String> message){

		try{
			process(message);
			redisTemplate.opsForStream().acknowledge(
					RedisKeys.TICKET_SERVICES_GROUP,
					message
			);
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	private static void process(MapRecord<String, String, String> message){
		Map<String, String> data = message.getValue();
		String type = data.get("type");

		String eventId = data.get("eventId");
		long ticketId = Long.parseLong(data.get("ticketId"));
		int quantity = Integer.parseInt(data.get("quantity"));
		int i = counter.incrementAndGet();
		System.out.println("i = " + i);
		if(i % 3 == 0){
			throw new RuntimeException("Error");
		}
		System.out.println("Processing ticket: " + ticketId);
	}

	@Scheduled(fixedDelay = 10000)
	public void processPendingMessages() {
		PendingMessages pendingMessages = redisTemplate.opsForStream()
				.pending(RedisKeys.TICKET_BOOK_CHANNEL, RedisKeys.TICKET_SERVICES_GROUP, Range.unbounded(), 10);

		if (pendingMessages.isEmpty()) {
			return;
		}

		for (PendingMessage pendingMessage : pendingMessages) {
			RecordId messageId = pendingMessage.getId();
			long deliveryCount = pendingMessage.getTotalDeliveryCount();

			System.out.println("⚠️ Found pending message: " + messageId + " | Attempts: " + deliveryCount);

			if (deliveryCount >= MAX_RETRIES) {
				handleDeadLetter(messageId);
			} else {
				retryMessage(messageId);
			}
		}
	}

	private void handleDeadLetter(RecordId messageId) {
		System.out.println("❌ Max retries reached for ID: " + messageId + ". Moving to DLQ.");

		try {
			List<MapRecord<String, Object, Object>> records = redisTemplate.opsForStream()
					.claim(RedisKeys.TICKET_BOOK_CHANNEL, RedisKeys.TICKET_SERVICES_GROUP, RETRY,
							Duration.ofMillis(0), // Force claim to get body
							messageId);

			if (!records.isEmpty()) {
				MapRecord<String, Object, Object> record = records.getFirst();

				redisTemplate.opsForStream().add(StreamRecords.newRecord()
						.ofMap(record.getValue())
						.withStreamKey(RedisKeys.TICKET_BOOK_DEAD_LETTER), XAddOptions.maxlen(5));
			}

			redisTemplate.opsForStream().acknowledge(RedisKeys.TICKET_BOOK_CHANNEL, RedisKeys.TICKET_SERVICES_GROUP, messageId);
			System.out.println("🚮 Message acknowledged and removed from main stream.");

		} catch (Exception e) {
			System.err.println("Error handling DLQ: " + e.getMessage());
		}
	}

	private void retryMessage(RecordId messageId) {
		System.out.println("🔄 Retrying message: " + messageId);

		StreamOperations<String, String, String> stringObjectObjectStreamOperations = redisTemplate.opsForStream(JacksonHashMapper.hierarchical());
		List<MapRecord<String, String, String>> claimedMessages = stringObjectObjectStreamOperations
				.claim(RedisKeys.TICKET_BOOK_CHANNEL, RedisKeys.TICKET_SERVICES_GROUP, RETRY,
						Duration.ofMillis(0), // Force claim to get body
						messageId);

		if (!claimedMessages.isEmpty()) {
			MapRecord<String, String, String> record = claimedMessages.getFirst();

			try {
				process(record);

				redisTemplate.opsForStream().acknowledge(RedisKeys.TICKET_BOOK_CHANNEL, RedisKeys.TICKET_SERVICES_GROUP, messageId);
				System.out.println("✅ Retry successful. Message ACKed.");

			} catch (Exception e) {
				System.err.println("💥 Retry failed again. Leaving in PEL for next cycle.");
			}
		}
	}

}
