package com.reservations.validation;

import static java.time.temporal.ChronoUnit.DAYS;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;
import com.reservations.entity.Reservation;
import com.reservations.exception.ReservationValidationException;

@Slf4j
@Component
public class ReservationValidator implements Validator<Reservation> {
	private final static String ARRIVAL_DATE_FIELD = "arrivalDate";
	private final static String DEPARTURE_DATE_FIELD = "departureDate";

	private final int minimumArrivalAheadDays;
	private final int maxAdvanceTime;
	private final int minDuration;
	private final int maxDuration;
	private final List<Predicate<Reservation>> validationsList;
	private final Set<ReservationValidatorError> errors = Sets.newHashSet();

	public ReservationValidator(@Value("${reservations.min-arrival-ahead-days}") int minimumArrivalAheadDays,
								@Value("${reservations.max-advance-time}") int maxAdvanceTime,
								@Value("${reservations.min-duration}") int minDuration,
								@Value("${reservations.max-duration}") int maxDuration) {
		this.minimumArrivalAheadDays = minimumArrivalAheadDays;
		this.maxAdvanceTime = maxAdvanceTime;
		this.minDuration = minDuration;
		this.maxDuration = maxDuration;
		this.validationsList = Arrays.asList(
				validateArrivalDateHasMinimumAheadDays(),
				validateArrivalDateDoesntExceedMaximumAdvanceDays(),
				validateMinimumDuration(),
				validateMaximumDuration());
	}

	@Override
	public void validate(Reservation reservation) {
		// Can't use anyMatch() or allMatch() because these are short-circuited, and we want to list all the errors
		if (!validationsList.stream()
				.map(validation -> validation.test(reservation))
				.reduce(Boolean::logicalAnd)
				.orElse(Boolean.TRUE)) {
			throw new ReservationValidationException(errors);
		}
	}

	/**
	 * Predicate that checks that the reservation can be created minimum {@link ReservationValidator#minimumArrivalAheadDays}
	 * days ahead from arrival. E.g. a reservation can be created minimum 1 day ahead from arrivalDate.
	 */
	private Predicate<Reservation> validateArrivalDateHasMinimumAheadDays() {
		return checkPredicate(reservation -> reservation.getArrivalDate().isBefore(LocalDate.now().plusDays(minimumArrivalAheadDays)),
				ReservationValidatorError.builder()
						.fields(Collections.singletonList("arrivalDate"))
						.description(String.format("Reservation must be created minimum %s days ahead from desired arrival date", minimumArrivalAheadDays))
						.build());
	}

	/**
	 * Predicate that checks that the reservation can be created up to {@link ReservationValidator#maxAdvanceTime}
	 * days in advance. E.g. a reservation can be created up to 30 days (1 month) in advance.
	 */
	private Predicate<Reservation> validateArrivalDateDoesntExceedMaximumAdvanceDays() {
		return checkPredicate(reservation -> !reservation.getArrivalDate().isBefore(LocalDate.now().plusDays(maxAdvanceTime)),
				ReservationValidatorError.builder()
						.fields(Collections.singletonList(ARRIVAL_DATE_FIELD))
						.description(String.format("Reservation can be created up to %s days in advance", maxAdvanceTime))
						.build());
	}

	/**
	 * Predicate that checks that the reservation has a minimum duration of {@link ReservationValidator#minDuration}
	 * days. E.g. a reservation must have a minimum duration of 1 day.
	 */
	private Predicate<Reservation> validateMinimumDuration() {
		return checkPredicate(reservation -> DAYS.between(reservation.getArrivalDate(), reservation.getDepartureDate()) < minDuration,
				ReservationValidatorError.builder()
						.fields(Arrays.asList(ARRIVAL_DATE_FIELD, DEPARTURE_DATE_FIELD))
						.description(String.format("Reservation must include at minimum %s days", minDuration))
						.build());
	}

	/**
	 * Predicate that checks that the reservation doesnt exceed the maximum duration of {@link ReservationValidator#maxDuration}
	 * days. E.g. a reservation can't exceed 3 days.
	 */
	private Predicate<Reservation> validateMaximumDuration() {
		return checkPredicate(reservation -> DAYS.between(reservation.getArrivalDate(), reservation.getDepartureDate()) > maxDuration,
				ReservationValidatorError.builder()
						.fields(Arrays.asList(ARRIVAL_DATE_FIELD, DEPARTURE_DATE_FIELD))
						.description(String.format("Reservation can't exceed %s days", maxDuration))
						.build());
	}

	/**
	 * Internal method that validates a negative predicate received as parameter.
	 * If the {@link Predicate} received as parameter is false, the error is added to the {@link ReservationValidator#errors} attribute.
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
}
