package com.popcorntime.org.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.popcorntime.org.dto.MovieResponse;
import com.popcorntime.org.entity.CityMovie;

@Repository
public interface CityMovieRepository extends JpaRepository<CityMovie, Long> {

    @Query(value = "select mo.id, mo.name from city_movie cm " +
            "join city ci " +
            "on ci.id = cm.city_id " +
            "join movie mo " +
            "on mo.id = cm.movie_id " +
            "where ci.id = :cityId and cm.available_till_date >= current_date", nativeQuery = true)
    List<MovieResponse> getMovies(@Param(value = "cityId") Long cityId);
}
