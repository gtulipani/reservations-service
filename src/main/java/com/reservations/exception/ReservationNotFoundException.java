package com.reservations.exception;

import lombok.Getter;

import org.springframework.http.HttpStatus;

@Getter
public class ReservationNotFoundException extends ReservationServiceException {
	public ReservationNotFoundException(String bookingIdentifierUuid) {
		super(HttpStatus.NOT_FOUND, String.format("Couldn't find Reservation with bookingIdentifierUuid=%s", bookingIdentifierUuid));
	}
}
