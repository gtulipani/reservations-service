package com.reservations.service;

import static com.reservations.TestUtils.basicError;
import static com.reservations.TestUtils.basicReservation;
import static com.reservations.TestUtils.basicReservationsWithinRange;
import static com.reservations.TestUtils.differentReservation;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.RandomStringUtils;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.reservations.entity.DateRange;
import com.reservations.entity.EventType;
import com.reservations.entity.Reservation;
import com.reservations.entity.ReservationAvailability;
import com.reservations.entity.ReservationStatus;
import com.reservations.entity.utils.DateUtils;
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
	private static final int PAGE_SIZE = 10;

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
		String bookingIdentifierUuid = UUID.randomUUID().toString();
		DateRange dateRange = DateRange.builder()
				.start(LocalDate.now())
				.end(LocalDate.now().plusDays(1))
				.build();
		when(reservationRepository.findQuantityByDateRangeAndStatusOmittingBookingIdentifierUuid(dateRange.getStart(), dateRange.getEnd(), ReservationStatus.ACTIVE, bookingIdentifierUuid)).thenReturn(MAX_CAPACITY - 1);

		boolean available = reservationService.checkAvailability(dateRange, bookingIdentifierUuid);

		verify(reservationRepository, times(1)).findQuantityByDateRangeAndStatusOmittingBookingIdentifierUuid(dateRange.getStart(), dateRange.getEnd(), ReservationStatus.ACTIVE, bookingIdentifierUuid);
		assertThat(available).isTrue();
	}

	@Test
	public void testCheckAvailabilityWithMaximumCapacity_returnsFalse() {
		String bookingIdentifierUuid = UUID.randomUUID().toString();
		DateRange dateRange = DateRange.builder()
				.start(LocalDate.now())
				.end(LocalDate.now().plusDays(1))
				.build();
		when(reservationRepository.findQuantityByDateRangeAndStatusOmittingBookingIdentifierUuid(dateRange.getStart(), dateRange.getEnd(), ReservationStatus.ACTIVE, bookingIdentifierUuid)).thenReturn(MAX_CAPACITY);

		boolean available = reservationService.checkAvailability(dateRange, bookingIdentifierUuid);
		
		verify(reservationRepository, times(1)).findQuantityByDateRangeAndStatusOmittingBookingIdentifierUuid(dateRange.getStart(), dateRange.getEnd(), ReservationStatus.ACTIVE, bookingIdentifierUuid);
		assertThat(available).isFalse();
	}

	@Test
	public void testCheckAvailabilityWithInvalidRange_throwsInvalidRangeException() {
		String bookingIdentifierUuid = UUID.randomUUID().toString();
		DateRange dateRange = DateRange.builder()
				.start(LocalDate.now())
				.end(LocalDate.now().minusDays(1))
				.build();

		try {
			reservationService.checkAvailability(dateRange, bookingIdentifierUuid);
			failBecauseExceptionWasNotThrown(InvalidRangeException.class);
		} catch (InvalidRangeException e) {
			verify(reservationRepository, never()).findQuantityByDateRangeAndStatusOmittingBookingIdentifierUuid(any(LocalDate.class), any(LocalDate.class), any(ReservationStatus.class), any(String.class));
			assertThat(e.getResponseStatus()).isEqualByComparingTo(HttpStatus.BAD_REQUEST);
			assertThat(e.getMessage()).contains(dateRange.getStart().toString(), dateRange.getEnd().toString());
		}
	}

	@Test
	public void testGetAvailabilityForTodayAndTomorrow_fetchesPagesUntilEmptyAndReturnsAvailabilityForTodayAndTomorrow() {
		DateRange dateRange = DateRange.builder()
				.start(LocalDate.now())
				.end(LocalDate.now().plusDays(1))
				.build();
		Reservation firstReservation = basicReservation(dateRange);
		Reservation secondReservation = basicReservation(dateRange);
		List<Reservation> reservations = Arrays.asList(firstReservation, secondReservation);
		// We'll receive a Page<Reservation> only the first time, and then an empty Page
		when(reservationRepository.findReservationsByDateRangeAndStatus(eq(dateRange.getStart()), eq(dateRange.getEnd()), eq(ReservationStatus.ACTIVE), any(Pageable.class)))
				.thenAnswer(new Answer<Object>() {
						private int callNumber = 0;
			
						@Override
						public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
							Pageable pageable = new PageRequest(callNumber, PAGE_SIZE);
							if (callNumber == 0) {
								callNumber++;
								return new PageImpl<>(reservations, pageable, reservations.size()
								);
							} else {
								return new PageImpl<>(Lists.newArrayList(), pageable, 0);
							}
						}});

		Set<ReservationAvailability> availabilitySet = reservationService.getAvailability(dateRange);

		verify(reservationRepository, times(2)).findReservationsByDateRangeAndStatus(eq(dateRange.getStart()), eq(dateRange.getEnd()), eq(ReservationStatus.ACTIVE), any(Pageable.class));
		// Contains one more date because the range is inclusive
		assertThat(availabilitySet.size()).isEqualTo(2);
		// It's occupied the first date but free the second date (it's the departure date from both reservations)
		assertThat(availabilitySet).containsOnlyOnce(
				ReservationAvailability.builder()
						.date(firstReservation.getArrivalDate())
						.availability(MAX_CAPACITY- 2L)
						.build(),
				ReservationAvailability.builder()
						.date(firstReservation.getDepartureDate())
						.availability(MAX_CAPACITY)
						.build());
	}

	@Test
	public void testGetAvailabilityFor30Days_fetchesMultiplePagesUntilEmptyAndReturnsAvailabilityFor31Days() {
		int datesQuantity = 30;
		DateRange dateRange = DateRange.builder()
				.start(LocalDate.now())
				.end(LocalDate.now().plusDays(datesQuantity))
				.build();
		// We create 30 reservations, each one starting on one day and finishing the next day
		List<Reservation> completeReservations = basicReservationsWithinRange(dateRange);
		List<Reservation> firstChunk = completeReservations.subList(0, PAGE_SIZE);
		List<Reservation> secondChunk = completeReservations.subList(PAGE_SIZE, (PAGE_SIZE * 2));
		List<Reservation> thirdChunk = completeReservations.subList(PAGE_SIZE * 2, (PAGE_SIZE * 3));
		List<List<Reservation>> chunksList = Arrays.asList(firstChunk, secondChunk, thirdChunk);
		// We'll receive a Page<Reservation> only three times, and then an empty Page
		when(reservationRepository.findReservationsByDateRangeAndStatus(eq(dateRange.getStart()), eq(dateRange.getEnd()), eq(ReservationStatus.ACTIVE), any(Pageable.class)))
				.thenAnswer(new Answer<Object>() {
					private int callNumber = 0;

					@Override
					public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
						Pageable pageable = new PageRequest(callNumber, 10);
						if (callNumber < 3) {
							List<Reservation> chunk = chunksList.get(callNumber);
							callNumber++;
							return new PageImpl<>(chunk, pageable, chunk.size());
						} else {
							return new PageImpl<>(Lists.newArrayList(), pageable, 0);
						}
					}});

		Set<ReservationAvailability> availabilitySet = reservationService.getAvailability(dateRange);

		verify(reservationRepository, times(4)).findReservationsByDateRangeAndStatus(eq(dateRange.getStart()), eq(dateRange.getEnd()), eq(ReservationStatus.ACTIVE), any(Pageable.class));
		// Contains one more because the range is inclusive
		assertThat(availabilitySet.size()).isEqualTo(datesQuantity + 1);
		// For each date between the start and the end (exclusive) it's occupied with one reservation
		DateUtils.daysBetween(dateRange).forEach(localDate -> assertThat(availabilitySet)
				.containsOnlyOnce(ReservationAvailability.builder()
						.date(localDate)
						.availability(MAX_CAPACITY - 1L)
						.build()));
		// The end date is not occupied (it's the departure date from the last reservation)
		assertThat(availabilitySet).containsOnlyOnce(ReservationAvailability.builder()
				.date(dateRange.getEnd())
				.availability(MAX_CAPACITY)
				.build());
	}

	@Test
	public void testGetAvailabilityWithInvalidRange_throwsInvalidRangeException() {
		DateRange dateRange = DateRange.builder()
				.start(LocalDate.now())
				.end(LocalDate.now().minusDays(1))
				.build();

		try {
			reservationService.getAvailability(dateRange);
			failBecauseExceptionWasNotThrown(InvalidRangeException.class);
		} catch (InvalidRangeException e) {
			verify(reservationRepository, never()).findReservationsByDateRangeAndStatus(any(LocalDate.class), any(LocalDate.class), any(ReservationStatus.class), any(Pageable.class));
			assertThat(e.getResponseStatus()).isEqualByComparingTo(HttpStatus.BAD_REQUEST);
			assertThat(e.getMessage()).contains(dateRange.getStart().toString(), dateRange.getEnd().toString());
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
