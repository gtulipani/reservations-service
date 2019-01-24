package com.reservations.validation;

import java.util.List;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import com.fasterxml.jackson.annotation.JsonInclude;

@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReservationValidatorError {
	@NonNull
	List<String> fields;
	@NonNull
	String description;
}
