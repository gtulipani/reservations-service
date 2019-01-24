package com.reservations.validation;

public interface Validator<T> {
	void validate(T object);
}
