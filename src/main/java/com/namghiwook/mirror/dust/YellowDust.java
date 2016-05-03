package com.namghiwook.mirror.dust;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

public class YellowDust {

	public static final int KMA = 0;
	public static final int SEOUL = 1;
	public static final int GYEONGGI = 2;
	
	@Id public String id;
	
	public int source;
	public String ddd;
	public String code;
	public String label;
	public int density;
	public Date lastUpdated;
	
	public GeoJsonPoint location;
	
	public String toString() {
		return label + " " + density + "㎍/㎥";
	}
	
	public int getDensityValue() {
		return density;
	}
	
}
