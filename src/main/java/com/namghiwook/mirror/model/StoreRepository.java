package com.namghiwook.mirror.model;

import java.util.List;

import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.geo.Polygon;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface StoreRepository extends MongoRepository<Store, String> {

	List<Store> findByLocationWithin(Polygon polygon);
	GeoResults<Store> findByLocationNear(Point location, Distance distance);
	
}
