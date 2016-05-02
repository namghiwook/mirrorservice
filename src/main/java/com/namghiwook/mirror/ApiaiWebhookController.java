package com.namghiwook.mirror;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.namghiwook.mirror.dust.YellowDustService;

@Controller
@RequestMapping("/webhook")
public class ApiaiWebhookController {

	@Autowired
	private YellowDustService dustService;
	
	@RequestMapping(method = RequestMethod.POST, produces={"application/json; charset=UTF-8"})
    public @ResponseBody WebhookResponse webhook(@RequestBody String obj){
		
		
		
        return new WebhookResponse("날씨?     거지같아!", "날씨?     거지같아!");
    }
	
}
