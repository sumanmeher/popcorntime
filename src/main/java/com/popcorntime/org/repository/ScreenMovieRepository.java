package com.popcorntime.org.repository;

import com.popcorntime.org.entity.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScreenMovieRepository extends JpaRepository<Show, Long> {
    List<Show> findAllByMovieIdAndScreenId(Long movieId, Long screenId);
}
