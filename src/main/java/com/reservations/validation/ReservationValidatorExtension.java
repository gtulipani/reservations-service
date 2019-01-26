package com.reservations.validation;

import com.reservations.entity.EventType;
import com.reservations.entity.Reservation;
import com.reservations.extensibility.extension.Extension;

/**
 * Interface used to validate the {@link Reservation} entity for each different {@link EventType}
 */
public interface ReservationValidatorExtension extends Extension<EventType> {
	void validate(Reservation reservation);
}
