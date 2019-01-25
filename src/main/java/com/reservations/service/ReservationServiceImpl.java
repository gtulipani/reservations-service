package com.reservations.service;

import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.reservations.entity.Reservation;
import com.reservations.entity.ReservationStatus;
import com.reservations.exception.ReservationNotFoundException;
import com.reservations.repository.ReservationRepository;
import com.reservations.validation.Validator;

@Slf4j
@Service
public class ReservationServiceImpl implements ReservationService {
	private final ReservationRepository reservationRepository;
	private final Validator<Reservation> reservationValidator;

	@Autowired
	public ReservationServiceImpl(ReservationRepository reservationRepository, Validator<Reservation> reservationValidator) {
		this.reservationRepository = reservationRepository;
		this.reservationValidator = reservationValidator;
	}

	@Override
	public Reservation createReservation(Reservation reservation) {
		reservation.setStatus(ReservationStatus.ACTIVE);
		reservationRepository.save(reservation);
		log.info("Successfully created reservation={}", reservation);
		return reservation;
	}

	@Override
	public Reservation getByBookingIdentifierUuid(String bookingIdentifierUuid){
		return reservationRepository.findByBookingIdentifierUuid(bookingIdentifierUuid)
				.orElseThrow(() -> new ReservationNotFoundException(bookingIdentifierUuid));
	}

	@Override
	public Reservation updateReservation(Reservation oldReservation, Reservation newReservation) {
		Reservation updatedReservation = patchReservation(oldReservation, newReservation);
		reservationRepository.save(updatedReservation);
		log.info("Successfully updated reservation={}", updatedReservation);
		return updatedReservation;
	}

	@Override
	public void cancelReservation(Reservation reservation) {
		
	}

	private Reservation patchReservation(Reservation oldReservation, Reservation newReservation) {
		if (Objects.nonNull(newReservation.getArrivalDate())) {
			oldReservation.setArrivalDate(newReservation.getArrivalDate());
		}
		if (Objects.nonNull(newReservation.getDepartureDate())) {
			oldReservation.setDepartureDate(newReservation.getDepartureDate());
		}
		reservationValidator.validate(oldReservation);
		return oldReservation;
	}
}
