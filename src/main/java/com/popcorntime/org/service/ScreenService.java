package com.popcorntime.org.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.popcorntime.org.dto.AddScreenRequest;
import com.popcorntime.org.dto.ScreenDetailsDto;
import com.popcorntime.org.entity.Screen;
import com.popcorntime.org.entity.Theatre;
import com.popcorntime.org.exception.BadRequestException;
import com.popcorntime.org.exception.NotFoundException;
import com.popcorntime.org.repository.ScreenRepository;
import com.popcorntime.org.repository.TheatreRepository;

@Service
/**
 * Service class for managing screens within a theatre.
 * Provides operations to retrieve existing screens and add new screens.
 */
public class ScreenService {

    @Autowired
    private ScreenRepository screenRepository;
    @Autowired
    private TheatreRepository theatreRepository;

    /**
     * Retrieves all screens associated with a specific theatre.
     * Validates that the theatre exists before fetching its screens.
     *
     * @param theatreId The unique identifier of the theatre.
     * @return A list of {@link ScreenDetailsDto} representing the screens in the theatre.
     * @throws NotFoundException If the theatre ID does not exist.
     */
    public List<ScreenDetailsDto> getScreens(Long theatreId) {
        theatreRepository.findById(theatreId)
                .orElseThrow(() -> new NotFoundException("Theatre not found for given Id."));
        List<Screen> screens = screenRepository.findAllByTheatreId(theatreId);
        return screens.stream().map(this::getScreenDetails).toList();
    }

    /**
     * Maps a {@link Screen} entity to a {@link ScreenDetailsDto} object.
     *
     * @param screen The screen entity from the database.
     * @return A mapped DTO containing the screen's basic details.
     */
    private ScreenDetailsDto getScreenDetails(Screen screen) {
        ScreenDetailsDto screenDetails = new ScreenDetailsDto();
        screenDetails.setId(screen.getId());
        screenDetails.setName(screen.getName());
        return screenDetails;
    }

    /**
     * Adds multiple new screens to an existing theatre.
     * Validates that the theatre exists and that none of the requested screen names are duplicates within that theatre.
     *
     * @param theatreId The unique identifier of the target theatre.
     * @param screenRequests A list of requests containing the details for the new screens to add.
     * @return A success message upon successfully saving all screens.
     * @throws NotFoundException If the target theatre is not found.
     * @throws BadRequestException If any of the requested screen names already exist in the given theatre.
     */
    public String addScreens(Long theatreId, List<AddScreenRequest> screenRequests) {
        Theatre theatre = theatreRepository.findById(theatreId)
                .orElseThrow(() -> new NotFoundException("Theatre not found with given Id."));
        List<String> screenNames = screenRequests.stream().map(AddScreenRequest::getName).toList();
        List<Screen> existScreens = screenRepository.findAllByTheatreAndNameIn(theatre, screenNames);
        if (!existScreens.isEmpty()) {
            throw new BadRequestException("Some of the requested Screens to be added already exist in the Theatre");
        }
        List<Screen> screens = new ArrayList<>();
        screenRequests.forEach(addScreenRequest -> {
            Screen screen = new Screen();
            screen.setName(addScreenRequest.getName());
            screen.setTheatre(theatre);
            screens.add(screen);
        });
        screenRepository.saveAll(screens);
        return "Screens Added successfully.";
    }

}
