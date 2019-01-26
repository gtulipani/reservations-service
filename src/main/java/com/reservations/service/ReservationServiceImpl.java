package com.reservations.service;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.reservations.entity.EventType;
import com.reservations.entity.Reservation;
import com.reservations.entity.ReservationStatus;
import com.reservations.exception.ReservationNotFoundException;
import com.reservations.extensibility.utils.ExtensionUtils;
import com.reservations.repository.ReservationRepository;
import com.reservations.validation.ReservationValidatorExtension;

@Slf4j
@Service
public class ReservationServiceImpl implements ReservationService {
	private final ReservationRepository reservationRepository;
	private final Set<ReservationValidatorExtension> reservationValidatorExtensions;
	private final int maxCapacity;

	@Autowired
	public ReservationServiceImpl(ReservationRepository reservationRepository,
								  Set<ReservationValidatorExtension> reservationValidatorExtensions,
								  @Value("${reservations.max-capacity}") int maxCapacity) {
		this.reservationRepository = reservationRepository;
		this.reservationValidatorExtensions = reservationValidatorExtensions;
		this.maxCapacity = maxCapacity;
	}

	@Override
	public boolean checkAvailability(LocalDate start, LocalDate end) {
		return true;
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

	private Reservation patchReservation(Reservation oldReservation, Reservation newReservation) {
		if (Objects.nonNull(newReservation.getArrivalDate())) {
			oldReservation.setArrivalDate(newReservation.getArrivalDate());
		}
		if (Objects.nonNull(newReservation.getDepartureDate())) {
			oldReservation.setDepartureDate(newReservation.getDepartureDate());
		}
		return oldReservation;
	}
}
