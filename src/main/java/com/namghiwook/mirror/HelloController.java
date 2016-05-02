package com.namghiwook.mirror;

import java.io.IOException;

import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.namghiwook.mirror.dust.YellowDustService;
import com.namghiwook.mirror.model.Store;
import com.namghiwook.mirror.model.StoreRepository;

@Controller
public class HelloController {

	@Autowired
	private YellowDustService yellowDustService;
	@Autowired
	private GeocodeService geocodeService;
	@Autowired
	private StoreRepository storeRepository;
	
	@RequestMapping(value = "/hello", method = RequestMethod.GET)
	public @ResponseBody String hello() {
		
		yellowDustService.loadData();
		
		return "yellowdust data loaded";
		
//		return testGeocode();
		
//		return testGeoJSON();

	}
	
	private String testGeocode() {
		try {
			Double[] latlng = geocodeService.addr2geo("제주 특별자치도 제주시 첨단로 242");
			return ArrayUtils.toString(latlng);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "hello";
	}
	
	private String testGeoJSON() {
		storeRepository.deleteAll();

		storeRepository.save(new Store("George Canning", 51.4678685, -0.0860632));
		storeRepository.save(new Store("The Cherry Tree", 51.461512, -0.078988));
		storeRepository.save(new Store("The Fox on the Hill", 51.4651705, -0.0895804));
		storeRepository.save(new Store("The Flying Pig", 51.461744, -0.070394));
		storeRepository.save(new Store("The East Dulwich Tavern", 51.460463, -0.07513));
		
		String s = "empty";
		
		GeoResults<Store> results = storeRepository.findByLocationNear(new Point(51.4634836, -0.0841914), new Distance(1, Metrics.KILOMETERS));
		if (results != null && results.getContent().size() > 0) {
			Store nearest = results.getContent().get(0).getContent();
			ObjectMapper mapper = new ObjectMapper();
			try {
				s = mapper.writeValueAsString(nearest);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		} 
		return s;
	}
	
}
