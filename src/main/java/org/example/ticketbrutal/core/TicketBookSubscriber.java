package org.example.ticketbrutal.core;

import lombok.RequiredArgsConstructor;
import org.example.ticketbrutal.entity.TicketBooking;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/************************
 * Made by [MR Ferry™]  *
 * on Desember 2025     *
 ************************/

@Component
@RequiredArgsConstructor
public class TicketBookSubscriber implements MessageListener{

	private final ObjectMapper objectMapper;

	@Override
	public void onMessage(Message message, byte[] pattern){
		TicketBooking booking = objectMapper.readValue(message.getBody(), TicketBooking.class);

		System.out.println("Ticket booked: " + objectMapper.writeValueAsString(booking));

		// Real actions
		// - Update WebSocket
		// - Refresh local cache
		// - Trigger email notification

	}
}
