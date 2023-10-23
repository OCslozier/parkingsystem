package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

	@Mock
	private static ParkingService parkingService;
	@Mock
	private static InputReaderUtil inputReaderUtil;
	@Mock
	private static ParkingSpotDAO parkingSpotDAO; // DATA ACCESS OBJECT
	@Mock
	private static TicketDAO ticketDAO;

	@BeforeEach
	private void setUpPerTest() {
		try {

			parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to set up test mock objects");
		}
	}

	@Test
	public void testprocessExitingVehicle() { // nomenclature apex

		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
		Ticket ticket = new Ticket();
		ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
		ticket.setParkingSpot(parkingSpot);
		ticket.setVehicleRegNumber("ABCDEF");

		when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
		when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
		when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
		when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
		when(ticketDAO.getNbTicket(anyString())).thenReturn(1);

		parkingService.processExitingVehicle();

		verify(ticketDAO, Mockito.times(1)).getTicket("ABCDEF");
		verify(ticketDAO, Mockito.times(1)).getNbTicket("ABCDEF");
		verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
		verify(ticketDAO, Mockito.times(1)).updateTicket(ticket);

	}

	@Test
    public void testProcessIncomingVehicle() {
    	
		when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
		when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
    	when(inputReaderUtil.readSelection()).thenReturn(1);
    	when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
        when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);    	
    	when(ticketDAO.getNbTicket(anyString())).thenReturn(1);
    	
    	parkingService.processIncomingVehicle();
    	
    	verify(inputReaderUtil, Mockito.times(1)).readSelection();
    	verify(parkingSpotDAO, Mockito.times(1)).getNextAvailableSlot(ParkingType.CAR);
    	verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));
    	verify(ticketDAO, Mockito.times(1)).getNbTicket(anyString());
    }

	@Test
	public void testProcessExitingVehicleUnableUpdate() {

//  exécution du test dans le cas où la méthode updateTicket() de ticketDAO renvoie false lors de l’appel de processExitingVehicle()

		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
		Ticket ticket = new Ticket();
		ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
		ticket.setParkingSpot(parkingSpot);
		ticket.setVehicleRegNumber("ABCDEF");

		when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
		when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
		when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false); // echec MAJ ticket
		when(ticketDAO.getNbTicket(anyString())).thenReturn(1);

		parkingService.processExitingVehicle();

		verify(ticketDAO, Mockito.times(1)).getTicket("ABCDEF");
		verify(ticketDAO, Mockito.times(1)).updateTicket(ticket);
		verify(ticketDAO, Mockito.times(1)).getNbTicket("ABCDEF");
		verify(parkingSpotDAO, never()).updateParking(any(ParkingSpot.class));
		// On vérifie que parkingSpotDAO.updateParking(any(ParkingSpot.class)) n'a pas
		// été appelé, car la mise à jour du ticket a échoué

	}

	@Test
	public void testGetNextParkingNumberIfAvailable() {

		// test de l’appel de la méthode getNextParkingNumberIfAvailable() avec pour
		// résultat l’obtention d’un spot dont l’ID est 1 et qui est disponible.

		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
		Ticket ticket = new Ticket();
		ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
		ticket.setParkingSpot(parkingSpot);
		ticket.setVehicleRegNumber("ABCDEF");

		when(inputReaderUtil.readSelection()).thenReturn(1);
		when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);

		parkingService.getNextParkingNumberIfAvailable();

		verify(inputReaderUtil, Mockito.times(1)).readSelection();
		verify(parkingSpotDAO, Mockito.times(1)).getNextAvailableSlot(ParkingType.CAR);

	}

	@Test
	public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() {

		// test de l’appel de la méthode getNextParkingNumberIfAvailable() avec pour
		// résultat l’obtention d’un spot dont l’ID est 1 et qui est disponible.

		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
		Ticket ticket = new Ticket();
		ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
		ticket.setParkingSpot(parkingSpot);
		ticket.setVehicleRegNumber("ABCDEF");

		when(inputReaderUtil.readSelection()).thenReturn(1);
		when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(0);

		parkingService.getNextParkingNumberIfAvailable();

		verify(inputReaderUtil, Mockito.times(1)).readSelection();
		verify(parkingSpotDAO, Mockito.times(1)).getNextAvailableSlot(ParkingType.CAR);

	}

	@Test
	public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() {

		// test de l’appel de la méthode getNextParkingNumberIfAvailable() avec pour
		// résultat l’obtention d’un spot dont l’ID est 1 et qui est disponible.

		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
		Ticket ticket = new Ticket();
		ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
		ticket.setParkingSpot(parkingSpot);
		ticket.setVehicleRegNumber("ABCDEF");

		when(inputReaderUtil.readSelection()).thenReturn(3);

		parkingService.getNextParkingNumberIfAvailable();

		verify(inputReaderUtil, Mockito.times(1)).readSelection();

	}

}