package com.popcorntime.org.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.popcorntime.org.dto.MovieRequest;
import com.popcorntime.org.service.MovieService;

@RestController
@RequestMapping(value = "/v1")
public class MovieController {
    @Autowired
    private MovieService movieService;

    /**
     * Retrieves a list of all movies available in a specified city.
     *
     * @param cityId The unique identifier of the city.
     * @return A list of movies available in the city.
     */
    @PreAuthorize("hasAuthority('USER') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    @GetMapping(value = "/cities/movies")
    public ResponseEntity<Object> getMovies(@RequestParam("cityId") final Long cityId) {
        return ResponseEntity.ok().body(movieService.getMovies(cityId));
    }

    /**
     * Adds a new movie to the system.
     *
     * @param movieRequest The request body containing movie details.
     * @return A success message confirming the addition of the movie.
     */
    @PreAuthorize("hasAuthority('MANAGER')")
    @PostMapping(value = "/movies")
    public ResponseEntity<Object> addMovie(@RequestBody MovieRequest movieRequest) {
        return ResponseEntity.ok().body(movieService.addMovie(movieRequest));
    }
}
