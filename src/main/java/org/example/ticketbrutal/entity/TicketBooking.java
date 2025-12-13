package org.example.ticketbrutal.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/************************
 * Made by [MR Ferry™]  *
 * on Desember 2025     *
 ************************/

@Getter
@Setter
@EqualsAndHashCode( of = "id")
@Entity
@Table(name = "ticket_bookings")
public class TicketBooking{

	@Id
	@GeneratedValue
	private Long id;

	private String eventId;
	private int bookedQuantity;

	@Enumerated(EnumType.STRING)
	private Status status;

	private Instant expireAt;

	@Version
	private Long version;

	public enum Status {
		PENDING,
		CONFIRMED,
		EXPIRED
	}

}
