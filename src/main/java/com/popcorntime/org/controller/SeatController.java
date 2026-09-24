package com.popcorntime.org.controller;

import com.popcorntime.org.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping(value = "/v1")
public class SeatController {
    @Autowired
    private SeatService seatService;

    /**
     * Retrieves the availability status of all seats for a specific movie show.
     *
     * @param showId The unique identifier of the movie show.
     * @return A list of seats and their availability status.
     */
    @PreAuthorize("hasAuthority('USER') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    @GetMapping("/cities/theatres/movies/screens/shows/seats")
    public ResponseEntity<Object> getSeats(@RequestParam("showId") final Long showId) {
        return ResponseEntity.ok().body(seatService.getSeats(showId));
    }

    /**
     * Retrieves all physical seat configurations for a specific screen in a theatre.
     *
     * @param theatreId The unique identifier of the theatre.
     * @param screenId The unique identifier of the screen.
     * @return A list of all seats belonging to the screen.
     */
    @PreAuthorize("hasAuthority('USER') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    @GetMapping("/cities/theatres/screens/seats")
    public ResponseEntity<Object> getAllSeats(@RequestParam("theatreId") final Long theatreId,
            @RequestParam("screenId") final Long screenId) {
        return ResponseEntity.ok().body(seatService.getSeats(theatreId, screenId));
    }

    /**
     * Adds a range of physical seats to a specified screen.
     *
     * @param screenId The unique identifier of the screen.
     * @param startSeatNumber The starting seat number in the range.
     * @param endSeatNumber The ending seat number in the range.
     * @return A success message confirming the addition of the seats.
     */
    @PreAuthorize("hasAuthority('MANAGER')")
    @PostMapping("/cities/theatres/screens/seats")
    public ResponseEntity<Object> addSeats(@RequestParam("screenId") final Long screenId,
            @RequestParam(name = "startSeatNumber") Short startSeatNumber,
            @RequestParam(name = "endSeatNumber") Short endSeatNumber) {
        return ResponseEntity.ok().body(seatService.addSeats(screenId, startSeatNumber, endSeatNumber));
    }

    /**
     * Temporarily books a set of selected seats for a specific show for the current user.
     *
     * @param showId The unique identifier of the movie show.
     * @param seatTimeSlotIds A list of seat IDs to book.
     * @return A success message confirming the temporary booking.
     */
    @PreAuthorize("hasAuthority('USER')")
    @PostMapping("/cities/theatres/movies/screens/shows/seats")
    public ResponseEntity<Object> bookSeats(@RequestParam("showId") final Long showId,
            @RequestBody List<Long> seatTimeSlotIds) {
        return ResponseEntity.ok().body(seatService.bookSeats(showId, seatTimeSlotIds));
    }

    /**
     * Finalizes the booking of temporarily reserved seats by processing the payment.
     *
     * @param showId The unique identifier of the movie show.
     * @param paymentRequest The payment details including the payment provider type and seat IDs.
     * @return A success or failure message regarding the payment transaction.
     */
    @PreAuthorize("hasAuthority('USER')")
    @PostMapping("/cities/theatres/movies/screens/shows/seats/checkout")
    public ResponseEntity<Object> checkout(@RequestParam("showId") final Long showId,
            @RequestBody com.popcorntime.org.payment.PaymentRequest paymentRequest) {
        return ResponseEntity.ok().body(seatService.checkout(showId, paymentRequest));
    }
}
