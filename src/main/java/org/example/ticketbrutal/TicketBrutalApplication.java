package org.example.ticketbrutal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TicketBrutalApplication{

	static void main(String[] args){
		SpringApplication.run(TicketBrutalApplication.class, args);
	}

}
