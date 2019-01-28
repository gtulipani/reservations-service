package com.reservations.controller;

import java.time.LocalDate;
import java.util.Objects;
import java.util.concurrent.Callable;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.reservations.entity.DateRange;
import com.reservations.entity.Reservation;
import com.reservations.entity.ReservationStatus;
import com.reservations.exception.ReservationServiceException;
import com.reservations.exception.ReservationValidationException;
import com.reservations.service.ReservationService;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/reservations", produces = MediaType.APPLICATION_JSON_VALUE)
public class ReservationControllerImpl implements ReservationController {
	private final ReservationService reservationService;
	private final int availabilityDefaultDays;

	@Autowired
	public ReservationControllerImpl(ReservationService reservationService,
									 @Value("${reservations.availability-default-days}") int availabilityDefaultDays) {
		this.reservationService = reservationService;
		this.availabilityDefaultDays = availabilityDefaultDays;
	}

	@Override
	@RequestMapping(method = RequestMethod.GET, value = "/availability")
	public Callable<ResponseEntity> getAvailability(@RequestParam(value = "start", required = false) @DateTimeFormat(pattern = "MM-dd-yyyy") LocalDate start,
													@RequestParam(value = "end", required = false) @DateTimeFormat(pattern = "MM-dd-yyyy") LocalDate end) {
		return () -> {
			DateRange dateRange = DateRange.builder()
					.start(start)
					.end(end)
					.build();
			applyDefaultRange(dateRange);
			log.info("Received API call to get campsite availability start={}, end={}", dateRange.getStart(), dateRange.getEnd());
			try {
				return ResponseEntity.ok(reservationService.getAvailability(dateRange));
			} catch (ReservationServiceException e) {
				log.error("Error getting availability in range start={}, end={}, error={}", dateRange.getStart(), dateRange.getEnd(), e.getMessage());
				return ResponseEntity.status(e.getResponseStatus()).body(e.getResponseBody());
			} catch (Exception e) {
				log.error("Error getting availability in range start={}, end={}, error={}", dateRange.getStart(), dateRange.getEnd(), e.getMessage());
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
			}
		};
	}

	@Override
	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public Callable<ResponseEntity> createReservation(@RequestBody Reservation reservation) {
		return () -> {
			log.info("Received API call to create a reservation for user with fullName={}", reservation.getFullName());
			try {
				return ResponseEntity.status(HttpStatus.CREATED).body(reservationService.createReservation(reservation));
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
	@RequestMapping(method = RequestMethod.PATCH, value = "/{bookingIdentifierUuid}", consumes = MediaType.APPLICATION_JSON_VALUE)
	public Callable<ResponseEntity> updateReservation(@PathVariable("bookingIdentifierUuid") String bookingIdentifierUuid, @RequestBody Reservation updatedReservation) {
		return () -> {
			log.info("Received API call to update reservation with bookingIdentifierUuid={}", bookingIdentifierUuid);
			try {
				return ResponseEntity.ok(reservationService.updateReservation(reservationService.getByBookingIdentifierUuidAndStatus(bookingIdentifierUuid, ReservationStatus.ACTIVE), updatedReservation));
			} catch (ReservationServiceException e) {
				log.error("Error updating reservation={}, error={}", updatedReservation, e.getMessage());
				return ResponseEntity.status(e.getResponseStatus()).body(e.getResponseBody());
			} catch (ReservationValidationException e) {
				log.error("Error validating update of reservation={}, error={}", updatedReservation, e.getMessage());
				return ResponseEntity.badRequest().body(e.getErrors());
			} catch (Exception e) {
				log.error("Error updating reservation={}, error={}", updatedReservation, e.getMessage());
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
			}
		};
	}

	@Override
	@RequestMapping(method = RequestMethod.DELETE, value = "/{bookingIdentifierUuid}")
	public Callable<ResponseEntity> cancelReservation(@PathVariable("bookingIdentifierUuid") String bookingIdentifierUuid) {
		return () -> {
			log.info("Received API call to cancel reservation with bookingIdentifierUuid={}", bookingIdentifierUuid);
			try {
				reservationService.cancelReservation(reservationService.getByBookingIdentifierUuidAndStatus(bookingIdentifierUuid, ReservationStatus.ACTIVE));
				return ResponseEntity.noContent().build();
			} catch (ReservationServiceException e) {
				log.error("Error cancelling reservation with bookingIdentifierUuid={}, error={}", bookingIdentifierUuid, e.getMessage());
				return ResponseEntity.status(e.getResponseStatus()).body(e.getResponseBody());
			} catch (ReservationValidationException e) {
				log.error("Error validating cancellation of reservation with bookingIdentifierUuid={}, error={}", bookingIdentifierUuid, e.getMessage());
				return ResponseEntity.badRequest().body(e.getErrors());
			} catch (Exception e) {
				log.error("Error cancelling reservation with bookingIdentifierUuid={}, error={}", bookingIdentifierUuid, e.getMessage());
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
			}
		};
	}

	private void applyDefaultRange(DateRange dateRange) {
		if (Objects.isNull(dateRange.getStart()) && Objects.isNull(dateRange.getEnd())) {
			// Applying default filter values
			dateRange.setStart(LocalDate.now());
			dateRange.setEnd(LocalDate.now().plusDays(availabilityDefaultDays));
			log.debug("Setting default values for dates range start={}, end={}", dateRange.getStart(), dateRange.getEnd());
		} else {
			if (Objects.isNull(dateRange.getStart())) {
				// Only end was specified. We set start as today's date
				dateRange.setStart(LocalDate.now());
			} else if (Objects.isNull(dateRange.getEnd())) {
				// Only start was specified. We set end as (start + availabilityDefaultDays)
				dateRange.setEnd(dateRange.getStart().plusDays(availabilityDefaultDays));
			}
		}
	}
}
