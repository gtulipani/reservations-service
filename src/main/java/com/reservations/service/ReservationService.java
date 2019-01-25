package com.reservations.service;

import com.reservations.entity.Reservation;

public interface ReservationService {
	Reservation createReservation(Reservation reservation);

	Reservation getByBookingIdentifierUuid(String bookingIdentifierUuid);

	Reservation updateReservation(Reservation oldReservation, Reservation newReservation);

	void cancelReservation(Reservation reservation);
}
