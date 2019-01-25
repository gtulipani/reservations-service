package com.reservations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.RandomStringUtils;

import com.reservations.entity.Reservation;
import com.reservations.entity.ReservationStatus;
import com.reservations.validation.ReservationValidatorError;

public class TestUtils {
	public static final String DEFAULT_ERROR_FIELD = "Error Field";
	public static final String DEFAULT_ERROR_MESSAGE = "Error Message";

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

	public static ReservationValidatorError basicError(List<String> errors, String description) {
		return ReservationValidatorError.builder()
				.fields(errors)
				.description(description)
				.build();
	}

	public static ReservationValidatorError basicError() {
		return basicError(Collections.singletonList(DEFAULT_ERROR_FIELD), DEFAULT_ERROR_MESSAGE);
	}

	public static Reservation differentReservation(Reservation reservation) {
		return Reservation.builder()
				.id(reservation.getId() + 1)
				.createdOn(reservation.getCreatedOn().plusDays(1))
				.lastModified(reservation.getLastModified().plusDays(1))
				.email(String.format("%s@%s.com", RandomStringUtils.random(5), RandomStringUtils.random(5)))
				.fullName(RandomStringUtils.random(10))
				.status(ReservationStatus.CANCELLED)
				.arrivalDate(reservation.getArrivalDate().plusDays(1))
				.departureDate(reservation.getDepartureDate().plusDays(1))
				.bookingIdentifierUuid(UUID.randomUUID().toString())
				.build();
	}
}
