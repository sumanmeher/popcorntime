package com.popcorntime.org.controller;

import com.popcorntime.org.dto.MovieShowDetailsRequest;
import com.popcorntime.org.service.MovieShowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/v1")
public class MovieShowController {
    @Autowired
    private MovieShowService movieShowService;

    /**
     * Retrieves all scheduled shows for a specific movie in a given city.
     *
     * @param cityId The unique identifier of the city.
     * @param movieId The unique identifier of the movie.
     * @return A list of theatre details and their respective show times.
     */
    @PreAuthorize("hasAuthority('USER') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    @GetMapping(value = "/cities/theatres/movies/shows", params = {"cityId", "movieId"})
    public ResponseEntity<Object> getAllShowsByCityAndMovie(@RequestParam("cityId") final Long cityId,
                                                            @RequestParam("movieId") final Long movieId) {
        return ResponseEntity.ok().body(movieShowService.getAllShows(cityId, movieId));
    }

    /**
     * Retrieves all scheduled shows across all movies for a given theatre.
     *
     * @param theatreId The unique identifier of the theatre.
     * @return A list of movies and their respective show times at the specified theatre.
     */
    @PreAuthorize("hasAuthority('USER') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    @GetMapping(value = "/cities/theatres/movies/shows", params = "theatreId")
    public ResponseEntity<Object> getAllShowsByTheatre(@RequestParam("theatreId") final Long theatreId) {
        return ResponseEntity.ok().body(movieShowService.getAllShows(theatreId));
    }

    /**
     * Schedules new movie shows for a specific screen and movie.
     *
     * @param movieId The unique identifier of the movie.
     * @param screenId The unique identifier of the screen.
     * @param movieShowDetailsRequest The request body containing dates, times, and pricing.
     * @return A success message confirming the shows were scheduled.
     */
    @PreAuthorize("hasAuthority('MANAGER')")
    @PostMapping("/cities/theatres/movies/screens/shows")
    public ResponseEntity<Object> addMovieShowsToScreen(@RequestParam("movieId") final Long movieId,
                                                        @RequestParam("screenId") final Long screenId,
                                                        @RequestBody MovieShowDetailsRequest movieShowDetailsRequest) {
        return ResponseEntity.ok().body(movieShowService.addMovieShowsToScreen(movieId, screenId, movieShowDetailsRequest));
    }
}
