package com.namghiwook.mirror.dust;

import org.elasticsearch.common.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController 
public class YellowDustController {

	@Autowired
	private YellowDustService yellowDustService;
	
	@RequestMapping(value = "/dust", method = RequestMethod.GET)
	public YellowDust getYellowDustByDDDCode(@RequestParam("ddd") String ddd, @RequestParam("code") String code) {
		YellowDust yellowDust = null;
		if (!StringUtils.isEmpty(ddd) && !StringUtils.isEmpty(code)) {
			yellowDust = yellowDustService.getYellowDust(ddd, code); 
		}
		if (yellowDust == null) yellowDust = new YellowDust();
		return yellowDust;
	}
	
}
