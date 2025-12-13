package org.example.ticketbrutal.controller;

import lombok.RequiredArgsConstructor;
import org.example.ticketbrutal.core.TicketService;
import org.example.ticketbrutal.entity.TicketBooking;
import org.example.ticketbrutal.entity.TicketEvent;
import org.springframework.web.bind.annotation.*;

/************************
 * Made by [MR Ferry™]  *
 * on Desember 2025     *
 ************************/

@RequiredArgsConstructor
@RestController
@RequestMapping("/tickets")
public class TicketController {

	private final TicketService ticketService;

	@PostMapping("/book")
	public TicketBooking book(@RequestBody TicketBooking ticketBooking) {
		return ticketService.book(ticketBooking.getEventId(), ticketBooking.getBookedQuantity());
	}

	@PostMapping("/stockIn")
	public TicketEvent stockIn(@RequestBody TicketEvent ticketEvent) {
		ticketService.stockIn(ticketEvent);
		return ticketEvent;
	}

	@PostMapping("/confirm/{bookId}")
	public String confirm(@PathVariable Long bookId) {
		ticketService.confirm(bookId);
		return "Confirmed";
	}

	@GetMapping("/getAvailable/{eventId}")
	public int getAvailable(@PathVariable String eventId) {
		return ticketService.getAvailableQuantity(eventId);
	}

	@GetMapping("getBooking/{bookId}")
	public TicketBooking getBooking(@PathVariable long bookId){
		return ticketService.getBooking(bookId);
	}

}
