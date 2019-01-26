package com.reservations.validation;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import com.reservations.entity.EventType;
import com.reservations.entity.Reservation;

/**
 * Validator for the {@link Reservation} entity for a {@link EventType#CREATION}
 */
@Slf4j
@Component
public class ReservationCreationValidatorExtensionImpl extends ReservationCompleteValidatorExtension {
	public ReservationCreationValidatorExtensionImpl(@Value("${reservations.min-arrival-ahead-days}") int minimumArrivalAheadDays,
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
		return EventType.CREATION.equals(eventType);
	}
}