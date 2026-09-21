package com.popcorntime.org.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.popcorntime.org.entity.Screen;
import com.popcorntime.org.entity.Theatre;

@Repository
public interface ScreenRepository extends JpaRepository<Screen, Long> {
    List<Screen> findAllByTheatreAndNameIn(Theatre theatre, List<String> screenRequests);

    Optional<Screen> findByIdAndTheatreId(Long id, Long theatreId);

    List<Screen> findAllByTheatreId(Long theatreId);
}
