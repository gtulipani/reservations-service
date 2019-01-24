package com.reservations.service;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.reservations.entity.Reservation;
import com.reservations.entity.ReservationStatus;
import com.reservations.repository.ReservationRepository;

@Slf4j
@Service
public class ReservationServiceImpl implements ReservationService {
	private final ReservationRepository reservationRepository;

	@Autowired
	public ReservationServiceImpl(ReservationRepository reservationRepository) {
		this.reservationRepository = reservationRepository;
	}

	@Override
	public Reservation createReservation(Reservation reservation) {
		reservation.setStatus(ReservationStatus.ACTIVE);
		reservationRepository.save(reservation);
		log.info("Reservation succesfully saved reservation={}", reservation);
		return reservation;
	}
}
