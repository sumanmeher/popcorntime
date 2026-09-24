package com.popcorntime.org.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.popcorntime.org.dto.AddScreenRequest;
import com.popcorntime.org.service.ScreenService;

@RestController
@RequestMapping(value = "/v1")
public class ScreenController {
    @Autowired
    private ScreenService screenService;

    /**
     * Retrieves a list of all screens belonging to a specific theatre.
     *
     * @param theatreId The unique identifier of the theatre.
     * @return A list of screen details.
     */
    @PreAuthorize("hasAuthority('MANAGER')")
    @GetMapping("/cities/theatres/screens")
    public ResponseEntity<Object> getScreens(@RequestParam(value = "theatreId") final Long theatreId) {
        return ResponseEntity.ok().body(screenService.getScreens(theatreId));
    }

    /**
     * Adds multiple new screens to a given theatre.
     *
     * @param theatreId The unique identifier of the target theatre.
     * @param screenRequests A list of requests containing the new screen names.
     * @return A success message confirming the addition of the screens.
     */
    @PreAuthorize("hasAuthority('MANAGER')")
    @PostMapping("/cities/theatres/screens")
    public ResponseEntity<Object> addScreens(@RequestParam(value = "theatreId") final Long theatreId,
            @RequestBody List<AddScreenRequest> screenRequests) {
        return ResponseEntity.ok().body(screenService.addScreens(theatreId, screenRequests));
    }
}
