package com.reservations.validation;

import static com.reservations.validation.ReservationValidatorConstants.ARRIVAL_DATE_FIELD;
import static com.reservations.validation.ReservationValidatorConstants.DEPARTURE_DATE_FIELD;
import static com.reservations.validation.ReservationValidatorConstants.VALIDATION_ERROR_BASE;
import static com.reservations.validation.ReservationValidatorConstants.VALIDATION_ERROR_MAXIMUM_ADVANCE_DAYS;
import static com.reservations.validation.ReservationValidatorConstants.VALIDATION_ERROR_MAXIMUM_CAPACITY;
import static com.reservations.validation.ReservationValidatorConstants.VALIDATION_ERROR_MAXIMUM_DURATION;
import static com.reservations.validation.ReservationValidatorConstants.VALIDATION_ERROR_MINIMUM_AHEAD_DAYS;
import static com.reservations.validation.ReservationValidatorConstants.VALIDATION_ERROR_MINIMUM_DURATION;
import static java.time.temporal.ChronoUnit.DAYS;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.MessageSource;

import com.google.common.collect.Sets;
import com.reservations.entity.DateRange;
import com.reservations.entity.Reservation;
import com.reservations.exception.ReservationValidationException;
import com.reservations.service.ReservationService;

/**
 * Intermediary class that has the logic to validate all the business rules applicable to Reservations
 */
@Slf4j
public abstract class ReservationCompleteValidatorExtension implements ReservationValidatorExtension {
	private final int minimumArrivalAheadDays;
	private final int maxAdvanceTime;
	private final int minDuration;
	private final int maxDuration;
	private final ReservationService reservationService;
	private final MessageSource messages;
	private final List<Predicate<Reservation>> validationsList;

	private Set<ReservationValidatorError> errors;

	public ReservationCompleteValidatorExtension(int minimumArrivalAheadDays,
												 int maxAdvanceTime,
												 int minDuration,
												 int maxDuration,
												 ReservationService reservationService,
												 MessageSource messages) {
		this.minimumArrivalAheadDays = minimumArrivalAheadDays;
		this.maxAdvanceTime = maxAdvanceTime;
		this.minDuration = minDuration;
		this.maxDuration = maxDuration;
		this.reservationService = reservationService;
		this.messages = messages;
		this.validationsList = Arrays.asList(
				validateArrivalDateHasMinimumAheadDays(),
				validateArrivalDateDoesntExceedMaximumAdvanceDays(),
				validateMinimumDuration(),
				validateMaximumDuration(),
				validateCampsiteAvailability());
	}

	@Override
	public void validate(Reservation reservation) {
		errors = Sets.newHashSet();
		// Can't use anyMatch() or allMatch() because these are short-circuited, and we want to list all the errors
		if (!validationsList.stream()
				.map(validation -> validation.test(reservation))
				.reduce(Boolean::logicalAnd)
				.orElse(Boolean.TRUE)) {
			throw new ReservationValidationException(errors);
		}
		log.info("Successfully validated reservation creation");
	}

	/**
	 * Predicate that checks that the reservation can be created minimum {@link ReservationCreationValidatorExtensionImpl#minimumArrivalAheadDays}
	 * days ahead from arrival. E.g. a reservation can be created minimum 1 day ahead from arrivalDate.
	 */
	private Predicate<Reservation> validateArrivalDateHasMinimumAheadDays() {
		return checkPredicate(reservation -> reservation.getArrivalDate().isBefore(LocalDate.now().plusDays(minimumArrivalAheadDays)),
				ReservationValidatorError.builder()
						.fields(Collections.singletonList(ARRIVAL_DATE_FIELD))
						.description(String.format(getMessage(VALIDATION_ERROR_MINIMUM_AHEAD_DAYS), minimumArrivalAheadDays))
						.build());
	}

	/**
	 * Predicate that checks that the reservation can be created up to {@link ReservationCreationValidatorExtensionImpl#maxAdvanceTime}
	 * days in advance. E.g. a reservation can be created up to 30 days (1 month) in advance.
	 */
	private Predicate<Reservation> validateArrivalDateDoesntExceedMaximumAdvanceDays() {
		return checkPredicate(reservation -> !reservation.getArrivalDate().isBefore(LocalDate.now().plusDays(maxAdvanceTime)),
				ReservationValidatorError.builder()
						.fields(Collections.singletonList(ARRIVAL_DATE_FIELD))
						.description(String.format(getMessage(VALIDATION_ERROR_MAXIMUM_ADVANCE_DAYS), maxAdvanceTime))
						.build());
	}

	/**
	 * Predicate that checks that the reservation has a minimum duration of {@link ReservationCreationValidatorExtensionImpl#minDuration}
	 * days. E.g. a reservation must have a minimum duration of 1 day.
	 */
	private Predicate<Reservation> validateMinimumDuration() {
		return checkPredicate(reservation -> DAYS.between(reservation.getArrivalDate(), reservation.getDepartureDate()) < minDuration,
				ReservationValidatorError.builder()
						.fields(Arrays.asList(ARRIVAL_DATE_FIELD, DEPARTURE_DATE_FIELD))
						.description(String.format(getMessage(VALIDATION_ERROR_MINIMUM_DURATION), minDuration))
						.build());
	}

	/**
	 * Predicate that checks that the reservation doesnt exceed the maximum duration of {@link ReservationCreationValidatorExtensionImpl#maxDuration}
	 * days. E.g. a reservation can't exceed 3 days.
	 */
	private Predicate<Reservation> validateMaximumDuration() {
		return checkPredicate(reservation -> DAYS.between(reservation.getArrivalDate(), reservation.getDepartureDate()) > maxDuration,
				ReservationValidatorError.builder()
						.fields(Arrays.asList(ARRIVAL_DATE_FIELD, DEPARTURE_DATE_FIELD))
						.description(String.format(getMessage(VALIDATION_ERROR_MAXIMUM_DURATION), maxDuration))
						.build());
	}

	/**
	 * Predicate that checks that the campsite is available in the range starting in {@link Reservation#arrivalDate}
	 * and finishing in {@link Reservation#departureDate}.
	 */
	private Predicate<Reservation> validateCampsiteAvailability() {
		return checkPredicate(reservation -> !reservationService.checkAvailability(
						DateRange.builder()
								.start(reservation.getArrivalDate())
								.end(reservation.getDepartureDate())
								.build(),
						reservation.getBookingIdentifierUuid()),
				ReservationValidatorError.builder()
						.fields(Arrays.asList(ARRIVAL_DATE_FIELD, DEPARTURE_DATE_FIELD))
						.description(getMessage(VALIDATION_ERROR_MAXIMUM_CAPACITY))
						.build());
	}

	/**
	 * Internal method that validates a negative predicate received as parameter.
	 * If the {@link Predicate} received as parameter is false, the error is added to the {@link ReservationCreationValidatorExtensionImpl#errors} attribute.
	 */
	private Predicate<Reservation> checkPredicate(Predicate<Reservation> predicate, ReservationValidatorError error) {
		return reservation -> {
			if (predicate.test(reservation)) {
				errors.add(error);
				return false;
			}
			return true;
		};
	}

	/**
	 * Private method that gets a message from the resource bundle using the specificErrorKey received as parameter
	 */
	String getMessage(String specificErrorKey) {
		return messages.getMessage(String.format(VALIDATION_ERROR_BASE, specificErrorKey), null, Locale.getDefault());
	}
}
