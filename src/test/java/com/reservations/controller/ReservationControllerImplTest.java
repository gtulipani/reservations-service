package com.reservations.controller;

import static com.reservations.TestUtils.DEFAULT_ERROR_MESSAGE;
import static com.reservations.TestUtils.basicError;
import static com.reservations.TestUtils.basicReservation;
import static com.reservations.TestUtils.differentReservation;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Sets;
import com.reservations.entity.Reservation;
import com.reservations.exception.ReservationNotFoundException;
import com.reservations.exception.ReservationValidationException;
import com.reservations.service.ReservationService;
import com.reservations.validation.Validator;

public class ReservationControllerImplTest {
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
		doThrow(new ReservationValidationException(Sets.newHashSet(basicError()))).when(reservationValidator).validate(reservation);

		ResponseEntity responseEntity = reservationControllerImpl.createReservation(reservation).call();

		verify(reservationValidator, times(1)).validate(reservation);
		verify(reservationService, never()).createReservation(any());
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(responseEntity.getBody()).isEqualTo(Sets.newHashSet(basicError()));
	}

	@Test
	public void testCreateReservation_returnsInternalServerErrorWhenExceptionIsThrownInValidator() throws Exception {
		Reservation reservation = basicReservation();
		doThrow(new NullPointerException(DEFAULT_ERROR_MESSAGE)).when(reservationValidator).validate(reservation);

		ResponseEntity responseEntity = reservationControllerImpl.createReservation(reservation).call();

		verify(reservationValidator, times(1)).validate(reservation);
		verify(reservationService, never()).createReservation(any());
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
		assertThat(responseEntity.getBody()).isEqualTo(DEFAULT_ERROR_MESSAGE);
	}

	@Test
	public void testCreateReservation_returnsInternalServerErrorWhenExceptionIsThrownInService() throws Exception {
		Reservation reservation = basicReservation();
		when(reservationService.createReservation(reservation)).thenThrow(new NullPointerException(DEFAULT_ERROR_MESSAGE));

		ResponseEntity responseEntity = reservationControllerImpl.createReservation(reservation).call();

		verify(reservationValidator, times(1)).validate(reservation);
		verify(reservationService, times(1)).createReservation(reservation);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
		assertThat(responseEntity.getBody()).isEqualTo(DEFAULT_ERROR_MESSAGE);
	}

	@Test
	public void testUpdateReservation_noErrors() throws Exception {
		Reservation reservation = basicReservation();
		Reservation newReservation = differentReservation(reservation);
		when(reservationService.getByBookingIdentifierUuid(reservation.getBookingIdentifierUuid())).thenReturn(reservation);
		when(reservationService.updateReservation(reservation, newReservation)).thenReturn(newReservation);

		ResponseEntity responseEntity = reservationControllerImpl.updateReservation(reservation.getBookingIdentifierUuid(), newReservation).call();

		verify(reservationService, times(1)).getByBookingIdentifierUuid(reservation.getBookingIdentifierUuid());
		verify(reservationService, times(1)).updateReservation(reservation, newReservation);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(responseEntity.getBody()).isEqualTo(newReservation);
	}

	@Test
	public void testUpdateReservation_returnsCorrespondingResponseStatusAndResponseBodyWhenReservationNotFoundExceptionIsThrown() throws Exception {
		Reservation reservation = basicReservation();
		Reservation newReservation = differentReservation(reservation);
		ReservationNotFoundException exception = new ReservationNotFoundException(reservation.getBookingIdentifierUuid());
		when(reservationService.getByBookingIdentifierUuid(reservation.getBookingIdentifierUuid())).thenThrow(exception);

		ResponseEntity responseEntity = reservationControllerImpl.updateReservation(reservation.getBookingIdentifierUuid(), newReservation).call();

		verify(reservationService, times(1)).getByBookingIdentifierUuid(reservation.getBookingIdentifierUuid());
		verify(reservationService, never()).updateReservation(any(Reservation.class), any(Reservation.class));
		assertThat(responseEntity.getStatusCode()).isEqualTo(exception.getResponseStatus());
		assertThat(responseEntity.getBody()).isEqualTo(exception.getResponseBody());
	}

	@Test
	public void testUpdateReservation_returnsBadRequestWhenReservationValidationExceptionIsThrown() throws Exception {
		Reservation reservation = basicReservation();
		Reservation newReservation = differentReservation(reservation);
		when(reservationService.getByBookingIdentifierUuid(reservation.getBookingIdentifierUuid())).thenReturn(reservation);
		doThrow(new ReservationValidationException(Sets.newHashSet(basicError()))).when(reservationService).updateReservation(reservation, newReservation);

		ResponseEntity responseEntity = reservationControllerImpl.updateReservation(reservation.getBookingIdentifierUuid(), newReservation).call();

		verify(reservationService, times(1)).getByBookingIdentifierUuid(reservation.getBookingIdentifierUuid());
		verify(reservationService, times(1)).updateReservation(reservation, newReservation);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(responseEntity.getBody()).isEqualTo(Sets.newHashSet(basicError()));
	}

	@Test
	public void testUpdateReservation_returnsInternalServerErrorWhenExceptionIsThrownInService() throws Exception {
		Reservation reservation = basicReservation();
		Reservation newReservation = differentReservation(reservation);
		when(reservationService.getByBookingIdentifierUuid(reservation.getBookingIdentifierUuid())).thenReturn(reservation);
		when(reservationService.updateReservation(reservation, newReservation)).thenThrow(new NullPointerException(DEFAULT_ERROR_MESSAGE));

		ResponseEntity responseEntity = reservationControllerImpl.updateReservation(reservation.getBookingIdentifierUuid(), newReservation).call();

		verify(reservationService, times(1)).getByBookingIdentifierUuid(reservation.getBookingIdentifierUuid());
		verify(reservationService, times(1)).updateReservation(reservation, newReservation);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
		assertThat(responseEntity.getBody()).isEqualTo(DEFAULT_ERROR_MESSAGE);
	}
}
