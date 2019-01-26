package com.reservations.service;

import static com.reservations.TestUtils.basicError;
import static com.reservations.TestUtils.basicReservation;
import static com.reservations.TestUtils.differentReservation;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.time.LocalDate;
import java.util.Optional;

import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Sets;
import com.reservations.entity.EventType;
import com.reservations.entity.Reservation;
import com.reservations.entity.ReservationStatus;
import com.reservations.exception.InvalidRangeException;
import com.reservations.exception.ReservationNotFoundException;
import com.reservations.exception.ReservationValidationException;
import com.reservations.exception.extension.ExtensionNotFoundException;
import com.reservations.repository.ReservationRepository;
import com.reservations.validation.DefaultReservationValidatorExtensionImpl;
import com.reservations.validation.ReservationCancellationValidatorExtensionImpl;
import com.reservations.validation.ReservationCreationValidatorExtensionImpl;
import com.reservations.validation.ReservationUpdateValidatorExtensionImpl;

public class ReservationServiceImplTest {
	private static final long MAX_CAPACITY = 10L;

	@Mock
	private ReservationRepository reservationRepository;
	@Mock
	private DefaultReservationValidatorExtensionImpl defaultReservationValidatorExtension;
	@Mock
	private ReservationCreationValidatorExtensionImpl reservationCreationValidatorExtension;
	@Mock
	private ReservationUpdateValidatorExtensionImpl reservationUpdateValidatorExtension;
	@Mock
	private ReservationCancellationValidatorExtensionImpl reservationCancellationValidatorExtension;

	private ReservationServiceImpl reservationService;

	@BeforeMethod
	public void setup() {
		initMocks(this);

		reservationService = new ReservationServiceImpl(reservationRepository,
				Sets.newHashSet(
						defaultReservationValidatorExtension,
						reservationCreationValidatorExtension,
						reservationUpdateValidatorExtension,
						reservationCancellationValidatorExtension),
				MAX_CAPACITY);
	}

	@Test
	public void testCheckAvailability_returnsTrue() {
		LocalDate startDate = LocalDate.now();
		LocalDate endDate = startDate.plusDays(1);
		when(reservationRepository.findQuantityByDateRangeAndStatus(startDate, endDate, ReservationStatus.ACTIVE)).thenReturn(MAX_CAPACITY - 1);

		boolean available = reservationService.checkAvailability(startDate, endDate);

		verify(reservationRepository, times(1)).findQuantityByDateRangeAndStatus(startDate, endDate, ReservationStatus.ACTIVE);
		assertThat(available).isTrue();
	}

	@Test
	public void testCheckAvailabilityWithMaximumCapacity_returnsFalse() {
		LocalDate startDate = LocalDate.now();
		LocalDate endDate = startDate.plusDays(1);
		when(reservationRepository.findQuantityByDateRangeAndStatus(startDate, endDate, ReservationStatus.ACTIVE)).thenReturn(MAX_CAPACITY);

		boolean available = reservationService.checkAvailability(startDate, endDate);
		
		verify(reservationRepository, times(1)).findQuantityByDateRangeAndStatus(startDate, endDate, ReservationStatus.ACTIVE);
		assertThat(available).isFalse();
	}

	@Test
	public void testCheckAvailabilityWithInvalidRange_throwsInvalidRangeException() {
		LocalDate startDate = LocalDate.now();
		LocalDate endDate = startDate.minusDays(1);

		try {
			reservationService.checkAvailability(startDate, endDate);
			failBecauseExceptionWasNotThrown(InvalidRangeException.class);
		} catch (InvalidRangeException e) {
			verify(reservationRepository, never()).findQuantityByDateRangeAndStatus(any(LocalDate.class), any(LocalDate.class), any(ReservationStatus.class));
			assertThat(e.getResponseStatus()).isEqualByComparingTo(HttpStatus.BAD_REQUEST);
			assertThat(e.getMessage()).contains(startDate.toString(), endDate.toString());
		}
	}

	@Test
	public void testCreateReservation_noErrors() {
		Reservation reservation = basicReservation();
		when(reservationCreationValidatorExtension.supports(EventType.CREATION)).thenReturn(true);

		Reservation updatedReservation = reservationService.createReservation(reservation);

		verify(reservationRepository, times(1)).save(reservation);

		reservation.setStatus(ReservationStatus.ACTIVE);
		assertThat(updatedReservation).isEqualTo(reservation);
	}

	@Test
	public void testCreateReservationWithoutValidator_throwsExtensionNotFoundException() {
		Reservation reservation = basicReservation();

		assertThatThrownBy(() -> reservationService.createReservation(reservation))
				.isInstanceOf(ExtensionNotFoundException.class)
				.hasMessageContaining(EventType.CREATION.name());

		verify(reservationRepository, never()).save(any(Reservation.class));
	}

	@Test
	public void testCreateReservationInvalid_throwsReservationValidationException() {
		Reservation reservation = basicReservation();
		when(reservationCreationValidatorExtension.supports(EventType.CREATION)).thenReturn(true);
		doThrow(new ReservationValidationException(Sets.newHashSet(basicError()))).when(reservationCreationValidatorExtension).validate(reservation);

		try {
			reservationService.createReservation(reservation);
			failBecauseExceptionWasNotThrown(ReservationValidationException.class);
		} catch (ReservationValidationException e) {
			verify(reservationRepository, never()).save(any(Reservation.class));
			assertThat(e.getErrors()).containsExactly(basicError());
		}
	}

	@Test
	public void testGetByBookingIdentifierUuidAndStatusActive_noErrors() {
		Reservation reservation = basicReservation();
		when(reservationRepository.findByBookingIdentifierUuidAndStatus(reservation.getBookingIdentifierUuid(), ReservationStatus.ACTIVE)).thenReturn(Optional.of(reservation));

		Reservation result = reservationService.getByBookingIdentifierUuidAndStatus(reservation.getBookingIdentifierUuid(), ReservationStatus.ACTIVE);

		verify(reservationRepository, times(1)).findByBookingIdentifierUuidAndStatus(reservation.getBookingIdentifierUuid(), ReservationStatus.ACTIVE);
		assertThat(reservation).isEqualTo(result);
	}

	@Test
	public void testGetByBookingIdentifierUuidAndStatusCancelled_noErrors() {
		Reservation reservation = basicReservation();
		when(reservationRepository.findByBookingIdentifierUuidAndStatus(reservation.getBookingIdentifierUuid(), ReservationStatus.CANCELLED)).thenReturn(Optional.of(reservation));

		Reservation result = reservationService.getByBookingIdentifierUuidAndStatus(reservation.getBookingIdentifierUuid(), ReservationStatus.CANCELLED);

		verify(reservationRepository, times(1)).findByBookingIdentifierUuidAndStatus(reservation.getBookingIdentifierUuid(), ReservationStatus.CANCELLED);
		assertThat(reservation).isEqualTo(result);
	}

	@Test
	public void testGetByBookingIdentifierUuidInvalid_throwsReservationNotFoundException() {
		Reservation reservation = basicReservation();
		when(reservationRepository.findByBookingIdentifierUuidAndStatus(reservation.getBookingIdentifierUuid(), ReservationStatus.ACTIVE)).thenReturn(Optional.empty());

		try {
			reservationService.getByBookingIdentifierUuidAndStatus(reservation.getBookingIdentifierUuid(), ReservationStatus.ACTIVE);
			failBecauseExceptionWasNotThrown(ReservationNotFoundException.class);
		} catch (ReservationNotFoundException e) {
			verify(reservationRepository, times(1)).findByBookingIdentifierUuidAndStatus(reservation.getBookingIdentifierUuid(), ReservationStatus.ACTIVE);
			assertThat(e.getResponseStatus()).isEqualTo(HttpStatus.NOT_FOUND);
			assertThat(e.getMessage()).contains(
					reservation.getBookingIdentifierUuid(),
					ReservationStatus.ACTIVE.name());
		}
	}

	@Test
	public void testUpdateReservation_onlyArrivalDateAndDepartureDateAreModified() {
		Reservation reservation = basicReservation();
		Reservation newReservation = differentReservation(reservation);
		when(reservationUpdateValidatorExtension.supports(EventType.UPDATE)).thenReturn(true);

		Reservation result = reservationService.updateReservation(reservation, newReservation);

		verify(reservationRepository, times(1)).save(any(Reservation.class));
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
		when(reservationUpdateValidatorExtension.supports(EventType.UPDATE)).thenReturn(true);

		Reservation result = reservationService.updateReservation(reservation, newReservation);

		verify(reservationRepository, times(1)).save(any(Reservation.class));
		assertThat(result).isNotEqualTo(newReservation);
		assertThat(reservation.getArrivalDate()).isEqualTo(reservation.getArrivalDate());
		assertThat(reservation.getDepartureDate()).isEqualTo(reservation.getDepartureDate());
	}

	@Test
	public void testUpdateReservationWithoutValidator_throwsExtensionNotFoundException() {
		Reservation reservation = basicReservation();
		Reservation newReservation = differentReservation(reservation);

		assertThatThrownBy(() -> reservationService.updateReservation(reservation, newReservation))
				.isInstanceOf(ExtensionNotFoundException.class)
				.hasMessageContaining(EventType.UPDATE.name());

		verify(reservationRepository, never()).save(any(Reservation.class));
	}

	@Test
	public void testUpdateReservationInvalid_throwsReservationValidationException() {
		Reservation reservation = basicReservation();
		Reservation newReservation = differentReservation(reservation);
		when(reservationUpdateValidatorExtension.supports(EventType.UPDATE)).thenReturn(true);
		doThrow(new ReservationValidationException(Sets.newHashSet(basicError()))).when(reservationUpdateValidatorExtension).validate(reservation);

		try {
			reservationService.updateReservation(reservation, newReservation);
			failBecauseExceptionWasNotThrown(ReservationValidationException.class);
		} catch (ReservationValidationException e) {
			verify(reservationRepository, never()).save(any(Reservation.class));
			assertThat(e.getErrors()).containsExactly(basicError());
		}
	}

	@Test
	public void testCancelReservation_noErrors() {
		Reservation reservation = basicReservation();
		when(reservationCancellationValidatorExtension.supports(EventType.CANCELLATION)).thenReturn(true);

		reservationService.cancelReservation(reservation);

		reservation.setStatus(ReservationStatus.CANCELLED);
		verify(reservationRepository, times(1)).save(reservation);
	}

	@Test
	public void testCancelReservationWithoutValidator_throwsExtensionNotFoundException() {
		Reservation reservation = basicReservation();

		assertThatThrownBy(() -> reservationService.cancelReservation(reservation))
				.isInstanceOf(ExtensionNotFoundException.class)
				.hasMessageContaining(EventType.CANCELLATION.name());

		verify(reservationRepository, never()).save(any(Reservation.class));
	}

	@Test
	public void testCancelReservationInvalid_throwsReservationValidationException() {
		Reservation reservation = basicReservation();
		when(reservationCancellationValidatorExtension.supports(EventType.CANCELLATION)).thenReturn(true);
		doThrow(new ReservationValidationException(Sets.newHashSet(basicError()))).when(reservationCancellationValidatorExtension).validate(reservation);

		try {
			reservationService.cancelReservation(reservation);
			failBecauseExceptionWasNotThrown(ReservationValidationException.class);
		} catch (ReservationValidationException e) {
			verify(reservationRepository, never()).save(any(Reservation.class));
			assertThat(e.getErrors()).containsExactly(basicError());
		}
	}
}
