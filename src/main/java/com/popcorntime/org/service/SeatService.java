package com.popcorntime.org.service;

import com.popcorntime.org.dto.*;
import com.popcorntime.org.entity.*;
import com.popcorntime.org.exception.BadRequestException;
import com.popcorntime.org.exception.NotFoundException;
import com.popcorntime.org.payment.PaymentGatewayService;
import com.popcorntime.org.payment.PaymentRequest;
import com.popcorntime.org.payment.PaymentResponse;
import com.popcorntime.org.repository.*;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;

@Service
@Slf4j
/**
 * Service class for handling all seat-related operations.
 * This includes fetching available seats, temporarily holding seats via Redis,
 * processing checkout and payments, and persisting final bookings to the database.
 */
public class SeatService {
    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private MovieShowRepository movieShowRepository;
    @Autowired
    private PaymentGatewayService paymentGatewayService;
    @Autowired
    private PaymentDetailsRepository paymentDetailsRepository;
    @Autowired
    private ScreenRepository screenRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${redis.key.expiryTimeInMinutes}")
    private long redisKeyExpiryTimeInMinutes;

    @Value("${rabbitmq.email.exchange.name}")
    private String emailExchangeName;
    @Value("${rabbitmq.email.routing.key}")
    private String emailRoutingKey;

    @Value("${rabbitmq.payment.details.unsaved.exchange.name}")
    private String unsavedPaymentDetailsExchange;
    @Value("${rabbitmq.payment.details.unsaved.routing.key}")
    private String unsavedPaymentDetailsRoutingKey;


    /**
     * Retrieves a list of available seats for a specific movie show time slot.
     *
     * @param showId The unique identifier of the scheduled show.
     * @return A list of {@link SeatResponse} representing available seats.
     * @throws NotFoundException If the given show ID does not exist.
     */
    public List<SeatResponse> getSeats(Long showId) {
        movieShowRepository.findById(showId).orElseThrow(() -> new NotFoundException("Movie Time Slot does not exist for given Id."));
        return seatRepository.getAvailableSeats(showId);
    }


    /**
     * Retrieves all physical seats associated with a specific screen within a theatre.
     *
     * @param theatreId The unique identifier of the theatre.
     * @param screenId The unique identifier of the screen.
     * @return A list of {@link Seat} entities belonging to the screen.
     * @throws BadRequestException If the screen ID does not belong to the specified theatre.
     */
    public List<Seat> getSeats(Long theatreId, Long screenId) {
        screenRepository.findByIdAndTheatreId(screenId, theatreId).orElseThrow(() -> new BadRequestException("ScreenId does not belong to theatreId."));
        return seatRepository.findAllByScreenId(screenId);
    }

    /**
     * Temporarily blocks a list of requested seats for a specific show so they can be booked by the current user.
     * Validates availability and applies a temporary Redis lock.
     *
     * @param showId The unique identifier of the scheduled show.
     * @param seatIds The list of seat IDs the user intends to book.
     * @return A message confirming the temporary booking.
     * @throws BadRequestException If seats are already booked, or locked by another user.
     */
    public String bookSeats(Long showId, List<Long> seatIds) {
        validateSeatsAndShow(seatIds, showId);

        List<BookingResponse> bookingResponses = seatRepository.getSeats(showId, seatIds);
        if (bookingResponses.size() != seatIds.size()) {
            throw new BadRequestException("Sorry, some of the selected seats have been booked by this time.");
        }
        validateIfSeatsAreBookedTemporarily(showId, seatIds);

        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userName = userDetails.getUsername();
        List<UserBookingDetails> userBookingDetailsList = new ArrayList<>();
        Duration expiryDurationInMinutes = Duration.ofMinutes(redisKeyExpiryTimeInMinutes);

        for (BookingResponse bookingResponse : bookingResponses) {
            UserBookingDetails userBookingDetails = getUserBokkingDetails(bookingResponse, showId);
            String key = getKey(showId, bookingResponse.getSeatId());
            redisTemplate.opsForValue().set(key, bookingResponse.getSeatId(), expiryDurationInMinutes);
            userBookingDetailsList.add(userBookingDetails);
        }
        redisTemplate.opsForHash().put(userName, showId.toString(), userBookingDetailsList);
        redisTemplate.expire(userName, Duration.ofMinutes(redisKeyExpiryTimeInMinutes));

        return "Seats Booked temporarily";
    }

    /**
     * Used to get Redis Key, using showId and seatId
     *
     * @param showId The unique identifier of the show.
     * @param seatId The unique identifier of the seat.
     * @return A formatted string acting as the Redis key.
     */
    private String getKey(Long showId, Long seatId) {
        return "key: showId-" + showId + ", " + "seatId-" + seatId;
    }

    /**
     * Used to check if request seats are already in Booking process by other user
     *
     * @param showId The unique identifier of the show.
     * @param seatIds The list of seat IDs to check.
     */
    private void validateIfSeatsAreBookedTemporarily(Long showId, List<Long> seatIds) {
        for (Long seatId : seatIds) {
            String key = getKey(showId,seatId);
            if (redisTemplate.opsForValue().get(key) != null) {
                throw new BadRequestException("Some of requested seats are being booked by someone else.");
            }
        }
    }

    /**
     * Used to validate SeatIds and Show
     *
     * @param seatIds The list of seat IDs to validate.
     * @param showId The unique identifier of the show.
     */
    private void validateSeatsAndShow(List<Long> seatIds, Long showId) {
        movieShowRepository.findById(showId).orElseThrow(() -> new NotFoundException("Show not found for given Id."));
        List<Long> existingSeatIds = seatRepository.getAllExistingSeatIds(seatIds);
        if (existingSeatIds.size() != seatIds.size()) {
            throw new BadRequestException("Invalid seats requested for Booking.");
        }
    }

    /**
     * Used to get User Booking details
     *
     * @param bookingResponse The response object containing seat price and ID.
     * @param showId The unique identifier of the show.
     * @return The populated {@link UserBookingDetails} object.
     */
    private UserBookingDetails getUserBokkingDetails(BookingResponse bookingResponse, Long showId) {
        UserBookingDetails userBookingDetails = new UserBookingDetails();
        userBookingDetails.setSeatId(bookingResponse.getSeatId());
        userBookingDetails.setPrice(bookingResponse.getSeatPrice());
        userBookingDetails.setShowId(showId);
        return userBookingDetails;
    }

    /**
     * Processes the final checkout for the temporarily booked seats.
     * Validates the booking against the Redis lock and database, triggers payment processing,
     * saves the final payment details, and clears the temporary lock.
     *
     * @param showId The unique identifier of the scheduled show.
     * @param paymentRequest The payment request details containing seat IDs and payment provider.
     * @return A success or failure message regarding the payment transaction.
     * @throws BadRequestException If validation fails or seats are already permanently booked.
     */
    public String checkout(Long showId, PaymentRequest paymentRequest) {
        Show timeSlot = movieShowRepository.findById(showId).orElseThrow(() -> new NotFoundException("Movie Show not found with given Id."));
        List<Seat> seats = seatRepository.findAllById(paymentRequest.getSeatIds());
        if (seats.size() != paymentRequest.getSeatIds().size()) {
            throw new BadRequestException("Invalid Seat Ids for Checkout request.");
        }

        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userName = userDetails.getUsername();
        Double totalPrice = (double) 0;
        Set<Long> seatIds = paymentRequest.getSeatIds();
        try {
            List<UserBookingDetails> list = getBookingDetailsForUser(userName, showId);
            if (list == null) throw new BadRequestException("Sorry, Invalid request");
            for (UserBookingDetails userBookingDetails : list) {
                if (!seatIds.contains(userBookingDetails.getSeatId()) || !userBookingDetails.getShowId().equals(showId))
                    throw new BadRequestException("Invalid Seat Bookings Requested.");
                totalPrice += userBookingDetails.getPrice();
            }
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }

        // PREVENT DUPLICATE TRANSACTIONS: Ensure the seats are not already booked in the database
        // before calling the payment gateway, in case a previous request saved them but crashed.
        List<BookingResponse> bookingResponses = seatRepository.getSeats(showId, new ArrayList<>(seatIds));
        if (bookingResponses.size() != seatIds.size()) {
            // Delete the stale Redis lock since the seats are actually booked permanently
            redisTemplate.opsForHash().delete(userName, showId.toString());
            throw new BadRequestException("Sorry, these seats have already been fully booked.");
        }

        // Generate Idempotency Key based on username, showId, and seatIds to prevent duplicate transactions
        String idempotencyKey = java.util.UUID.nameUUIDFromBytes((userName + "-" + showId + "-" + seatIds.toString()).getBytes()).toString();
        paymentRequest.setIdempotencyKey(idempotencyKey);

        PaymentResponse paymentResponse = paymentGatewayService.paymentGateway(paymentRequest, totalPrice);

        removeTemporaryBookingDetailsForUser(userName);
        if (paymentResponse != null && paymentResponse.getChargeId() != null) {
            try {
                savePaymentDetails(paymentResponse, userDetails, timeSlot, seats, totalPrice);
            } catch (Exception e) {
                log.error("Unsaved Payment::" + e.getMessage());
                rabbitTemplate.convertAndSend(unsavedPaymentDetailsExchange, unsavedPaymentDetailsRoutingKey, getPaymentDetails(userDetails.getUsername(), totalPrice, paymentRequest.getSeatIds(), showId, paymentResponse.getChargeId()));
            }
            EmailDetails emailDetails = getEmailDetails(userName, totalPrice, seatIds);
            rabbitTemplate.convertAndSend(emailExchangeName, emailRoutingKey, emailDetails);
            redisTemplate.opsForHash().delete(userName,showId.toString());
            return "Payment Successful";
        }
        return "Payment was unsuccessful.";
    }



    /**
     * Used to remove Booking details
     *
     * @param userName The username for which to remove the booking details.
     */
    private void removeTemporaryBookingDetailsForUser(String userName) {
        redisTemplate.opsForHash().delete(userName, userName);
    }

    /**
     * Used to Fetch Seats booked temporarily by User
     *
     * @param userName The username of the currently authenticated user.
     * @param showId The unique identifier of the show.
     * @return A list of {@link UserBookingDetails} retrieved from Redis.
     */
    private List<UserBookingDetails> getBookingDetailsForUser(String userName, Long showId) {
        try {
            return (List<UserBookingDetails>) redisTemplate.opsForHash().get(userName, showId.toString());
        }catch (Exception ex){
            throw new BadRequestException(ex.getMessage());
        }
    }

    /**
     * Used to get Payment Details
     *
     * @param username The email/username of the user making the payment.
     * @param totalPrice The total cost of the booking.
     * @param seatIds The set of seat IDs booked.
     * @param showId The unique identifier of the show.
     * @param chargeId The transaction reference returned by the payment gateway.
     * @return The populated {@link PaymentDetailsDto}.
     */
    private PaymentDetailsDto getPaymentDetails(String username, Double totalPrice, Set<Long> seatIds, Long showId, String chargeId) {
        PaymentDetailsDto paymentDetails = new PaymentDetailsDto();
        paymentDetails.setUserEmail(username);
        paymentDetails.setShowId(showId);
        paymentDetails.getSeatIds().addAll(seatIds);
        paymentDetails.setTotalPrice(totalPrice);
        paymentDetails.setChargeId(chargeId);
        return paymentDetails;
    }


    /**
     * Used to get Email Details
     *
     * @param userName The username of the receiver.
     * @param totalPrice The total cost of the booking.
     * @param seatIds The set of booked seat IDs.
     * @return The constructed {@link EmailDetails} object.
     */
    private EmailDetails getEmailDetails(String userName, Double totalPrice, Set<Long> seatIds) {
        return new EmailDetails(userName, totalPrice, new ArrayList<>(seatIds));
    }


    /**
     * Persists the final payment and booking details to the database after a successful charge,
     * confirming the permanent seat reservations.
     *
     * @param paymentResponse The successful response from the payment gateway.
     * @param userDetails The details of the currently authenticated user.
     * @param show The scheduled show being booked.
     * @param seats The list of physical seats being booked.
     * @param totalPrice The total price charged for this booking.
     */
    @Transactional
    private void savePaymentDetails(PaymentResponse paymentResponse, UserDetails userDetails, Show show, List<Seat> seats, Double totalPrice) {
        List<BookingDetails> bookingDetailsList = new ArrayList<>();
        AppUser user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow(() -> new NotFoundException("User not found for given Id."));

        PaymentDetails paymentDetails = new PaymentDetails();
        paymentDetails.setChargeId(paymentResponse.getChargeId());
        paymentDetails.setTotalBookingPrice(totalPrice);
        paymentDetails.setUser(user);

        seats.forEach(seat -> {
            paymentDetails.getBookingDetails().add(getTimeSlotSeat(show, seat, paymentDetails));
        });
        paymentDetailsRepository.save(paymentDetails);
    }

    /**
     * Used to create link for Movie-time and Seat
     *
     * @param show The scheduled show instance.
     * @param seat The booked physical seat.
     * @param paymentDetails The associated payment details.
     * @return A {@link BookingDetails} link entity connecting the show, seat, and payment.
     */
    private static BookingDetails getTimeSlotSeat(Show show, Seat seat, PaymentDetails paymentDetails) {
        BookingDetails bookingDetails = new BookingDetails();
        bookingDetails.setSeat(seat);
        bookingDetails.setShow(show);
        bookingDetails.setPaymentDetails(paymentDetails);
        return bookingDetails;
    }

    /**
     * Creates and saves new seat entities for a given screen within a specified range of seat numbers.
     *
     * @param screenId The unique identifier of the target screen.
     * @param startSeatNumber The starting number of the new seats.
     * @param endSeatNumber The ending number of the new seats.
     * @return A success message upon successfully saving the seats.
     * @throws BadRequestException If the specified range is invalid or seats already exist.
     */
    public String addSeats(Long screenId, Short startSeatNumber, Short endSeatNumber) {
        if (endSeatNumber < startSeatNumber) throw new BadRequestException("Invalid Seats requested");
        List<Short> seatIds = getSeatNumbers(startSeatNumber, endSeatNumber);

        Screen screen = screenRepository.findById(screenId).orElseThrow(() -> new NotFoundException("Screen not found for given Id."));
        List<Seat> existingSeats = seatRepository.findAllByScreenIdAndSeatNumberIn(screenId, seatIds);
        if (!existingSeats.isEmpty()) {
            throw new BadRequestException("Some of the requested Seats are already present in the Screen.");
        }
        List<Seat> seats = seatIds.stream().map(seatNumber -> {
            Seat seat = new Seat();
            seat.setSeatNumber(seatNumber);
            seat.setScreen(screen);
            return seat;
        }).toList();

        seatRepository.saveAll(seats);
        return "Seats added Successfully";
    }

    /**
     * Helper method to generate a sequence of seat numbers.
     *
     * @param startSeatNumber The starting seat number.
     * @param endSeatNumber The ending seat number.
     * @return A list of sequential seat numbers.
     */
    private List<Short> getSeatNumbers(Short startSeatNumber, Short endSeatNumber) {
        List<Short> seatNumbers = new ArrayList<>();
        for (Short i = startSeatNumber; i <= endSeatNumber; i++) {
            seatNumbers.add(i);
        }
        return seatNumbers;
    }
}
