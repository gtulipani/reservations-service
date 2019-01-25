package com.reservations.service;

import static com.reservations.TestUtils.basicError;
import static com.reservations.TestUtils.basicReservation;
import static com.reservations.TestUtils.differentReservation;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Optional;

import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Sets;
import com.reservations.entity.Reservation;
import com.reservations.entity.ReservationStatus;
import com.reservations.exception.ReservationNotFoundException;
import com.reservations.exception.ReservationValidationException;
import com.reservations.repository.ReservationRepository;
import com.reservations.validation.Validator;

public class ReservationServiceImplTest {
	@Mock
	private ReservationRepository reservationRepository;
	@Mock
	private Validator<Reservation> reservationValidator;

	private ReservationServiceImpl reservationService;

	@BeforeMethod
	public void setup() {
		initMocks(this);
		
		reservationService = new ReservationServiceImpl(reservationRepository, reservationValidator);
	}

	@Test
	public void testCreateReservation_noErrors() {
		Reservation reservation = basicReservation();

		Reservation updatedReservation = reservationService.createReservation(reservation);

		verify(reservationRepository).save(reservation);

		reservation.setStatus(ReservationStatus.ACTIVE);
		assertThat(updatedReservation).isEqualTo(reservation);
	}

	@Test
	public void testGetByBookingIdentifierUuid_noErrors() {
		Reservation reservation = basicReservation();
		when(reservationRepository.findByBookingIdentifierUuid(reservation.getBookingIdentifierUuid())).thenReturn(Optional.of(reservation));

		Reservation result = reservationService.getByBookingIdentifierUuid(reservation.getBookingIdentifierUuid());

		verify(reservationRepository, times(1)).findByBookingIdentifierUuid(reservation.getBookingIdentifierUuid());
		assertThat(reservation).isEqualTo(result);
	}

	@Test
	public void testGetByBookingIdentifierUuidInvalid_throwsReservationNotFoundException() {
		Reservation reservation = basicReservation();
		when(reservationRepository.findByBookingIdentifierUuid(reservation.getBookingIdentifierUuid())).thenThrow(new ReservationNotFoundException(reservation.getBookingIdentifierUuid()));

		try {
			reservationService.getByBookingIdentifierUuid(reservation.getBookingIdentifierUuid());
			failBecauseExceptionWasNotThrown(ReservationNotFoundException.class);
		} catch (ReservationNotFoundException e) {
			verify(reservationRepository, times(1)).findByBookingIdentifierUuid(reservation.getBookingIdentifierUuid());
			assertThat(e.getResponseStatus()).isEqualTo(HttpStatus.NOT_FOUND);
			assertThat(e.getMessage()).containsIgnoringCase(reservation.getBookingIdentifierUuid());
		}
	}

	@Test
	public void testUpdateReservation_onlyArrivalDateAndDepartureDateAreModified() {
		Reservation reservation = basicReservation();
		Reservation newReservation = differentReservation(reservation);

		Reservation result = reservationService.updateReservation(reservation, newReservation);

		verify(reservationRepository, times(1)).save(any(Reservation.class));
		verify(reservationValidator, times(1)).validate(any(Reservation.class));
		assertThat(result).isNotEqualTo(newReservation);
		assertThat(reservation.getId()).isEqualTo(result.getId());
		assertThat(reservation.getCreatedOn()).isEqualTo(result.getCreatedOn());
		assertThat(reservation.getEmail()).isEqualTo(result.getEmail());
		assertThat(reservation.getFullName()).isEqualTo(result.getFullName());
		assertThat(reservation.getStatus()).isEqualTo(result.getStatus());
		assertThat(reservation.getArrivalDate()).isEqualTo(newReservation.getArrivalDate());
		assertThat(reservation.getDepartureDate()).isEqualTo(newReservation.getDepartureDate());
		assertThat(reservation.getBookingIdentifierUuid()).isEqualTo(reservation.getBookingIdentifierUuid());
	}

	@Test
	public void testUpdateReservation_arrivalDateAndDepartureDateAreModifiedIfNotNull() {
		Reservation reservation = basicReservation();
		Reservation newReservation = differentReservation(reservation);
		newReservation.setArrivalDate(null);
		newReservation.setDepartureDate(null);

		Reservation result = reservationService.updateReservation(reservation, newReservation);

		verify(reservationRepository, times(1)).save(any(Reservation.class));
		verify(reservationValidator, times(1)).validate(any(Reservation.class));
		assertThat(result).isNotEqualTo(newReservation);
		assertThat(reservation.getArrivalDate()).isEqualTo(reservation.getArrivalDate());
		assertThat(reservation.getDepartureDate()).isEqualTo(reservation.getDepartureDate());
	}

	@Test
	public void testUpdateReservationInvalid_throwsReservationValidationException() {
		Reservation reservation = basicReservation();
		Reservation newReservation = differentReservation(reservation);
		doThrow(new ReservationValidationException(Sets.newHashSet(basicError()))).when(reservationValidator).validate(any(Reservation.class));

		try {
			reservationService.updateReservation(reservation, newReservation);
			failBecauseExceptionWasNotThrown(ReservationValidationException.class);
		} catch (ReservationValidationException e) {
			verify(reservationRepository, never()).save(any(Reservation.class));
			verify(reservationValidator, times(1)).validate(any(Reservation.class));
			assertThat(e.getErrors()).containsExactly(basicError());
		}
	}
}
