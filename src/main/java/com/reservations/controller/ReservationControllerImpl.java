package com.reservations.controller;

import java.util.Optional;
import java.util.concurrent.Callable;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.reservations.entity.Reservation;
import com.reservations.exception.ReservationServiceException;
import com.reservations.exception.ReservationValidationException;
import com.reservations.service.ReservationService;
import com.reservations.validation.Validator;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/v1/reservations", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class ReservationControllerImpl implements ReservationController {
	private final ReservationService reservationService;
	private final Validator<Reservation> reservationValidator;

	@Override
	@RequestMapping(method = RequestMethod.POST)
	public Callable<ResponseEntity> createReservation(@RequestBody Reservation reservation) {
		return () -> {
			log.info("Received API to create a reservation for user with fullName={}", reservation.getFullName());
			try {
				reservationValidator.validate(reservation);
				return ResponseEntity.ok(reservationService.createReservation(reservation));
			} catch (ReservationValidationException e) {
				log.error("Error validating reservation={}, error={}", reservation, e.getMessage());
				return ResponseEntity.badRequest().body(e.getErrors());
			} catch (Exception e) {
				log.error("Error creating reservation={}, error={}", reservation, e.getMessage());
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
			}
		};
	}

	@Override
	@RequestMapping(method = RequestMethod.PATCH, value = "/{bookingIdentifierUuid}")
	public Callable<ResponseEntity> updateReservation(@PathVariable("bookingIdentifierUuid") String bookingIdentifierUuid, @RequestBody Reservation updatedReservation) {
		return () -> {
			log.info("Received API to update reservation with bookingIdentifierUuid={}", bookingIdentifierUuid);
			try {
				return ResponseEntity.ok(reservationService.updateReservation(reservationService.getByBookingIdentifierUuid(bookingIdentifierUuid), updatedReservation));
			} catch (ReservationServiceException e) {
				log.error("Error updating reservation={}, error={}", updatedReservation, e.getMessage());
				return ResponseEntity.status(e.getResponseStatus()).body(e.getResponseBody());
			} catch (ReservationValidationException e) {
				log.error("Error validating reservation={}, error={}", updatedReservation, e.getMessage());
				return ResponseEntity.badRequest().body(e.getErrors());
			} catch (Exception e) {
				log.error("Error updating reservation={}, error={}", updatedReservation, e.getMessage());
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
			}
		};
	}
}
