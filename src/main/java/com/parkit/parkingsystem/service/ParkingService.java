package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;

public class ParkingService {

	private static final Logger logger = LogManager.getLogger("ParkingService");

	private static FareCalculatorService fareCalculatorService = new FareCalculatorService();

	private InputReaderUtil inputReaderUtil;
	private ParkingSpotDAO parkingSpotDAO;
	private TicketDAO ticketDAO;

	public ParkingService(InputReaderUtil inputReaderUtil, ParkingSpotDAO parkingSpotDAO, TicketDAO ticketDAO) {
		this.inputReaderUtil = inputReaderUtil;
		this.parkingSpotDAO = parkingSpotDAO;
		this.ticketDAO = ticketDAO;
	}

	public void processIncomingVehicle() {
		try {
			ParkingSpot parkingSpot = getNextParkingNumberIfAvailable();
			if (parkingSpot != null && parkingSpot.getId() > 0) {
				String vehicleRegNumber = getVehichleRegNumber();
				parkingSpot.setAvailable(false);
				parkingSpotDAO.updateParking(parkingSpot);// allot this parking space and mark it's availability as
															// false

				int count = ticketDAO.countTicket(vehicleRegNumber);

				Date inTime = new Date();
				Ticket ticket = new Ticket();
				// ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
				// ticket.setId(ticketID);
				ticket.setParkingSpot(parkingSpot);
				ticket.setVehicleRegNumber(vehicleRegNumber);
				ticket.setPrice(0);
				ticket.setInTime(inTime);
				ticket.setOutTime(null);
				ticketDAO.saveTicket(ticket);

				if (count < 1) {

					System.out.println("          W E L CO M E             ");
					System.out.println("***********************************");
					System.out.println("* Hello, this is your first visit *");
					System.out.println("* so allow me to welcome you  :-) *");
					System.out.println("***********************************");
					System.out.println("         P R I C I N G             ");
					System.out.println("***********************************");
					System.out.println("* Current pricing: 1.5€/h (CARS)  *");
					System.out.println("* Current pricing: 1.0€/h (BIKE)  *");
					System.out.println("* Less 30 minutes park free       *");
					System.out.println("* Fidelity pricing: -5% 2nd visit *");
					System.out.println("***********************************");
					System.out.println("");
					System.out.println("Generated Ticket and saved in DB");
					System.out.println("Please park your vehicle in spot number:" + parkingSpot.getId());
					System.out.println("Recorded in-time for vehicle number:" + vehicleRegNumber + " is:" + inTime);
					System.out.println("");
					System.out.println("");

				} else {

					System.out.println("     !!HAPPY TO SEE YOU AGAIN!!     ");
					System.out.println("************************************");
					System.out.println("*      Hello,already back ?        *");
					System.out.println("*   Thank you for your loyalty     *");
					System.out.println("************************************");
					System.out.println("   F I D E L I T Y  P R I C I N G   ");
					System.out.println("************************************");
					System.out.println("* Current pricing: 1.425€/h (CARS) *");
					System.out.println("* Current pricing: 0.95 €/h (BIKE) *");
					System.out.println("* Less 30 minutes park free        *");
					System.out.println("************************************");
					System.out.println("");
					System.out.println("Number of visits before today :" +count);
					System.out.println("Generated Ticket and saved in DB");
					System.out.println("Please park your vehicle in spot number:" + parkingSpot.getId());
					System.out.println("Recorded in-time for vehicle number:" + vehicleRegNumber + " is:" + inTime);
					System.out.println("");
					System.out.println("");

				}
			}

		} catch (Exception e) {
			logger.error("Unable to process incoming vehicle", e);
		}
	}

	public String getVehichleRegNumber() throws Exception {
		System.out.println("Please type the vehicle registration number and press enter key");
		return inputReaderUtil.readVehicleRegistrationNumber();
	}

	public ParkingSpot getNextParkingNumberIfAvailable() {
		int parkingNumber = 0;
		ParkingSpot parkingSpot = null;
		try {
			ParkingType parkingType = getVehichleType();
			parkingNumber = parkingSpotDAO.getNextAvailableSlot(parkingType);
			if (parkingNumber > 0) {
				parkingSpot = new ParkingSpot(parkingNumber, parkingType, true);
			} else {
				throw new Exception("Error fetching parking number from DB. Parking slots might be full");
			}
		} catch (IllegalArgumentException ie) {
			logger.error("Error parsing user input for type of vehicle", ie);
		} catch (Exception e) {
			logger.error("Error fetching next available parking slot", e);
		}
		return parkingSpot;
	}

	private ParkingType getVehichleType() {
		System.out.println("Please select vehicle type from menu");
		System.out.println("1 CAR");
		System.out.println("2 BIKE");
		int input = inputReaderUtil.readSelection();
		switch (input) {
		case 1: {
			return ParkingType.CAR;
		}
		case 2: {
			return ParkingType.BIKE;
		}
		default: {
			System.out.println("Incorrect input provided");
			throw new IllegalArgumentException("Entered input is invalid");
		}
		}
	}

	public void processExitingVehicle() {
		try {
			String vehicleRegNumber = getVehichleRegNumber();
			Ticket ticket = ticketDAO.getTicket(vehicleRegNumber);

			int count = ticketDAO.countTicket(vehicleRegNumber);
			Date outTime = new Date();
			ticket.setOutTime(outTime);

			// fareCalculatorService.calculateFare(ticket);

			if (count > 1) {
				fareCalculatorService.calculateFare(ticket, true); // Appliquer la réduction
			} else {
				fareCalculatorService.calculateFare(ticket, false); // Pas de réduction
			}

			if (ticketDAO.updateTicket(ticket)) {
				ParkingSpot parkingSpot = ticket.getParkingSpot();
				parkingSpot.setAvailable(true);
				parkingSpotDAO.updateParking(parkingSpot);
				System.out.println("Nombre de records:" + count);
				System.out.println("Please pay the parking fare:" + ticket.getPrice());
				System.out.println(
						"Recorded out-time for vehicle number:" + ticket.getVehicleRegNumber() + " is:" + outTime);
			} else {
				System.out.println("Unable to update ticket information. Error occurred");
			}
		} catch (Exception e) {
			logger.error("Unable to process exiting vehicle", e);
		}
	}
}
