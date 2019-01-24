package com.reservations.repository;

import org.springframework.data.repository.CrudRepository;

import com.reservations.entity.Reservation;

public interface ReservationRepository extends CrudRepository<Reservation, Long> {
}
