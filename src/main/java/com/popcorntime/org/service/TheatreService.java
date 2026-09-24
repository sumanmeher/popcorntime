package com.popcorntime.org.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.popcorntime.org.dto.AddTheatreRequest;
import com.popcorntime.org.dto.TheatreDetails;
import com.popcorntime.org.dto.TheatreDetailsResponse;
import com.popcorntime.org.entity.City;
import com.popcorntime.org.entity.Theatre;
import com.popcorntime.org.exception.NotFoundException;
import com.popcorntime.org.repository.CityRepository;
import com.popcorntime.org.repository.TheatreRepository;

@Service
/**
 * Service class handling theatre-related operations.
 * Includes fetching theatres by city and adding new theatres.
 */
public class TheatreService {

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private TheatreRepository theatreRepository;

    /**
     * Retrieves all theatres located in a specific city.
     * Validates that the city exists before fetching its theatres.
     *
     * @param cityId The unique identifier of the city.
     * @return A list of {@link TheatreDetailsResponse} objects representing theatres in the specified city.
     * @throws NotFoundException If the city ID does not exist.
     */
    public List<TheatreDetailsResponse> getTheatres(Long cityId) {
        cityRepository.findById(cityId).orElseThrow(() -> new NotFoundException("City not found for given cityId"));
        List<TheatreDetails> theatreDetailsList = theatreRepository.getTheatres(cityId);
        List<TheatreDetailsResponse> theatreDetailsResponses = new ArrayList<>();
        theatreDetailsList
                .forEach(theatreDetails -> theatreDetailsResponses.add(getTheatreDetailsResponse(theatreDetails)));

        return theatreDetailsResponses;
    }

    /**
     * Maps the DB projection interface {@link TheatreDetails} to a concrete {@link TheatreDetailsResponse} class.
     *
     * @param theatreDetails The DB projection representing basic theatre data.
     * @return A mapped response DTO.
     */
    private TheatreDetailsResponse getTheatreDetailsResponse(TheatreDetails theatreDetails) {
        TheatreDetailsResponse theatreDetailsResponse = new TheatreDetailsResponse();
        theatreDetailsResponse.setId(theatreDetails.getId());
        theatreDetailsResponse.setName(theatreDetails.getName());
        return theatreDetailsResponse;
    }

    /**
     * Adds a new theatre to a specific city.
     * Validates that the target city exists and constructs the theatre entity before saving.
     *
     * @param cityId The unique identifier of the city where the theatre is located.
     * @param addTheatreRequest The request payload containing the theatre's details (name, address, capacity).
     * @return A success message confirming the theatre was saved.
     */
    public String addTheatre(Long cityId, AddTheatreRequest addTheatreRequest) {
        Theatre theatre = getTheatre(cityId, addTheatreRequest);
        theatreRepository.save(theatre);
        return "Theatre details saved successfully";
    }

    /**
     * Helper method to map a request to a {@link Theatre} entity.
     * Also fetches the associated {@link City} and links it to the newly created theatre object.
     *
     * @param cityId The unique identifier of the city.
     * @param addTheatreRequest The request DTO with the theatre properties.
     * @return A fully populated {@link Theatre} entity ready to be persisted.
     * @throws NotFoundException If the specified city does not exist.
     */
    private Theatre getTheatre(Long cityId, AddTheatreRequest addTheatreRequest) {
        Theatre theatre = new Theatre();
        theatre.setName(addTheatreRequest.getName());
        theatre.setAddress(addTheatreRequest.getAddress());
        theatre.setCapacity(addTheatreRequest.getCapacity() != null ? addTheatreRequest.getCapacity() : 0);

        // Fetch the city and link it to this theatre
        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new NotFoundException("City with given Id not found."));

        theatre.setCity(city);
        return theatre;
    }

}
