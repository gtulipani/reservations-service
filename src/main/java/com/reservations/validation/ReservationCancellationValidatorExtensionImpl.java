package com.reservations.validation;

import static com.reservations.validation.ReservationValidatorConstants.ARRIVAL_DATE_FIELD;
import static com.reservations.validation.ReservationValidatorConstants.VALIDATION_ERROR_RESERVATION_ALREADY_STARTED;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;
import com.reservations.entity.EventType;
import com.reservations.entity.Reservation;
import com.reservations.exception.ReservationValidationException;
import com.reservations.service.ReservationService;

/**
 * Validator for the {@link Reservation} entity for a {@link EventType#CANCELLATION}
 */
@Slf4j
@Component
public class ReservationCancellationValidatorExtensionImpl extends ReservationCompleteValidatorExtension {
	private final LocalTime checkinTime;

	public ReservationCancellationValidatorExtensionImpl(@Value("${reservations.check-in-time-hour}") int checkInTimeHour,
														 @Value("${reservations.check-in-time-minute}") int checkInTimeMinute,
														 @Value("${reservations.min-arrival-ahead-days}") int minimumArrivalAheadDays,
														 @Value("${reservations.max-advance-days}") int maxAdvanceTime,
														 @Value("${reservations.min-duration}") int minDuration,
														 @Value("${reservations.max-duration}") int maxDuration,
														 @Autowired ReservationService reservationService,
														 @Autowired MessageSource messages) {
		super(minimumArrivalAheadDays, maxAdvanceTime, minDuration, maxDuration, reservationService, messages);
		this.checkinTime = LocalTime.of(checkInTimeHour, checkInTimeMinute);
	}

	@Override
	public boolean isDefault() {
		return false;
	}

	@Override
	public boolean supports(EventType eventType) {
		return EventType.CANCELLATION.equals(eventType);
	}

	@Override
	public void validate(Reservation reservation) {
		if (reservationAlreadyStarted(reservation)) {
			throw new ReservationValidationException(Sets.newHashSet(ReservationValidatorError.builder()
					.fields(Collections.singletonList(ARRIVAL_DATE_FIELD))
					.description(getMessage(VALIDATION_ERROR_RESERVATION_ALREADY_STARTED))
					.build()));
		}
	}

	/**
	 * Checks if the reservation already started. This means one of the following cases:
	 * - {@link Reservation#arrivalDate} is before today's date
	 * - {@link Reservation#arrivalDate} is today and {@link #checkinTime} already passed
	 */
	private boolean reservationAlreadyStarted(Reservation reservation) {
		LocalDate today = LocalDate.now();
		LocalDate arrivalDate = reservation.getArrivalDate();
		return (today.isAfter(arrivalDate)) ||
				(today.isEqual(arrivalDate) && (LocalTime.now().isAfter(checkinTime)));
	}
}
