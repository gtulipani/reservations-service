package com.reservations.service;

import java.util.Set;

import com.reservations.entity.DateRange;
import com.reservations.entity.Reservation;
import com.reservations.entity.ReservationAvailability;
import com.reservations.entity.ReservationStatus;

public interface ReservationService {
	Set<ReservationAvailability> getAvailability(DateRange dateRange);

	boolean checkAvailability(DateRange dateRange);

	Reservation createReservation(Reservation reservation);

	Reservation getByBookingIdentifierUuidAndStatus(String bookingIdentifierUuid, ReservationStatus reservationStatus);

	Reservation updateReservation(Reservation oldReservation, Reservation newReservation);

	void cancelReservation(Reservation reservation);
}
