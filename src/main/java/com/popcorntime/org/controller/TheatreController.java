package com.popcorntime.org.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.popcorntime.org.dto.AddTheatreRequest;
import com.popcorntime.org.service.TheatreService;

@RestController
@RequestMapping("/v1")
public class TheatreController {
    @Autowired
    private TheatreService theatreService;

    /**
     * Retrieves a list of all theatres located in a specific city.
     *
     * @param cityId The unique identifier of the city.
     * @return A list of theatres available in the city.
     */
    @PreAuthorize("hasAuthority('USER') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    @GetMapping("/cities/theatres")
    public ResponseEntity<Object> getTheatres(@RequestParam("cityId") final Long cityId) {
        return ResponseEntity.ok().body(theatreService.getTheatres(cityId));
    }

    /**
     * Adds a new theatre to a specified city.
     *
     * @param cityId The unique identifier of the city.
     * @param addTheatreRequest The request body containing the theatre details.
     * @return A success message confirming the theatre was added.
     */
    @PreAuthorize("hasAuthority('MANAGER')")
    @PostMapping("/cities/theatres/addTheatre")
    public ResponseEntity<Object> addTheatre(@RequestParam(name = "cityId") final Long cityId,
            @RequestBody AddTheatreRequest addTheatreRequest) {
        return ResponseEntity.ok().body(theatreService.addTheatre(cityId, addTheatreRequest));
    }

}
