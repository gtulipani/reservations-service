package com.reservations.controller;

import java.time.LocalDate;
import java.util.concurrent.Callable;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.reservations.entity.Reservation;

public interface ReservationController {
	Callable<ResponseEntity> getAvailability(@RequestParam(value = "start", required = false) LocalDate start,
											 @RequestParam(value = "end", required = false) LocalDate end);

	Callable<ResponseEntity> createReservation(@RequestBody Reservation reservation);

	Callable<ResponseEntity> updateReservation(@PathVariable("bookingIdentifierUuid") String bookingIdentifierUuid, @RequestBody Reservation reservation);

	Callable<ResponseEntity> cancelReservation(@PathVariable("bookingIdentifierUuid") String bookingIdentifierUuid);
}
