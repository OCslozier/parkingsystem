package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

	public void calculateFare(Ticket ticket, boolean discount) {
		
		if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
			throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
		}

		long inTime = ticket.getInTime().getTime();
		long outTime = ticket.getOutTime().getTime();

		double duration = ((outTime - inTime) / (60.0 * 60 * 1000));

		if (duration < 0.5) { // Si en dessous de 30 min de stationnement
			duration = 0;
		}

		switch (ticket.getParkingSpot().getParkingType()) {
		case CAR: {
			if (discount) {
				ticket.setPrice(duration * Fare.DISCOUNT_CAR_RATE_PER_HOUR);
			} else {
				ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR);
			}
			break;

		}
		case BIKE: {
			if (discount) {
				ticket.setPrice(duration * Fare.DISCOUNT_BIKE_RATE_PER_HOUR);
			} else {
				ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR);
			}
			break;

		}
		default:
			throw new IllegalArgumentException("Unkown Parking Type");
		}
	}

}
