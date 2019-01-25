package com.reservations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.reservations.entity.Reservation;

public class TestUtils {
	public static Reservation basicReservation() {
		return Reservation.builder()
				.id(1L)
				.createdOn(LocalDateTime.now())
				.lastModified(LocalDateTime.now())
				.email("test@gmail.com")
				.fullName("James Smith")
				.arrivalDate(LocalDate.now().plusDays(5))
				.departureDate(LocalDate.now().plusDays(6))
				.bookingIdentifierUuid(UUID.randomUUID().toString())
				.build();
	}

	public static Reservation basicReservation(LocalDate arrivalDate, LocalDate departureDate) {
		return Reservation.builder()
				.id(1L)
				.createdOn(LocalDateTime.now())
				.lastModified(LocalDateTime.now())
				.email("test@gmail.com")
				.fullName("James Smith")
				.arrivalDate(arrivalDate)
				.departureDate(departureDate)
				.bookingIdentifierUuid(UUID.randomUUID().toString())
				.build();
	}
}
