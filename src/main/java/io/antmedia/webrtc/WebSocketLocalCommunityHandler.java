package io.antmedia.webrtc;

import javax.websocket.server.ServerEndpoint;

import org.apache.tomcat.websocket.server.DefaultServerEndpointConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import io.antmedia.websocket.WebSocketCommunityHandler;

@ServerEndpoint(value="/websocket", configurator=DefaultServerEndpointConfigurator.class)
public class WebSocketLocalCommunityHandler extends WebSocketCommunityHandler {

	private ApplicationContext appCtx; 
	
	protected static Logger logger = LoggerFactory.getLogger(WebSocketLocalCommunityHandler.class);

	@Override
	public ApplicationContext getAppContext() {
		if (appCtx == null) {
			appCtx = WebRTCApplication.getApplication().getContext().getApplicationContext();
		}
		return appCtx;
	}
}
