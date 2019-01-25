package com.reservations.controller;

import static com.reservations.TestUtils.basicReservation;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Collections;

import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Sets;
import com.reservations.entity.Reservation;
import com.reservations.exception.ReservationValidationException;
import com.reservations.service.ReservationService;
import com.reservations.validation.ReservationValidatorError;
import com.reservations.validation.Validator;

public class ReservationControllerImplTest {
	private static final String ERROR_FIELD = "ERROR_FIELD";
	private static final String ERROR_MESSAGE = "Error Message";

	@Mock
	private ReservationService reservationService;

	@Mock
	private Validator<Reservation> reservationValidator;

	private ReservationControllerImpl reservationControllerImpl;

	@BeforeMethod
	public void setUp() {
		initMocks(this);

		reservationControllerImpl = new ReservationControllerImpl(reservationService, reservationValidator);
	}

	@Test
	public void testCreateReservation_noErrors() throws Exception {
		Reservation reservation = basicReservation();
		when(reservationService.createReservation(reservation)).thenReturn(reservation);

		ResponseEntity responseEntity = reservationControllerImpl.createReservation(reservation).call();

		verify(reservationValidator, times(1)).validate(reservation);
		verify(reservationService, times(1)).createReservation(reservation);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(responseEntity.getBody()).isEqualTo(reservation);
	}

	@Test
	public void testCreateReservation_returnsBadRequestWhenReservationValidationExceptionIsThrown() throws Exception {
		Reservation reservation = basicReservation();
		doThrow(new ReservationValidationException(Sets.newHashSet(basicReservationError()))).when(reservationValidator).validate(reservation);

		ResponseEntity responseEntity = reservationControllerImpl.createReservation(reservation).call();

		verify(reservationValidator, times(1)).validate(reservation);
		verify(reservationService, never()).createReservation(any());
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(responseEntity.getBody()).isEqualTo(Sets.newHashSet(basicReservationError()));
	}

	@Test
	public void testCreateReservation_returnsInternalServerErrorWhenExceptionIsThrownInValidator() throws Exception {
		Reservation reservation = basicReservation();
		doThrow(new NullPointerException(ERROR_MESSAGE)).when(reservationValidator).validate(reservation);

		ResponseEntity responseEntity = reservationControllerImpl.createReservation(reservation).call();

		verify(reservationValidator, times(1)).validate(reservation);
		verify(reservationService, never()).createReservation(any());
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
		assertThat(responseEntity.getBody()).isEqualTo(ERROR_MESSAGE);
	}

	@Test
	public void testCreateReservation_returnsInternalServerErrorWhenExceptionIsThrownInService() throws Exception {
		Reservation reservation = basicReservation();
		when(reservationService.createReservation(reservation)).thenThrow(new NullPointerException(ERROR_MESSAGE));

		ResponseEntity responseEntity = reservationControllerImpl.createReservation(reservation).call();

		verify(reservationValidator, times(1)).validate(reservation);
		verify(reservationService, times(1)).createReservation(reservation);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
		assertThat(responseEntity.getBody()).isEqualTo(ERROR_MESSAGE);
	}

	private ReservationValidatorError basicReservationError() {
		return ReservationValidatorError.builder()
				.fields(Collections.singletonList(ERROR_FIELD))
				.description(ERROR_MESSAGE)
				.build();
	}
}
