package io.antmedia.webrtc;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.avutil;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.red5.logging.Red5LoggerFactory;
import org.red5.net.websocket.WebSocketConnection;
import org.red5.net.websocket.listener.WebSocketDataListener;
import org.red5.net.websocket.model.MessageType;
import org.red5.net.websocket.model.WSMessage;
import org.slf4j.Logger;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnection.IceServer;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import org.webrtc.SessionDescription.Type;

import io.antmedia.recorder.FFmpegFrameRecorder;
import io.antmedia.recorder.FrameRecorder;
import io.antmedia.webrtc.adaptor.RTMPAdaptor;



public class WebSocketListener extends WebSocketDataListener {



	private static final Logger log = Red5LoggerFactory.getLogger(WebSocketListener.class);

	private JSONParser parser = new JSONParser();

	private Map<Long, RTMPAdaptor> connectionContextList = new HashMap<>();

	private Set<WebSocketConnection> connections = new HashSet<WebSocketConnection>();

	private String baseUrl = "rtmp://127.0.0.1/WebRTCApp/";


	public void takeAction(JSONObject jsonObject, WebSocketConnection connection) {
		try {
			String cmd = (String) jsonObject.get("command");
			if (cmd.equals("publish")) {
				String streamName = (String) jsonObject.get("streamName");
				if (streamName == null || streamName.equals("")) {
					//do nothing
					JSONObject jsonResponse = new JSONObject();
					jsonResponse.put("command", "error");
					jsonResponse.put("definition", "No stream name specified");
					connection.send(jsonResponse.toJSONString());
					return;
				}
				String outputURL = "rtmp://127.0.0.1/WebRTCApp/" + streamName;

				RTMPAdaptor connectionContext = new RTMPAdaptor(getNewRecorder(outputURL));

				connectionContextList.put(connection.getId(), connectionContext);

				connectionContext.setWsConnection(connection);
				
				connectionContext.start();

				

			}
			else if (cmd.equals("play")) {

				JSONObject jsonResponse = new JSONObject();
				jsonResponse.put("command", "error");
				jsonResponse.put("definition", "play command is not supported in community edition");
				connection.send(jsonResponse.toJSONString());
			}
			else if (cmd.equals("takeConfiguration")) {
				String typeString = (String)jsonObject.get("type");
				String sdpDescription = (String)jsonObject.get("sdp");

				RTMPAdaptor connectionContext = connectionContextList.get(connection.getId());

				if (connectionContext != null) {
					SessionDescription.Type type;
					if (typeString.equals("offer")) {
						type = Type.OFFER;
						System.out.println("received sdp type is offer");
					}
					else {
						type = Type.ANSWER;
						System.out.println("received sdp type is answer");
					}
					SessionDescription sdp = new SessionDescription(type, sdpDescription);
					connectionContext.setRemoteDescription(sdp);
					
				}
			}
			else if (cmd.equals("takeCandidate")) {
				String sdpMid = (String) jsonObject.get("id");
				String sdp = (String) jsonObject.get("candidate");
				long sdpMLineIndex = (long)jsonObject.get("label");

				RTMPAdaptor connectionContext = connectionContextList.get(connection.getId());

				IceCandidate iceCandidate = new IceCandidate(sdpMid, (int)sdpMLineIndex, sdp);
				
				connectionContext.addIceCandidate(iceCandidate);
				
			}
			else if (cmd.equals("stop")) {

				RTMPAdaptor connectionContext = connectionContextList.get(connection.getId());
				if (connectionContext != null) {
					connectionContext.stop();
				}

			}
		}  catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}


	private String getBaseURL() {
		return baseUrl ;
	}
	
	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}


	@Override
	public void onWSMessage(WSMessage message) {

		String msg = new String(message.getPayload().array()).trim();
		log.info("onWSMessage: {}\n", msg);

		// ignore ping and pong
		if (message.getMessageType() == MessageType.PING || message.getMessageType() == MessageType.PONG) {
			return;
		}
		// close if we get a close
		if (message.getMessageType() == MessageType.CLOSE) {
			message.getConnection().close();
			return;
		}

		//TODO: check that expected fields are present and do not throw exception in any case of failure

		JSONObject jsonObject;
		try {
			jsonObject = (JSONObject) parser.parse(msg);
			WebSocketConnection connection = message.getConnection();
			takeAction(jsonObject, connection);
		} catch (ParseException e) {
			e.printStackTrace();
		}

	}


	public static FFmpegFrameRecorder getNewRecorder(String outputURL) {

		FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputURL, 640, 480, 1);
		recorder.setFormat("flv");
		recorder.setSampleRate(44100);
		// Set in the surface changed method
		recorder.setFrameRate(30);
		recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
		recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
		recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
		recorder.setAudioChannels(2);
		recorder.setGopSize(20);

		try {
			recorder.start();
		} catch (FrameRecorder.Exception e) {
			e.printStackTrace();
		}

		return recorder;
	}



	@Override
	public void onWSConnect(WebSocketConnection conn) {
		connections.add(conn);
	}

	@Override
	public void onWSDisconnect(WebSocketConnection conn) {
		connections.remove(conn);
		RTMPAdaptor context = connectionContextList.get(conn.getId());
		if (context != null) {
			context.stop();
		}
	}

	@Override
	public void stop() {

	}

}
