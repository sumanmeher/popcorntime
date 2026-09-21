package com.popcorntime.org.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.popcorntime.org.dto.TheatreDetails;
import com.popcorntime.org.entity.Theatre;

import io.lettuce.core.dynamic.annotation.Param;

@Repository
public interface TheatreRepository extends JpaRepository<Theatre, Long> {

    @Query(value = "select t.id, t.name, t.address from theatre t " +
            "join city c " +
            "on t.city_id = c.id " +
            " where t.city_id = :cityId ", nativeQuery = true)
    List<TheatreDetails> getTheatres(@Param(value = "cityId") Long cityId);

}
