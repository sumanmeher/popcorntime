package com.popcorntime.org.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.popcorntime.org.entity.City;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {

}
