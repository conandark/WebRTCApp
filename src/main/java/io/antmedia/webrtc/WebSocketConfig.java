package io.antmedia.webrtc;

import java.util.HashSet;
import java.util.Set;

import javax.websocket.Endpoint;
import javax.websocket.server.ServerApplicationConfig;
import javax.websocket.server.ServerEndpointConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketConfig implements ServerApplicationConfig {

	private static Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);
	
	@Override
	public Set<ServerEndpointConfig> getEndpointConfigs(Set<Class<? extends Endpoint>> endpointClasses) {
		return new HashSet<>();
	}

	@Override
	public Set<Class<?>> getAnnotatedEndpointClasses(Set<Class<?>> scanned) {
		Set<Class<?>> results = new HashSet<>();
		
		for (Class<?> clazz : scanned) {
			
			if (clazz.isAssignableFrom(WebSocketLocalCommunityHandler.class)) 
			{
				logger.info("Adding websocket endpoint {}" ,clazz.getName());
				results.add(clazz);
			}
		}
		return results;
	}

}
