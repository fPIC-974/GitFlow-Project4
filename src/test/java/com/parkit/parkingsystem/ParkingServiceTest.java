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
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    private static ParkingService parkingService;

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;

    @BeforeEach
    public void setUpPerTest() {
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
            Ticket ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber("ABCDEF");
            when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
            when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);

            when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }

    @Test
    public void processExitingVehicleTest(){
        //FareCalculatorService fareCalculatorService = mock(FareCalculatorService.class);
        when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(0);
        parkingService.processExitingVehicle();

        // FPIC-974 - Alternative - fareCalculatorService not seen as invoked

        //verify(fareCalculatorService).calculateFare(any(Ticket.class));
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
    }

    @Test
    public void testProcessIncomingVehicle() {
        reset(ticketDAO);
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(ticketDAO.getNbTicket(anyString())).thenReturn(0);
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);

        parkingService.processIncomingVehicle();

        verify(parkingSpotDAO).updateParking(any(ParkingSpot.class));
        verify(ticketDAO).saveTicket(any(Ticket.class));
    }

    @Test
    public void processExitingVehicleTestUnableUpdate() {
        // Setting test case values
        when(ticketDAO.getNbTicket(anyString())).thenReturn(0);
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);

        // Call method to test
        parkingService.processExitingVehicle();

        // Verification method
        verifyNoInteractions(parkingSpotDAO);

        reset(ticketDAO);
        reset(parkingSpotDAO);
    }

    @Test
    public void testGetNextParkingNumberIfAvailable() {
        // Setting test case values
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(ticketDAO.getNbTicket(anyString())).thenReturn(0);
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);

        // Call method to test
        ParkingSpot toCheck = parkingService.getNextParkingNumberIfAvailable();

        // Assert method
        assertEquals(1, toCheck.getId());

        reset(inputReaderUtil);
        reset(parkingSpotDAO);
        reset(ticketDAO);
    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() {
        // Setting test case values
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(ticketDAO.getNbTicket(anyString())).thenReturn(0);
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(0);

        // Call method to test
        ParkingSpot toCheck = parkingService.getNextParkingNumberIfAvailable();

        // Assert method
        assertNull(toCheck);

        // FPIC-974 - Alternative using assertions
/*        Exception exception = Assertions.assertThrows(Exception.class,
                () -> parkingService.getNextParkingNumberIfAvailable());
        Assertions.assertEquals(
                "Error fetching parking number from DB. Parking slots might be full", exception.getMessage());*/

        reset(inputReaderUtil);
        reset(parkingSpotDAO);
        reset(ticketDAO);
    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() {
        // Setting test case values
        when(inputReaderUtil.readSelection()).thenReturn(3);
        when(ticketDAO.getNbTicket(anyString())).thenReturn(0);

        // Call method to test
        ParkingSpot toCheck = parkingService.getNextParkingNumberIfAvailable();

        // Verification method
        verifyNoInteractions(parkingSpotDAO);
        assertNull(toCheck);

        reset(inputReaderUtil);
        reset(ticketDAO);
        reset(parkingSpotDAO);
    }
}
