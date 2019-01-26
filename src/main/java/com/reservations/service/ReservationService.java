package com.reservations.service;

import java.time.LocalDate;
import java.util.Set;

import com.reservations.entity.Reservation;
import com.reservations.entity.ReservationAvailability;
import com.reservations.entity.ReservationStatus;

public interface ReservationService {
	Set<ReservationAvailability> getAvailability(LocalDate start, LocalDate end);

	boolean checkAvailability(LocalDate start, LocalDate end);

	Reservation createReservation(Reservation reservation);

	Reservation getByBookingIdentifierUuidAndStatus(String bookingIdentifierUuid, ReservationStatus reservationStatus);

	Reservation updateReservation(Reservation oldReservation, Reservation newReservation);

	void cancelReservation(Reservation reservation);
}
