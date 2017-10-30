package endpoints;

import java.io.IOException;

import javax.json.JsonObject;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import game.SessionHandler;
import message.AdminInMessage;
import message.JsonDecoder;
import message.JsonEncoder;
import message.JsonMsg;

@ServerEndpoint(value = "/Admin/{pass}", encoders = { JsonEncoder.class }, decoders = { JsonDecoder.class })
public class AdminEndpoint {

	SessionHandler sessionHandler = SessionHandler.getInstance();
	private final Logger logger = LoggerFactory.getLogger(AdminEndpoint.class);

	@OnOpen
	public void onOpen(Session session, EndpointConfig endpoint, @PathParam("pass") String pass) {

		logger.info("Incoming Admin connection with pass {}", pass);

		if (pass.equals("David")) {
			sessionHandler.setAdmin(session);

		} else {
			try {
				session.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@OnClose
	public void onClose(Session session) {
		logger.info("Admin disconnected");
	}

	@OnMessage
	public void onMessage(Session session, JsonMsg msg) {

		logger.info("Incoming Admin messgae: {}", msg);
		JsonObject json = msg.getJson();
		AdminInMessage mes = new Gson().fromJson(json.toString(), AdminInMessage.class);
		sessionHandler.adminMsg(session, mes);

	}

}