package com.popcorntime.org.repository;

import com.popcorntime.org.dto.BookingResponse;
import com.popcorntime.org.dto.SeatResponse;
import com.popcorntime.org.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    @Query(value = "select s.id as seatId, s.seat_number as seatNumber, sho.seat_price as seatPrice from seat s  " +
            "join \"show\" sho " +
            "on sho.screen_id  = s.screen_id  " +
            "where sho.id = :showId " +
            "and s.id not in (select bd.seat_id  from booking_details bd  where bd.show_id  = :showId )" +
            "order by s.seat_number "
            , nativeQuery = true)
    List<SeatResponse> getAvailableSeats(@Param(value = "showId") Long showId);

    @Query(value = "select s.id as seatId, sho.seat_price as seatPrice from seat s " +
            "join \"show\" sho  " +
            "on sho.screen_id  = s.screen_id  " +
            "where sho.id = :showId " +
            "and s.id in :seatIds and s.id not in (select bd.seat_id  from booking_details bd  where bd.show_id  = :showId )"
            , nativeQuery = true)
    List<BookingResponse> getSeats(@Param(value = "showId") Long showId, @Param(value = "seatIds") List<Long> seatIds);

    List<Seat> findAllByScreenId(Long screenId);

    List<Seat> findAllByScreenIdAndSeatNumberIn(Long screenId, List<Short> seatIds);

    @Query(value = "select s.id  from seat s " +
            "where s.id in :seatIds ;", nativeQuery = true)
    List<Long> getAllExistingSeatIds(@Param(value = "seatIds") List<Long> seatIds);
}
