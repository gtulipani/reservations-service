package com.reservations.validation;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.reservations.entity.EventType;
import com.reservations.entity.Reservation;
import com.reservations.service.ReservationService;

/**
 * Validator for the {@link Reservation} entity for a {@link EventType#UPDATE}
 */
@Slf4j
@Component
public class ReservationUpdateValidatorExtensionImpl extends ReservationCompleteValidatorExtension {
	public ReservationUpdateValidatorExtensionImpl(@Value("${reservations.min-arrival-ahead-days}") int minimumArrivalAheadDays,
													 @Value("${reservations.max-advance-days}") int maxAdvanceTime,
													 @Value("${reservations.min-duration}") int minDuration,
													 @Value("${reservations.max-duration}") int maxDuration,
													 @Lazy ReservationService reservationService,
													 @Autowired MessageSource messages) {
		super(minimumArrivalAheadDays, maxAdvanceTime, minDuration, maxDuration, reservationService, messages);
	}

	@Override
	public boolean isDefault() {
		return false;
	}

	@Override
	public boolean supports(EventType eventType) {
		return EventType.UPDATE.equals(eventType);
	}
}
