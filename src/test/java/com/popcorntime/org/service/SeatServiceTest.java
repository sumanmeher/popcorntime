package com.popcorntime.org.service;

import com.popcorntime.org.dto.BookingResponse;
import com.popcorntime.org.dto.EmailDetails;
import com.popcorntime.org.dto.UserBookingDetails;
import com.popcorntime.org.entity.*;
import com.popcorntime.org.exception.BadRequestException;
import com.popcorntime.org.exception.NotFoundException;
import com.popcorntime.org.payment.PaymentGatewayService;
import com.popcorntime.org.payment.PaymentRequest;
import com.popcorntime.org.payment.PaymentResponse;
import com.popcorntime.org.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeatServiceTest {

    @InjectMocks
    private SeatService seatService;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private MovieShowRepository movieShowRepository;

    @Mock
    private PaymentGatewayService paymentGatewayService;

    @Mock
    private PaymentDetailsRepository paymentDetailsRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @BeforeEach
    void setUp() {
        // Set up Spring Security Context Mock
        UserDetails userDetails = User.withUsername("test@example.com").password("pass").authorities("USER").build();
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testCheckout_Success() {
        // 1. Setup Data
        Long showId = 1L;
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setSeatIds(new HashSet<>(Arrays.asList(101L, 102L)));

        Show mockShow = new Show();
        mockShow.setId(showId);

        Seat seat1 = new Seat();
        seat1.setId(101L);
        Seat seat2 = new Seat();
        seat2.setId(102L);
        List<Seat> seats = Arrays.asList(seat1, seat2);

        UserBookingDetails booking1 = new UserBookingDetails();
        booking1.setSeatId(101L);
        booking1.setShowId(showId);
        booking1.setPrice(100.0);
        UserBookingDetails booking2 = new UserBookingDetails();
        booking2.setSeatId(102L);
        booking2.setShowId(showId);
        booking2.setPrice(100.0);
        List<UserBookingDetails> redisBookingDetails = Arrays.asList(booking1, booking2);

        // 2. Mocking DB & Redis Calls
        when(movieShowRepository.findById(showId)).thenReturn(Optional.of(mockShow));
        when(seatRepository.findAllById(paymentRequest.getSeatIds())).thenReturn(seats);
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.get("test@example.com", showId.toString())).thenReturn(redisBookingDetails);

        // Mock to pass the duplicate check
        BookingResponse br1 = mock(BookingResponse.class);
        BookingResponse br2 = mock(BookingResponse.class);
        when(seatRepository.getSeats(eq(showId), anyList()))
                .thenReturn(Arrays.asList(br1, br2));

        PaymentResponse mockPaymentResponse = new PaymentResponse();
        mockPaymentResponse.setChargeId("ch_12345");
        when(paymentGatewayService.paymentGateway(any(PaymentRequest.class), eq(200.0)))
                .thenReturn(mockPaymentResponse);

        AppUser appUser = new AppUser();
        appUser.setEmail("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(appUser));

        // 3. Execute Method
        String result = seatService.checkout(showId, paymentRequest);

        // 4. Assertions & Verifications
        assertEquals("Payment Successful", result);

        // Verify payment was saved
        verify(paymentDetailsRepository, times(1)).save(any(PaymentDetails.class));

        // Verify async email was sent to RabbitMQ!
        verify(rabbitTemplate, times(1)).convertAndSend(any(), any(), any(EmailDetails.class));

        // Verify Redis lock was deleted
        verify(hashOperations, times(1)).delete("test@example.com", "test@example.com");
        verify(hashOperations, times(1)).delete("test@example.com", showId.toString());
    }

    @Test
    void testCheckout_ThrowsBadRequest_WhenSeatsAlreadyFullyBooked() {
        Long showId = 1L;
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setSeatIds(new HashSet<>(Arrays.asList(101L)));

        when(movieShowRepository.findById(showId)).thenReturn(Optional.of(new Show()));
        Seat seat1 = new Seat();
        seat1.setId(101L);
        when(seatRepository.findAllById(paymentRequest.getSeatIds())).thenReturn(Arrays.asList(seat1));

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        UserBookingDetails booking1 = new UserBookingDetails();
        booking1.setSeatId(101L);
        booking1.setShowId(showId);
        booking1.setPrice(100.0);
        when(hashOperations.get("test@example.com", showId.toString())).thenReturn(Arrays.asList(booking1));

        // Simulate that the DB duplicate check returns 0 available seats
        when(seatRepository.getSeats(eq(showId), anyList())).thenReturn(Collections.emptyList());

        // Assert Exception
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            seatService.checkout(showId, paymentRequest);
        });

        assertEquals("Sorry, these seats have already been fully booked.", exception.getMessage());
        verify(paymentGatewayService, never()).paymentGateway(any(), any());
    }
}
