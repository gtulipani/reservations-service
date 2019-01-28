package com.reservations.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.reservations.entity.DateRange;
import com.reservations.entity.EventType;
import com.reservations.entity.Reservation;
import com.reservations.entity.ReservationAvailability;
import com.reservations.entity.ReservationStatus;
import com.reservations.entity.utils.DateUtils;
import com.reservations.exception.InvalidRangeException;
import com.reservations.exception.ReservationNotFoundException;
import com.reservations.extensibility.utils.ExtensionUtils;
import com.reservations.repository.ReservationRepository;
import com.reservations.validation.ReservationValidatorExtension;

@Slf4j
@Service
public class ReservationServiceImpl implements ReservationService {
	private final ReservationRepository reservationRepository;
	private final Set<ReservationValidatorExtension> reservationValidatorExtensions;
	private final long maxCapacity;

	@Autowired
	public ReservationServiceImpl(ReservationRepository reservationRepository,
								  Set<ReservationValidatorExtension> reservationValidatorExtensions,
								  @Value("${reservations.max-capacity}") long maxCapacity) {
		this.reservationRepository = reservationRepository;
		this.reservationValidatorExtensions = reservationValidatorExtensions;
		this.maxCapacity = maxCapacity;
	}

	@Override
	public Set<ReservationAvailability> getAvailability(DateRange dateRange) {
		checkValidRange(dateRange);
		return getReservationsPerDate(dateRange)
				.entrySet()
				.stream()
				.map(entry -> ReservationAvailability.builder()
						.date(entry.getKey())
						.availability(maxCapacity - entry.getValue())
						.build())
				.collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(
						ReservationAvailability::getDate,
						LocalDate::compareTo))));
	}

	@Override
	public boolean checkAvailability(DateRange dateRange, String bookingIdentifierUuid) {
		checkValidRange(dateRange);
		return reservationRepository.findQuantityByDateRangeAndStatusOmittingBookingIdentifierUuid(dateRange.getStart(), dateRange.getEnd(), ReservationStatus.ACTIVE, bookingIdentifierUuid) < maxCapacity;
	}

	@Override
	public Reservation createReservation(Reservation reservation) {
		ExtensionUtils.get(reservationValidatorExtensions, EventType.CREATION).validate(reservation);
		reservation.setStatus(ReservationStatus.ACTIVE);
		reservationRepository.save(reservation);
		log.info("Successfully created reservation={}", reservation);
		return reservation;
	}

	@Override
	public Reservation getByBookingIdentifierUuidAndStatus(String bookingIdentifierUuid, ReservationStatus reservationStatus){
		return reservationRepository.findByBookingIdentifierUuidAndStatus(bookingIdentifierUuid, reservationStatus)
				.orElseThrow(() -> new ReservationNotFoundException(bookingIdentifierUuid, reservationStatus));
	}

	@Override
	public Reservation updateReservation(Reservation oldReservation, Reservation newReservation) {
		Reservation updatedReservation = patchReservation(oldReservation, newReservation);
		ExtensionUtils.get(reservationValidatorExtensions, EventType.UPDATE).validate(updatedReservation);
		reservationRepository.save(updatedReservation);
		log.info("Successfully updated reservation={}", updatedReservation);
		return updatedReservation;
	}

	@Override
	public void cancelReservation(Reservation reservation) {
		ExtensionUtils.get(reservationValidatorExtensions, EventType.CANCELLATION).validate(reservation);
		reservation.setStatus(ReservationStatus.CANCELLED);
		reservationRepository.save(reservation);
		log.info("Successfully cancelled reservation={}", reservation);
	}

	/**
	 * Private method that updates only the fields are allowed.
	 */
	private Reservation patchReservation(Reservation oldReservation, Reservation newReservation) {
		if (Objects.nonNull(newReservation.getArrivalDate())) {
			oldReservation.setArrivalDate(newReservation.getArrivalDate());
		}
		if (Objects.nonNull(newReservation.getDepartureDate())) {
			oldReservation.setDepartureDate(newReservation.getDepartureDate());
		}
		return oldReservation;
	}

	/**
	 * Private method that fetches the Reservation for a particular {@link DateRange}, applies some operations and
	 * returns a {@link Map} by {@link LocalDate} and {@link Long}
	 */
	private Map<LocalDate, Long> getReservationsPerDate(DateRange dateRange) {
		checkValidRange(dateRange);
		Map<LocalDate, Long> availabilityMap = Maps.newHashMap();
		int pageNumber = 0;
		int pageSize = 10;
		List<LocalDate> desiredDates = DateUtils.daysBetweenInclusive(dateRange);
		List<Reservation> reservations;
		do {
			reservations = reservationRepository.findReservationsByDateRangeAndStatus(dateRange.getStart(), dateRange.getEnd(), ReservationStatus.ACTIVE, new PageRequest(pageNumber++, pageSize)).getContent();
			// Map Page<Reservation> to Map<LocalDate, Long> and updates the availabilityMap
			reservations.stream()
					.flatMap(reservation -> DateUtils.daysBetween(
									reservation.getArrivalDate(),
									reservation.getDepartureDate())
							.stream())
					.collect(Collectors.groupingBy(
							localDate -> localDate,
							Collectors.reducing(
									0L,
									localDate -> 1L,
									Long::sum)))
					// Now we have a Map<LocalDate, Long> that contains all the dates from the Reservations that occupied
					// at least one day from the desired range. We need to filter the map to preserve only the dates
					// from the desired range.
					.entrySet()
					.stream()
					.filter(entry -> desiredDates.contains(entry.getKey()))
					.forEach(entry -> availabilityMap.merge(entry.getKey(), entry.getValue(), Long::sum));
		} while (!reservations.isEmpty());
		// However, not all the dates within the desired range will be in the Map that we created, because we flattened
		// the existing reservations. We must merge the Map with all the dates - the existing and the missing ones
		desiredDates.forEach(date -> availabilityMap.merge(date, 0L, Long::sum));
		return availabilityMap;
	}

	/**
	 * Checks if the {@link DateRange} is valid. Otherwise, throws a {@link InvalidRangeException}
	 */
	private void checkValidRange(DateRange dateRange) {
		if (!dateRange.isValid()) {
			throw new InvalidRangeException(dateRange);
		}
	}
}
