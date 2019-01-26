package com.reservations.exception;

import lombok.Getter;

import org.springframework.http.HttpStatus;

import com.reservations.entity.ReservationStatus;

@Getter
public class ReservationNotFoundException extends ReservationServiceException {
	public ReservationNotFoundException(String bookingIdentifierUuid, ReservationStatus reservationStatus) {
		super(HttpStatus.NOT_FOUND, String.format("Couldn't find Reservation with bookingIdentifierUuid=%s and status=%s", bookingIdentifierUuid, reservationStatus));
	}
}
