package com.reservations.controller;

import java.util.concurrent.Callable;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import com.reservations.entity.Reservation;

public interface ReservationController {
	public Callable<ResponseEntity> createReservation(@RequestBody Reservation reservation);
}
