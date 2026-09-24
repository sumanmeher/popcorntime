package com.popcorntime.org.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.popcorntime.org.dto.MovieDetailsResponse;
import com.popcorntime.org.dto.MovieRequest;
import com.popcorntime.org.dto.MovieResponse;
import com.popcorntime.org.entity.Movie;
import com.popcorntime.org.exception.NotFoundException;
import com.popcorntime.org.repository.CityMovieRepository;
import com.popcorntime.org.repository.CityRepository;
import com.popcorntime.org.repository.MovieRepository;

@Service
/**
 * Service class responsible for managing movie-related business logic.
 * Handles operations such as retrieving movies for a city and adding new movies.
 */
public class MovieService {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private CityMovieRepository cityMovieRepository;

    /**
     * Retrieves a list of movies available in a specific city.
     * Validates the city's existence before fetching the movies.
     *
     * @param cityId The unique identifier of the city.
     * @return A list of {@link MovieDetailsResponse} containing basic movie details for the specified city.
     * @throws NotFoundException If the provided city ID does not exist.
     */
    public List<MovieDetailsResponse> getMovies(Long cityId) {
        cityRepository.findById(cityId).orElseThrow(() -> new NotFoundException("City not found for given cityId"));
        List<MovieResponse> movieDetails = cityMovieRepository.getMovies(cityId);
        List<MovieDetailsResponse> movieDetailsResponses = movieDetails.stream().map(this::getMovieDetailsResponse)
                .toList();
        return movieDetailsResponses;
    }

    /**
     * Maps a {@link MovieResponse} DB projection to a {@link MovieDetailsResponse} DTO.
     *
     * @param movieResponse The internal movie response projection containing basic movie data.
     * @return A mapped {@link MovieDetailsResponse} object meant for API consumers.
     */
    private MovieDetailsResponse getMovieDetailsResponse(MovieResponse movieResponse) {
        MovieDetailsResponse movieDetailsResponse = new MovieDetailsResponse();
        movieDetailsResponse.setId(movieResponse.getId());
        movieDetailsResponse.setName(movieResponse.getName());
        return movieDetailsResponse;
    }

    /**
     * Adds a new movie to the system database.
     *
     * @param movieRequest The request DTO containing the details of the new movie.
     * @return A success message indicating the movie was added.
     */
    public String addMovie(MovieRequest movieRequest) {
        Movie movie = getMovie(movieRequest);
        movieRepository.save(movie);
        return "Movie Added Successfully.";
    }

    /**
     * Maps a {@link MovieRequest} DTO to a {@link Movie} entity for database persistence.
     * Defaults the movie length to 120 minutes if not provided.
     *
     * @param movieRequest The incoming movie request DTO.
     * @return A populated {@link Movie} entity ready to be saved.
     */
    private Movie getMovie(MovieRequest movieRequest) {
        Movie movie = new Movie();
        movie.setName(movieRequest.getName());
        movie.setMovieLengthInMinutes(movieRequest.getMovieLengthInMinutes() != null ? movieRequest.getMovieLengthInMinutes() : 120);
        return movie;
    }

}
