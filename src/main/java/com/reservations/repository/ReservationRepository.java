package com.reservations.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.reservations.entity.Reservation;

public interface ReservationRepository extends CrudRepository<Reservation, Long> {
	Optional<Reservation> findByBookingIdentifierUuid(String bookingIdentifierUuid);
}
