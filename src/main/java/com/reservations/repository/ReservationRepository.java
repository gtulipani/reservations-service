package com.reservations.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.reservations.entity.Reservation;
import com.reservations.entity.ReservationStatus;

public interface ReservationRepository extends CrudRepository<Reservation, Long> {
	Optional<Reservation> findByBookingIdentifierUuidAndStatus(String bookingIdentifierUuid, ReservationStatus reservationStatus);
}
