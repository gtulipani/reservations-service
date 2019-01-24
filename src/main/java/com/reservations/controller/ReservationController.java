package com.reservations.controller;

import lombok.AllArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.reservations.entity.Reservation;
import com.reservations.service.ReservationService;

@AllArgsConstructor
@RestController
@RequestMapping(value = "/api/v1/reservations", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class ReservationController {
	private ReservationService reservationService;

	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<Reservation> createReservation(@RequestBody Reservation reservation) {
		return ResponseEntity.ok(reservationService.createReservation(reservation));
	}
}
