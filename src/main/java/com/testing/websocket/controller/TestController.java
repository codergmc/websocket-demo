package com.testing.websocket.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
	private static final Logger LOGGER = LoggerFactory.getLogger(TestController.class);
	private SimpMessagingTemplate template;
	Thread thread;
	public TestController(SimpMessagingTemplate template) {
		this.template = template;
		thread = new Thread(new Runnable() {
			@Override
			public void run() {
				LOGGER.info(Thread.currentThread().toString());
				try {
					for (int i = 0; i < 1000_000_000; i++) {
						LocalDateTime dateTime = LocalDateTime.now();
						DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
						template.convertAndSend("/topic/chat", "{\"servertime\":\"" + dateTime.format(dtf) + "\"}");
//						if(i%1000==0) {
							LOGGER.info("Sending data in servertime: {} {}", dateTime,i);
//						}
					}
				}catch (Exception e){
					LOGGER.error("",e);
				}

			}
		},"test_thread");

	}
	
	@GetMapping("/checking")
	public String checkingAgain() {
		LOGGER.info("testAgain starting...");
		LOGGER.info("testAgain finished");
		StackTraceElement[] stackTrace = thread.getStackTrace();
		StringBuilder builder = new StringBuilder();
		if(stackTrace!=null&& stackTrace.length>0){
			for (int i = 0; i < stackTrace.length; i++) {
				builder.append(stackTrace[i].toString());
				builder.append("\r\n");
			}
		}
		return builder.toString();
	}
	
	@GetMapping("/test")
	public String testWebsocket() {
		LOGGER.info("testWebsocket starting...");
		thread.start();

		
		LOGGER.info("testWebsocket finished");
		return "OK";
	}
}
