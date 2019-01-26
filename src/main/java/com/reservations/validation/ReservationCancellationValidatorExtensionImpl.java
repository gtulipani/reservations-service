package com.reservations.validation;

import static com.reservations.validation.ReservationValidatorConstants.ARRIVAL_DATE_FIELD;
import static com.reservations.validation.ReservationValidatorConstants.VALIDATION_ERROR_RESERVATION_ALREADY_STARTED;

import java.time.LocalDate;
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

/**
 * Validator for the {@link Reservation} entity for a {@link EventType#CANCELLATION}
 */
@Slf4j
@Component
public class ReservationCancellationValidatorExtensionImpl extends ReservationCompleteValidatorExtension {
	public ReservationCancellationValidatorExtensionImpl(@Value("${reservations.min-arrival-ahead-days}") int minimumArrivalAheadDays,
													 @Value("${reservations.max-advance-days}") int maxAdvanceTime,
													 @Value("${reservations.min-duration}") int minDuration,
													 @Value("${reservations.max-duration}") int maxDuration,
													 @Autowired MessageSource messages) {
		super(minimumArrivalAheadDays, maxAdvanceTime, minDuration, maxDuration, messages);
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
		if (reservation.getArrivalDate().isBefore(LocalDate.now())) {
			throw new ReservationValidationException(Sets.newHashSet(ReservationValidatorError.builder()
					.fields(Collections.singletonList(ARRIVAL_DATE_FIELD))
					.description(getMessage(VALIDATION_ERROR_RESERVATION_ALREADY_STARTED))
					.build()));
		}
	}
}
