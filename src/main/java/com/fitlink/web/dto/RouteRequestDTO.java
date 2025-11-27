package com.fitlink.web.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RouteRequestDTO {
    private float originLat;
    private float originLng;
    private float destLat;
    private float destLng;
    private String type; // walk, car, transit
}