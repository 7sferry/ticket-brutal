package org.example.ticketbrutal.core;

/************************
 * Made by [MR Ferry™]  *
 * on Desember 2025     *
 ************************/

public class RedisKeys{
	public static final String BOOK_ZSET = "ticket:books";
	public static final String STOCK_ZSET = "ticket:stock";
	public static final String TICKET_BOOK_CHANNEL = "ticket.book.channel";
	public static final String TICKET_SERVICES_GROUP = "ticket-services";
	public static final String TICKET_BOOK_DEAD_LETTER = "ticket_book_dead_letter";
}
