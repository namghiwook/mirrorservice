package com.namghiwook.mirror.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

public class Store {

	@Id private String id;
	
	String name;
	GeoJsonPoint location;
	
	public Store(String name, double latitude, double longitude) {
		this.name = name;
		this.location = new GeoJsonPoint(latitude, longitude);
	}
	
}
