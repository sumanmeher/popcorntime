package com.popcorntime.org.repository;

import com.popcorntime.org.dto.ShowTimeDetailsResponse;
import com.popcorntime.org.dto.MovieShowDetails;
import com.popcorntime.org.dto.MovieShowTheatreDetails;
import com.popcorntime.org.entity.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;
import java.util.Set;

@Repository
public interface MovieShowRepository extends JpaRepository<Show, Long> {

    @Query(value = "select sho.id as showId,sho.\"date\" ,sho.show_time as showTime, sho.seat_price as seatPrice, srn.id as screenId, srn.\"name\" as screenName, t.id as theatreId, t.\"name\" as theatreName, t.address as theatreAddress from show sho  " +
            "join screen srn " +
            "on sho.screen_id = srn.id " +
            "join theatre t " +
            "on t.id = srn.theatre_id " +
            "where ((sho.date = current_date and sho.show_time >= current_time) or (sho.date > current_date)) " +
            "and t.city_id = :cityId and sho.movie_id = :movieId " +
            "order by sho.date, sho.show_time ;",
            nativeQuery = true)
    List<MovieShowTheatreDetails> getAllUpcomingShows(@Param(value = "cityId") Long cityId, @Param(value = "movieId") Long movieId);

    @Query(value = "select scr.id as screenId,scr.\"name\" screenName,m.id as movieId,m.\"name\" as movieName,sho.\"date\" ,sho.show_time as showTime,sho.id as showId, sho.seat_price as seatPrice from show sho " +
            "join movie m  " +
            "on m.id = sho.movie_id " +
            "join screen scr " +
            "on scr.id = sho.screen_id  " +
            "where ((sho.date = current_date and sho.show_time >= current_time) or (sho.date > current_date)) " +
            "and scr.theatre_id = :theatreId " +
            "order by sho.date, sho.show_time ",
            nativeQuery = true)
    List<MovieShowDetails> getAllUpcomingShows(@Param(value = "theatreId") Long theatreId);

    List<Show> findAllByScreenIdAndMovieIdAndDateIn(Long screenId, Long movieId, Set<Date> movieShowDates);

    @Query(value = "select s.\"date\" ,s.show_time as showTime from \"show\" s  " +
            "where s.screen_id = :screenId and s.movie_id = :movieId " +
            "and s.\"date\" in :movieShowDates ",
            nativeQuery = true)
    List<ShowTimeDetailsResponse> getShows(@Param(value = "screenId") Long screenId, @Param(value = "movieId") Long movieId,@Param(value = "movieShowDates") Set<Date> movieShowDates);
}
