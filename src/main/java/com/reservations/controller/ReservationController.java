package com.reservations.controller;

import java.util.concurrent.Callable;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.reservations.entity.Reservation;

public interface ReservationController {
	Callable<ResponseEntity> createReservation(@RequestBody Reservation reservation);

	Callable<ResponseEntity> updateReservation(@PathVariable("bookingIdentifierUuid") String bookingIdentifierUuid, @RequestBody Reservation reservation);
}
