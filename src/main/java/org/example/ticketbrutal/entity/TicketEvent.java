package org.example.ticketbrutal.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/************************
 * Made by [MR Ferry™]  *
 * on Desember 2025     *
 ************************/

@Getter
@Setter
@EqualsAndHashCode( of = "id")
@Entity
@Table(name = "ticket_events")
public class TicketEvent{
	@Id
	private String id;
	private String name;
	private int unrestrictedQuantity;
	@Version
	private Integer version;
}
