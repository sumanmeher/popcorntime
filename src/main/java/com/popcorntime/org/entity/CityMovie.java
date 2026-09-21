package com.popcorntime.org.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor

// uk : unique key
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "uk_city_movie", columnNames = { "city_id", "movie_id" })
})
public class CityMovie {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @ManyToOne
    @JoinColumn(name = "city_id")
    private City city;

    private Date availableTillDate;
}
