package com.reservations.service;

import com.reservations.entity.Reservation;
import com.reservations.entity.ReservationStatus;

public interface ReservationService {
	Reservation createReservation(Reservation reservation);

	Reservation getByBookingIdentifierUuidAndStatus(String bookingIdentifierUuid, ReservationStatus reservationStatus);

	Reservation updateReservation(Reservation oldReservation, Reservation newReservation);

	void cancelReservation(Reservation reservation);
}
