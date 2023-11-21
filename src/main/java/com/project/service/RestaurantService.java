package com.project.service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.maps.GeoApiContext;
import com.google.maps.PlacesApi;
import com.google.maps.model.LatLng;
import com.google.maps.model.PlaceType;
import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.PlacesSearchResult;
import com.project.controller.RestaurantController;
import com.project.domain.entity.RestaurantInfo;

@Service
public class RestaurantService {
	@Value("${googlemaps.api.key}")
	private String apiKey;

	private static final Logger logger = LoggerFactory.getLogger(RestaurantController.class);

	public List<RestaurantInfo> getRestaurants(@RequestParam double latitude, @RequestParam double longitude,
			@RequestParam String foodName) {
		List<RestaurantInfo> restaurantInfoList = new ArrayList<>();
		List<RestaurantInfo> restaurantInfoTop5 = new ArrayList<>();
		try {
			LatLng location = new LatLng(latitude, longitude);
			GeoApiContext context = new GeoApiContext.Builder().apiKey(apiKey).build();

			PlacesSearchResponse response = PlacesApi.textSearchQuery(context, foodName).location(location).radius(1000)
					.type(PlaceType.RESTAURANT).language("en").await();

			DecimalFormat df = new DecimalFormat("#.00");

			List<LatLng> destinationCoordinates = new ArrayList<>();
			for (PlacesSearchResult result : response.results) {
				destinationCoordinates.add(new LatLng(result.geometry.location.lat, result.geometry.location.lng));
			}

			for (int i = 0; i < response.results.length; i++) {
				PlacesSearchResult result = response.results[i];

				double distanceInMeters = Math.round(calManhattanDistanceInMeters(latitude, longitude,
						result.geometry.location.lat, result.geometry.location.lng) * 10.0) / 10.0; // 맨해튼 거리 구하기
				try {
					double rating = getRatingByPlaceId(context, result.placeId);
					int reviewCount = getReviewCountByPlaceId(context, result.placeId);

					if (rating >= 4.0 && reviewCount >= 60 && "OPERATIONAL".equals(result.businessStatus)) {
						restaurantInfoList.add(new RestaurantInfo(result.name, df.format(rating), reviewCount,
								distanceInMeters, result.placeId));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			Collections.sort(restaurantInfoList, Comparator.comparingDouble(RestaurantInfo::getDistanceInMeters));

		} catch (Exception e) {
			e.printStackTrace();
		}

		restaurantInfoTop5 = restaurantInfoList.subList(0, Math.min(5, restaurantInfoList.size()));
		logger.info("POST response: {}", restaurantInfoTop5); // 반환하는 음식점 리스트 log

		return restaurantInfoTop5;
	}

	private double getRatingByPlaceId(GeoApiContext context, String placeId) throws Exception {
		return PlacesApi.placeDetails(context, placeId).await().rating;
	}

	private int getReviewCountByPlaceId(GeoApiContext context, String placeId) throws Exception {
		return PlacesApi.placeDetails(context, placeId).await().userRatingsTotal;
	}

	private double calManhattanDistanceInMeters(double lat1, double lon1, double lat2, double lon2) {

		final int R = 6371; // 지구 반지름 (km)
		double dLat = Math.abs(Math.toRadians(lat2 - lat1));
		double dLon = Math.abs(Math.toRadians(lon2 - lon1));

		double distance = dLat + dLon;
		double distanceInKm = R * distance;
		return distanceInKm * 1000;
	}
}
