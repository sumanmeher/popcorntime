package com.popcorntime.org.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.popcorntime.org.entity.Movie;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

}
