package com.project.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.project.domain.entity.RestaurantInfo;
import com.project.domain.entity.RestaurantSearchRequest;
import com.project.service.RestaurantService;


@RestController
public class RestaurantController {
	
	private static final Logger logger = LoggerFactory.getLogger(RestaurantController.class);
	
	@Autowired
    private RestaurantService findRestaurantService;

    @PostMapping("/restaurants")
    public List<RestaurantInfo> getRestaurants(@RequestBody RestaurantSearchRequest request) {
    	logger.info("POST request: {}", request);//요청 정보 log
        return findRestaurantService.getRestaurants(request.getLatitude(), request.getLongitude(), request.getFoodName());    
    }
}
