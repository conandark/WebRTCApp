package io.antmedia.webrtc;

import org.red5.logging.Red5LoggerFactory;
import org.red5.server.api.scope.IScope;
import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import io.antmedia.AntMediaApplicationAdapter;

public class WebRTCApplication extends AntMediaApplicationAdapter implements ApplicationContextAware{

	static WebRTCApplication application;

	private static final Logger logger = Red5LoggerFactory.getLogger(WebRTCApplication.class);

	private ApplicationContext applicationContext;
	private ThreadPoolTaskScheduler taskScheduler;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
	
	public static WebRTCApplication getApplication() {
		return application;
	}
	

	@Override
	public boolean appStart(IScope app) {
		application = this;
		// get the websocket plugin
	
		return super.appStart(app);
	}
	
	public ThreadPoolTaskScheduler getTaskScheduler() {
		return taskScheduler;
	}
	public void setTaskScheduler(ThreadPoolTaskScheduler taskScheduler) {
		this.taskScheduler = taskScheduler;
	}

	@Override
    public void appStop(IScope scope) {
        super.appStop(scope);
    }

}
