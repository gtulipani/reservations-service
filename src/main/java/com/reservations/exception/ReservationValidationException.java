package com.reservations.exception;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;

import com.reservations.validation.ReservationValidatorError;

@AllArgsConstructor
public class ReservationValidationException extends RuntimeException {
	@Getter
	Set<ReservationValidatorError> errors;
}
