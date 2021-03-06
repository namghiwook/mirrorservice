package com.namghiwook.mirror.dust;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

@RestController 
public class YellowDustController {
	
	Logger logger = Logger.getLogger(getClass());
	
	OkHttpClient client = new OkHttpClient();
	ObjectMapper mapper = new ObjectMapper();
	
	@RequestMapping(value = "/dust", method = RequestMethod.GET)
	public String getYellowDustByCode(@RequestParam("code") String code) {

		int density = 0;
		
		// http://dweet.io/get/latest/dweet/for/yellowdust-031-040
		Request request = new Request.Builder().url("http://dweet.io/get/latest/dweet/for/yellowdust-" + code).build();
		try {
			Response response = client.newCall(request).execute();
			if (response.code() == 200) {
				// {"this":"succeeded","by":"getting","the":"dweets","with":[{"thing":"yellowdust-031-040","created":"2016-05-04T05:15:52.993Z","content":{"density":74}}]}
				// with/content/density
				String json = response.body().string();
				logger.info(String.format("getYellowDustByCode json : %s", json));
				JsonNode root = mapper.readTree(json);
				JsonNode thisNode = root.path("this");
				if (!thisNode.isMissingNode() && thisNode.asText("failed").equals("succeeded")) {
					JsonNode densityNode = root.path("with").get(0).path("content").path("density");
					if (densityNode.isMissingNode()) { // true if no such path exists
						logger.error("density node missing");
					} else {
						density = densityNode.intValue();
					}
				} 
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		logger.info(String.format("getYellowDustByCode code %s density : %d", code, density));
		
		return String.valueOf(density);
	}
	
}
