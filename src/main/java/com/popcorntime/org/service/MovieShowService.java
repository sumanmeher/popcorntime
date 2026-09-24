package com.popcorntime.org.service;

import com.popcorntime.org.dto.*;
import com.popcorntime.org.entity.*;
import com.popcorntime.org.exception.BadRequestException;
import com.popcorntime.org.exception.NotFoundException;
import com.popcorntime.org.repository.*;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.sql.Date;
import java.sql.Time;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Service
@Validated
@Slf4j
/**
 * Service class for handling business operations regarding movie shows.
 * Includes scheduling shows, checking overlaps, mapping to screens, and fetching show details.
 */
public class MovieShowService {
    @Autowired
    private ScreenRepository screenRepository;

    @Autowired
    private MovieShowRepository movieShowRepository;

    @Autowired
    private ScreenMovieRepository screenMovieRepository;
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private CityMovieRepository cityMovieRepository;
    @Autowired
    private TheatreRepository theatreRepository;
    @Autowired
    private CityRepository cityRepository;
    /**
     * Groups a list of screen time slot details by their respective dates.
     *
     * @param timeSlotDetails A list of raw time slot projections.
     * @return A map linking each date to its corresponding list of time slots.
     */
    private static Map<LocalDate, List<ScreenTimeSlotResponse>> getTimeSlotResponseMap(List<ScreenTimeSlotDetails> timeSlotDetails) {
        Map<LocalDate, List<ScreenTimeSlotResponse>> timeSlotResponseMap = new LinkedHashMap<>();
        timeSlotDetails.forEach(screenTimeSlotDetails -> {
            var date = screenTimeSlotDetails.getDate();
            List<ScreenTimeSlotResponse> list;
            ScreenTimeSlotResponse timeSlotResponse = new ScreenTimeSlotResponse();
            timeSlotResponse.setTimeSlotId(screenTimeSlotDetails.getTimeSlotId());
            timeSlotResponse.setSlotTime(java.sql.Time.valueOf(screenTimeSlotDetails.getSlotTime()));
            if (timeSlotResponseMap.containsKey(date)) {
                list = timeSlotResponseMap.get(date);
            } else {
                list = new ArrayList<>();
            }
            list.add(timeSlotResponse);
            timeSlotResponseMap.put(date, list);
        });
        return timeSlotResponseMap;
    }

    /**
     * Adds multiple movie shows to a specific screen.
     * Validates that the requested times don't overlap with existing shows based on the movie length.
     * Also links the movie to the theatre's city with an updated availability date.
     *
     * @param movieId The unique identifier of the movie.
     * @param screenId The unique identifier of the screen where shows will be added.
     * @param movieShowDetailsRequest The request payload containing the required time slots.
     * @return A success message upon successfully saving all show details.
     * @throws NotFoundException If the movie or screen cannot be found.
     * @throws BadRequestException If requested show times overlap with existing shows.
     */
    @Transactional
    public String addMovieShowsToScreen(@NotNull(message = "Invalid") Long movieId, @NotNull(message = "Invalid") Long screenId, @Valid MovieShowDetailsRequest movieShowDetailsRequest) {
        // validation
        Movie movie = movieRepository.findById(movieId).orElseThrow(() -> new NotFoundException("Movie not found for given Id."));
        Screen screen = screenRepository.findById(screenId).orElseThrow(() -> new NotFoundException("Screen not found for given Id."));
        Short movieLengthInMinutes = movie.getMovieLengthInMinutes();

        Set<Date> movieShowDates = validateMovieShowRequest(movieShowDetailsRequest, movieLengthInMinutes);
        // validate in DB if record are already existing
        List<ShowTimeDetailsResponse> existingShows = movieShowRepository.getShows(screenId, movieId, movieShowDates);
        existingShows.forEach(showTimeDetailsResponse -> {
            LocalTime showTime = showTimeDetailsResponse.getShowTime();

            List<Time> newShowTimes = movieShowDetailsRequest.getTimeSlots().get(java.sql.Date.valueOf(showTimeDetailsResponse.getDate()));
            newShowTimes.forEach(requestedShowTime -> {
                int diff = getAbsDifferenceInMinutes(showTime, requestedShowTime.toLocalTime());
                if (diff < movieLengthInMinutes) {
                    throw new BadRequestException("Requested show time is overlapping with existing for date " + showTimeDetailsResponse.getDate() + " and time " + showTime);
                }
            });
        });
        List<Show> movieShows = getShowDetails(movieShowDetailsRequest, screen, movie);
        Theatre theatre = screen.getTheatre();
        City city = theatre.getCity();
        addMovieToCity(city, movie, movieShowDetailsRequest.getTimeSlots().keySet());

        screenMovieRepository.saveAll(movieShows);
        return "Movie Show Details successfully added to Screen";
    }

    /**
     * Used to get MovieDetailsResponse
     *
     * @param movie The movie entity to map.
     * @return The corresponding {@link MovieDetailsResponse} DTO.
     */
    private MovieDetailsResponse getMovieDetailsResponse(Movie movie) {
        MovieDetailsResponse movieDetailsResponse = new MovieDetailsResponse();
        movieDetailsResponse.setId(movie.getId());
        movieDetailsResponse.setName(movie.getName());
        return movieDetailsResponse;
    }



    /**
     * Calculates the absolute time difference in minutes between two LocalTime instances.
     *
     * @param time1 The first time.
     * @param time2 The second time.
     * @return The absolute difference in minutes.
     */
    private int getAbsDifferenceInMinutes(LocalTime time1, LocalTime time2) {
        int totalMinutes = 0;
        if (time2.isAfter(time1)) {
            LocalTime temp = time1;
            time1 = time2;
            time2 = temp;
        }
        var hd = time1.getHour() - time2.getHour();
        var md = time1.getMinute() - time2.getMinute();

        if (hd != 0) {
            totalMinutes += hd * 60;
        }
        totalMinutes += md;
        return totalMinutes;
    }

    /**
     * Links a movie to a city and updates its availability date based on the latest requested show date.
     *
     * @param city The city where the movie is being shown.
     * @param movie The movie to be linked.
     * @param showRequestdates The set of dates for which shows are requested.
     */
    private void addMovieToCity(City city, Movie movie, Set<Date> showRequestdates) {
        Date lastAvalibleMovieDate = null;
        for (Date sh : showRequestdates) {
            if (lastAvalibleMovieDate == null) {
                lastAvalibleMovieDate = sh;
            } else if (sh.compareTo(lastAvalibleMovieDate) > 0) {
                lastAvalibleMovieDate = sh;
            }
        }
        Long cityId = city.getId();
        Optional<CityMovie> optionalCityMovie = movie.getCityMovieList().stream().filter(cityMovie -> cityMovie.getCity().getId().equals(cityId)).findFirst();
        CityMovie cityMovie;
        if (optionalCityMovie.isEmpty()) {
            cityMovie = new CityMovie();
            cityMovie.setMovie(movie);
            cityMovie.setCity(city);
            cityMovie.setAvailableTillDate(lastAvalibleMovieDate);
            cityMovieRepository.save(cityMovie);

            MovieDetailsResponse movieDetailsResponse = getMovieDetailsResponse(movie);
} else {
            cityMovie = optionalCityMovie.get();
            if (cityMovie.getAvailableTillDate().compareTo(lastAvalibleMovieDate) < 0) {
                cityMovie.setAvailableTillDate(lastAvalibleMovieDate);
                cityMovieRepository.save(cityMovie);
            }
        }
    }

    /**
     * Used to create Screen-Movie Link Record
     *
     * @param movieShowDetailsRequest The request payload containing time slots and prices.
     * @param screen The screen where the movie will be shown.
     * @param movie The movie being shown.
     * @return A list of {@link Show} entities representing the scheduled shows.
     */
    private List<Show> getShowDetails(MovieShowDetailsRequest movieShowDetailsRequest, Screen screen, Movie movie) {

        List<Show> movieShows = new ArrayList<>();
        movieShowDetailsRequest.getTimeSlots().forEach((date, showTimeList) ->
                showTimeList.forEach(showTime -> {
                    Show showDetails = getShowDetails(movieShowDetailsRequest, screen, movie, date, showTime);
                    movieShows.add(showDetails);
                })
        );
        return movieShows;
    }

    /**
     * Used to get Show-Details
     *
     * @param movieShowDetailsRequest The request payload containing show settings.
     * @param screen The screen assigned for this show.
     * @param movie The movie to be screened.
     * @param key The date of the show.
     * @param showTime The specific time of the show.
     * @return A configured {@link Show} entity for the given date and time.
     */
    private static Show getShowDetails(MovieShowDetailsRequest movieShowDetailsRequest, Screen screen, Movie movie, Date key, Time showTime) {
        Show showDetails = new Show();
        showDetails.setScreen(screen);
        showDetails.setMovie(movie);
        showDetails.setSeatPrice(movieShowDetailsRequest.getSeatPrice());
        showDetails.setDate(key);
        showDetails.setShowTime(showTime);
        return showDetails;
    }

    /**
     * Validates that requested time slots for a single day do not overlap, taking the movie length into consideration.
     *
     * @param movieShowDetailsRequest The request payload containing the time slots.
     * @param movieLengthInMinutes The length of the movie to check for overlap.
     * @return A set of valid dates extracted from the request.
     * @throws BadRequestException If shows overlap or if an empty list of time slots is requested.
     */
    private Set<Date> validateMovieShowRequest(MovieShowDetailsRequest movieShowDetailsRequest, Short movieLengthInMinutes) {
        Map<Date, List<Time>> timeSLotsMap = movieShowDetailsRequest.getTimeSlots();
        timeSLotsMap.forEach((key, value) -> {
            if (value.isEmpty())
                throw new BadRequestException("Invalid request to add ZERO time-slots for given Date.");

            int noOfShowPerDay = value.size();
            Time prevShowTime = value.get(0);
            for (int i = 1; i < noOfShowPerDay; i++) {
                Time currShowTime = value.get(i);
                Duration duration = Duration.between(prevShowTime.toLocalTime(), currShowTime.toLocalTime());
                if (duration.toMinutes() < movieLengthInMinutes)
                    throw new BadRequestException("Invalid Shows Requested to be added to Screen as the Movie-times are overlapping for Movie-length: " + movieLengthInMinutes + " in minutes");
                prevShowTime = currShowTime;
            }
        });
        return timeSLotsMap.keySet();
    }

    /**
     * Retrieves all upcoming shows for a specific movie across all theatres in a given city.
     *
     * @param cityId The unique identifier of the city.
     * @param movieId The unique identifier of the movie.
     * @return A map linking theatre IDs to their corresponding show details.
     * @throws NotFoundException If the city or movie cannot be found.
     */
    public Map<Long, TheatreDetailsDto> getAllShows(Long cityId, Long movieId) {
        cityRepository.findById(cityId).orElseThrow(() -> new NotFoundException("City not found for given Id."));
        movieRepository.findById(movieId).orElseThrow(() -> new NotFoundException("Movie not found for given Id."));
        List<MovieShowTheatreDetails> showDetails = movieShowRepository.getAllUpcomingShows(cityId, movieId);
        Map<Long, TheatreDetailsDto> showDetaislMap = new HashMap<>();
        showDetails.forEach(movieShowTheatreDetails -> {
            addTheatreShowDetails(movieShowTheatreDetails, showDetaislMap);
        });
        return showDetaislMap;
    }

    /**
     * Retrieves all upcoming shows for all movies currently scheduled in a specific theatre.
     *
     * @param theatreId The unique identifier of the target theatre.
     * @return A map linking movie IDs to their corresponding aggregated show details.
     * @throws NotFoundException If the theatre cannot be found.
     */
    public Map<Long, MovieDetailsDto> getAllShows(Long theatreId) {
        theatreRepository.findById(theatreId).orElseThrow(() -> new NotFoundException("Theatre not found for given Id."));
        List<MovieShowDetails> movieShowDetailsList = movieShowRepository.getAllUpcomingShows(theatreId);
        Map<Long, MovieDetailsDto> showDetailsMap = new HashMap<>();
        movieShowDetailsList.forEach(movieShowDetails -> {
            addMovieShowDetails(showDetailsMap, movieShowDetails);
        });
        return showDetailsMap;
    }

    /**
     * Used to add Movie Show Details
     *
     * @param showDetailsMap The map accumulating the aggregated movie details.
     * @param movieShowDetails The raw DB projection of show details to add.
     */
    private void addMovieShowDetails(Map<Long, MovieDetailsDto> showDetailsMap, MovieShowDetails movieShowDetails) {
        MovieDetailsDto movieDetails = showDetailsMap.getOrDefault(movieShowDetails.getMovieId(), getMovieDetails(movieShowDetails));
        ScreenDetails screenDetails = movieDetails.getScreens().getOrDefault(movieShowDetails.getScreenId(), getScreenDetails(movieShowDetails));
        List<ShowDetails> shows = screenDetails.getShows().getOrDefault(movieShowDetails.getDate(), new ArrayList<>());
        shows.add(new ShowDetails(movieShowDetails.getShowId(), java.sql.Time.valueOf(movieShowDetails.getShowTime())));
        screenDetails.getShows().put(movieShowDetails.getDate(), shows);
        movieDetails.getScreens().put(movieShowDetails.getScreenId(), screenDetails);

        showDetailsMap.put(movieShowDetails.getMovieId(), movieDetails);
    }

    private MovieDetailsDto getMovieDetails(MovieShowDetails movieShowDetails) {
        MovieDetailsDto movieDetails = new MovieDetailsDto();
        movieDetails.setMovieName(movieShowDetails.getMovieName());
        return movieDetails;
    }

    /**
     * Used to add Theatre Show Details
     *
     * @param movieShowTheatreDetails The raw DB projection of theatre show details.
     * @param showDetailsMap The map accumulating the aggregated theatre details.
     */
    private void addTheatreShowDetails(MovieShowTheatreDetails movieShowTheatreDetails, Map<Long, TheatreDetailsDto> showDetailsMap) {
        TheatreDetailsDto theatreDetails = showDetailsMap.getOrDefault(movieShowTheatreDetails.getTheatreId(), getTheatreDetails(movieShowTheatreDetails));
        ScreenDetails screenDetails = theatreDetails.getScreens().getOrDefault(movieShowTheatreDetails.getScreenId(), getScreenDetails(movieShowTheatreDetails));
        List<ShowDetails> shows = screenDetails.getShows().getOrDefault(movieShowTheatreDetails.getDate(), new ArrayList<>());
        shows.add(new ShowDetails(movieShowTheatreDetails.getShowId(), java.sql.Time.valueOf(movieShowTheatreDetails.getShowTime())));
        screenDetails.getShows().put(movieShowTheatreDetails.getDate(), shows);
        theatreDetails.getScreens().put(movieShowTheatreDetails.getScreenId(), screenDetails);
        showDetailsMap.put(movieShowTheatreDetails.getTheatreId(), theatreDetails);
    }

    /**
     * Used to get ScreenDetails with Shows
     *
     * @param movieShowTheatreDetails The raw DB projection containing screen information.
     * @return The initialized {@link ScreenDetails} DTO.
     */
    private ScreenDetails getScreenDetails(MovieShowTheatreDetails movieShowTheatreDetails) {
        ScreenDetails screenDetails = new ScreenDetails();
        screenDetails.setName(movieShowTheatreDetails.getScreenName());
        return screenDetails;
    }

    /**
     * Used to get ScreenDetails with Shows
     *
     * @param movieShowDetails The raw DB projection containing screen information.
     * @return The initialized {@link ScreenDetails} DTO.
     */
    private ScreenDetails getScreenDetails(MovieShowDetails movieShowDetails) {
        ScreenDetails screenDetails = new ScreenDetails();
        screenDetails.setName(movieShowDetails.getScreenName());
        return screenDetails;
    }

    /**
     * Used to get Theatre Details
     *
     * @param showDetaislResponse The raw DB projection containing theatre information.
     * @return The initialized {@link TheatreDetailsDto}.
     */
    private TheatreDetailsDto getTheatreDetails(MovieShowTheatreDetails showDetaislResponse) {
        TheatreDetailsDto theatreDetailsDto = new TheatreDetailsDto();
        theatreDetailsDto.setName(showDetaislResponse.getTheatreName());
        theatreDetailsDto.setAddress(showDetaislResponse.getTheatreAddress());
        return theatreDetailsDto;
    }
}
