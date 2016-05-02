package com.namghiwook.mirror;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.namghiwook.mirror.dust.YellowDustRepository;
import com.namghiwook.mirror.dust.YellowDustService;

@Component
public class ApplicationStartup {

	private Logger logger = Logger.getLogger(getClass());

	@EventListener
    public void handleContextRefresh(ContextRefreshedEvent event) {
		logger.info("handleContextRefresh called!");
		
    }
	
}
