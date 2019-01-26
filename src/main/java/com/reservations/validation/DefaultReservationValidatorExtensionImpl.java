package com.reservations.validation;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import com.reservations.entity.EventType;
import com.reservations.entity.Reservation;

@RequiredArgsConstructor
@Component
public class DefaultReservationValidatorExtensionImpl implements ReservationValidatorExtension {
	@Override
	public boolean isDefault() {
		return true;
	}

	@Override
	public boolean supports(EventType context) {
		return true;
	}

	@Override
	public void validate(Reservation reservation) {
		
	}
}
