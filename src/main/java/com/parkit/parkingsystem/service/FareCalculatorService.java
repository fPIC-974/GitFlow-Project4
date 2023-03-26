package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    // FPIC-974 Updating method to enable discount
    public void calculateFare(Ticket ticket, boolean discount){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        // FPIC-974 - Fix bug for time value
        // Get time in ms
        double inHour = ticket.getInTime().getTime();
        double outHour = ticket.getOutTime().getTime();

        // FPIC-974 - Convert duration to hours
        double duration = (outHour - inHour) / 3600000;

        // FPIC-974 - Free parking for less than 30mn
        if (duration <= 0.5) {
            duration = 0;
        }

        switch (ticket.getParkingSpot().getParkingType()){
            case CAR: {
                if (discount) {
                    ticket.setPrice(0.95 * duration * Fare.CAR_RATE_PER_HOUR);
                } else {
                    ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR);
                }
                break;
            }
            case BIKE: {
                if (discount) {
                    ticket.setPrice(0.95 * duration * Fare.BIKE_RATE_PER_HOUR);
                } else {
                    ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR);
                }
                break;
            }
            default: throw new IllegalArgumentException("Unknown Parking Type");
        }
    }

    // Method overload if no discount applied
    public void calculateFare(Ticket ticket) {
        calculateFare(ticket, false);
    }
}