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

import java.time.LocalDate;

import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Sets;
import com.reservations.entity.DateRange;
import com.reservations.entity.Reservation;
import com.reservations.entity.ReservationStatus;
import com.reservations.exception.InvalidRangeException;
import com.reservations.exception.ReservationNotFoundException;
import com.reservations.exception.ReservationValidationException;
import com.reservations.service.ReservationService;

public class ReservationControllerImplTest {
	private static final int AVAILABILITY_DEFAULT_DAYS = 30;

	@Mock
	private ReservationService reservationService;

	private ReservationControllerImpl reservationControllerImpl;

	@BeforeMethod
	public void setup() {
		initMocks(this);

		reservationControllerImpl = new ReservationControllerImpl(reservationService, AVAILABILITY_DEFAULT_DAYS);
	}

	@Test
	public void testGetAvailability_returnsCorrespondingResponseStatusAndResponseBodyWhenInvalidRangeExceptionIsThrown() throws Exception {
		DateRange dateRange = DateRange.builder()
				.start(LocalDate.now())
				.end(LocalDate.now().plusDays(AVAILABILITY_DEFAULT_DAYS))
				.build();
		InvalidRangeException exception = new InvalidRangeException(dateRange);
		when(reservationService.getAvailability(dateRange)).thenThrow(exception);

		ResponseEntity responseEntity = reservationControllerImpl.getAvailability(dateRange.getStart(), dateRange.getEnd()).call();

		verify(reservationService, times(1)).getAvailability(dateRange);
		assertThat(responseEntity.getStatusCode()).isEqualByComparingTo(exception.getResponseStatus());
		assertThat(responseEntity.getBody()).isEqualTo(exception.getResponseBody());
	}

	@Test
	public void testGetAvailability_returnsInternalServerErrorWhenExceptionIsThrownInService() throws Exception {
		DateRange dateRange = DateRange.builder()
				.start(LocalDate.now())
				.end(LocalDate.now().plusDays(AVAILABILITY_DEFAULT_DAYS))
				.build();
		when(reservationService.getAvailability(dateRange)).thenThrow(new NullPointerException(DEFAULT_ERROR_MESSAGE));

		ResponseEntity responseEntity = reservationControllerImpl.getAvailability(dateRange.getStart(), dateRange.getEnd()).call();

		verify(reservationService, times(1)).getAvailability(dateRange);
		assertThat(responseEntity.getStatusCode()).isEqualByComparingTo(HttpStatus.INTERNAL_SERVER_ERROR);
		assertThat(responseEntity.getBody()).isEqualTo(DEFAULT_ERROR_MESSAGE);
	}

	@Test
	public void testCreateReservation_noErrors() throws Exception {
		Reservation reservation = basicReservation();
		when(reservationService.createReservation(reservation)).thenReturn(reservation);

		ResponseEntity responseEntity = reservationControllerImpl.createReservation(reservation).call();

		verify(reservationService, times(1)).createReservation(reservation);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(responseEntity.getBody()).isEqualTo(reservation);
	}

	@Test
	public void testCreateReservation_returnsBadRequestWhenReservationValidationExceptionIsThrown() throws Exception {
		Reservation reservation = basicReservation();
		when(reservationService.createReservation(reservation)).thenThrow(new ReservationValidationException(Sets.newHashSet(basicError())));

		ResponseEntity responseEntity = reservationControllerImpl.createReservation(reservation).call();

		verify(reservationService, times(1)).createReservation(reservation);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(responseEntity.getBody()).isEqualTo(Sets.newHashSet(basicError()));
	}

	@Test
	public void testCreateReservation_returnsInternalServerErrorWhenExceptionIsThrownInService() throws Exception {
		Reservation reservation = basicReservation();
		when(reservationService.createReservation(reservation)).thenThrow(new NullPointerException(DEFAULT_ERROR_MESSAGE));

		ResponseEntity responseEntity = reservationControllerImpl.createReservation(reservation).call();

		verify(reservationService, times(1)).createReservation(reservation);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
		assertThat(responseEntity.getBody()).isEqualTo(DEFAULT_ERROR_MESSAGE);
	}

	@Test
	public void testUpdateReservation_noErrors() throws Exception {
		Reservation reservation = basicReservation();
		Reservation newReservation = differentReservation(reservation);
		when(reservationService.getByBookingIdentifierUuidAndStatus(reservation.getBookingIdentifierUuid(), ReservationStatus.ACTIVE)).thenReturn(reservation);
		when(reservationService.updateReservation(reservation, newReservation)).thenReturn(newReservation);

		ResponseEntity responseEntity = reservationControllerImpl.updateReservation(reservation.getBookingIdentifierUuid(), newReservation).call();

		verify(reservationService, times(1)).getByBookingIdentifierUuidAndStatus(reservation.getBookingIdentifierUuid(), ReservationStatus.ACTIVE);
		verify(reservationService, times(1)).updateReservation(reservation, newReservation);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(responseEntity.getBody()).isEqualTo(newReservation);
	}

	@Test
	public void testUpdateReservation_returnsCorrespondingResponseStatusAndResponseBodyWhenReservationNotFoundExceptionIsThrown() throws Exception {
		Reservation reservation = basicReservation();
		Reservation newReservation = differentReservation(reservation);
		ReservationNotFoundException exception = new ReservationNotFoundException(reservation.getBookingIdentifierUuid(), ReservationStatus.ACTIVE);
		when(reservationService.getByBookingIdentifierUuidAndStatus(reservation.getBookingIdentifierUuid(), ReservationStatus.ACTIVE)).thenThrow(exception);

		ResponseEntity responseEntity = reservationControllerImpl.updateReservation(reservation.getBookingIdentifierUuid(), newReservation).call();

		verify(reservationService, times(1)).getByBookingIdentifierUuidAndStatus(reservation.getBookingIdentifierUuid(), ReservationStatus.ACTIVE);
		verify(reservationService, never()).updateReservation(any(Reservation.class), any(Reservation.class));
		assertThat(responseEntity.getStatusCode()).isEqualTo(exception.getResponseStatus());
		assertThat(responseEntity.getBody()).isEqualTo(exception.getResponseBody());
	}

	@Test
	public void testUpdateReservation_returnsBadRequestWhenReservationValidationExceptionIsThrown() throws Exception {
		Reservation reservation = basicReservation();
		Reservation newReservation = differentReservation(reservation);
		when(reservationService.getByBookingIdentifierUuidAndStatus(reservation.getBookingIdentifierUuid(), ReservationStatus.ACTIVE)).thenReturn(reservation);
		when(reservationService.updateReservation(reservation, newReservation)).thenThrow(new ReservationValidationException(Sets.newHashSet(basicError())));

		ResponseEntity responseEntity = reservationControllerImpl.updateReservation(reservation.getBookingIdentifierUuid(), newReservation).call();

		verify(reservationService, times(1)).getByBookingIdentifierUuidAndStatus(reservation.getBookingIdentifierUuid(), ReservationStatus.ACTIVE);
		verify(reservationService, times(1)).updateReservation(reservation, newReservation);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(responseEntity.getBody()).isEqualTo(Sets.newHashSet(basicError()));
	}

	@Test
	public void testUpdateReservation_returnsInternalServerErrorWhenExceptionIsThrownInService() throws Exception {
		Reservation reservation = basicReservation();
		Reservation newReservation = differentReservation(reservation);
		when(reservationService.getByBookingIdentifierUuidAndStatus(reservation.getBookingIdentifierUuid(), ReservationStatus.ACTIVE)).thenReturn(reservation);
		when(reservationService.updateReservation(reservation, newReservation)).thenThrow(new NullPointerException(DEFAULT_ERROR_MESSAGE));

		ResponseEntity responseEntity = reservationControllerImpl.updateReservation(reservation.getBookingIdentifierUuid(), newReservation).call();

		verify(reservationService, times(1)).getByBookingIdentifierUuidAndStatus(reservation.getBookingIdentifierUuid(), ReservationStatus.ACTIVE);
		verify(reservationService, times(1)).updateReservation(reservation, newReservation);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
		assertThat(responseEntity.getBody()).isEqualTo(DEFAULT_ERROR_MESSAGE);
	}

	@Test
	public void testCancelReservation_noErrors() throws Exception {
		Reservation reservation = basicReservation();
		when(reservationService.getByBookingIdentifierUuidAndStatus(reservation.getBookingIdentifierUuid(), ReservationStatus.ACTIVE)).thenReturn(reservation);

		ResponseEntity responseEntity = reservationControllerImpl.cancelReservation(reservation.getBookingIdentifierUuid()).call();

		verify(reservationService, times(1)).getByBookingIdentifierUuidAndStatus(reservation.getBookingIdentifierUuid(), ReservationStatus.ACTIVE);
		verify(reservationService, times(1)).cancelReservation(reservation);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		assertThat(responseEntity.getBody()).isNull();
	}

	@Test
	public void testCancelReservation_returnsCorrespondingResponseStatusAndResponseBodyWhenReservationNotFoundExceptionIsThrown() throws Exception {
		Reservation reservation = basicReservation();
		ReservationNotFoundException exception = new ReservationNotFoundException(reservation.getBookingIdentifierUuid(), ReservationStatus.ACTIVE);
		when(reservationService.getByBookingIdentifierUuidAndStatus(reservation.getBookingIdentifierUuid(), ReservationStatus.ACTIVE)).thenThrow(exception);

		ResponseEntity responseEntity = reservationControllerImpl.cancelReservation(reservation.getBookingIdentifierUuid()).call();

		verify(reservationService, times(1)).getByBookingIdentifierUuidAndStatus(reservation.getBookingIdentifierUuid(), ReservationStatus.ACTIVE);
		verify(reservationService, never()).cancelReservation(any(Reservation.class));
		assertThat(responseEntity.getStatusCode()).isEqualTo(exception.getResponseStatus());
		assertThat(responseEntity.getBody()).isEqualTo(exception.getResponseBody());
	}

	@Test
	public void testCancelReservation_returnsBadRequestWhenReservationValidationExceptionIsThrown() throws Exception {
		Reservation reservation = basicReservation();
		when(reservationService.getByBookingIdentifierUuidAndStatus(reservation.getBookingIdentifierUuid(), ReservationStatus.ACTIVE)).thenReturn(reservation);
		doThrow(new ReservationValidationException(Sets.newHashSet(basicError()))).when(reservationService).cancelReservation(reservation);

		ResponseEntity responseEntity = reservationControllerImpl.cancelReservation(reservation.getBookingIdentifierUuid()).call();

		verify(reservationService, times(1)).getByBookingIdentifierUuidAndStatus(reservation.getBookingIdentifierUuid(), ReservationStatus.ACTIVE);
		verify(reservationService, times(1)).cancelReservation(reservation);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(responseEntity.getBody()).isEqualTo(Sets.newHashSet(basicError()));
	}

	@Test
	public void testCancelReservation_returnsInternalServerErrorWhenExceptionIsThrownInService() throws Exception {
		Reservation reservation = basicReservation();
		when(reservationService.getByBookingIdentifierUuidAndStatus(reservation.getBookingIdentifierUuid(), ReservationStatus.ACTIVE)).thenReturn(reservation);
		doThrow(new NullPointerException(DEFAULT_ERROR_MESSAGE)).when(reservationService).cancelReservation(reservation);

		ResponseEntity responseEntity = reservationControllerImpl.cancelReservation(reservation.getBookingIdentifierUuid()).call();

		verify(reservationService, times(1)).getByBookingIdentifierUuidAndStatus(reservation.getBookingIdentifierUuid(), ReservationStatus.ACTIVE);
		verify(reservationService, times(1)).cancelReservation(reservation);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
		assertThat(responseEntity.getBody()).isEqualTo(DEFAULT_ERROR_MESSAGE);
	}
}
