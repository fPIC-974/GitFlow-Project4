package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    public static void setUp() {
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    public void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    public static void tearDown(){

    }

    @Test
    public void testParkingACar(){
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();

        ticketDAO.updateInTime(ticketDAO.getTicket("ABCDEF"));
        ticketDAO.updateInTime(ticketDAO.getTicket("ABCDEF"));

        Ticket toCheckTicket = ticketDAO.getTicket("ABCDEF");
        int toCheckSpot = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);

        assertEquals(1, toCheckTicket.getParkingSpot().getId());
        assertEquals(2, toCheckSpot);

    }

    @Test
    public void testParkingLotExit(){
        testParkingACar();
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processExitingVehicle();

        Ticket toCheckTicket = ticketDAO.getTicket("ABCDEF");
        int toCheckSpot = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);

        assertEquals(2 * 1.5, toCheckTicket.getPrice(), 0.01);
        assertNotNull(toCheckTicket.getOutTime());

        assertEquals(1, toCheckSpot);
    }

    @Test
    public void testParkingLotExitRecurringUser() {
        testParkingLotExit();

        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        parkingService.processIncomingVehicle();

        ticketDAO.updateInTime(ticketDAO.getTicket("ABCDEF"));

        parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        parkingService.processExitingVehicle();

        assertEquals(2, ticketDAO.getNbTicket("ABCDEF"));
        assertNotNull(ticketDAO.getTicket("ABCDEF").getOutTime());
        assertEquals(0.95 * Fare.CAR_RATE_PER_HOUR, ticketDAO.getTicket("ABCDEF").getPrice(), 0.01);
    }
}
