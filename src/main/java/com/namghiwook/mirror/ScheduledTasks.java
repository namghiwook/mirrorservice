package com.namghiwook.mirror;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.namghiwook.mirror.dust.YellowDustService;

@Component
public class ScheduledTasks {

	@Autowired
	private YellowDustService yellowDustService;
	
	@Scheduled(fixedRate = 60*1000)
	public void reportCurrentTime() {
		
		System.out.println("tick");
		
		if (yellowDustService != null) {
			yellowDustService.loadData();
		}
	}
}
