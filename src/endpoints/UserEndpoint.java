package endpoints;

import javax.json.JsonObject;
import javax.websocket.CloseReason;
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
import message.JsonDecoder;
import message.JsonEncoder;
import message.JsonMsg;
import message.UserInMessage;


@ServerEndpoint(value="/User/{uid}/{name}",encoders = {JsonEncoder.class}, decoders = {JsonDecoder.class})
public class UserEndpoint{


    private SessionHandler sessionHandler = SessionHandler.getInstance();
	
    private final Logger logger = LoggerFactory.getLogger(UserEndpoint.class);
	@OnOpen
	public void onOpen(Session session, EndpointConfig endpoint,@PathParam("uid") String uid,@PathParam("name") String name)  {	

		logger.info("User {} with id {} connected",name,uid);
		sessionHandler.addSession(session, uid,name);
		
	}

	 @OnClose
	 public void onClose(Session session,CloseReason closeReason,@PathParam("uid") String uid)
	 {
		 logger.info("User id {} disconnected with close reason {}",uid,closeReason);
		 sessionHandler.removeSession(session,uid);	
	 }

	
	@OnMessage
	public void onMessage(Session session, JsonMsg msg,@PathParam("uid") String uid,@PathParam("name") String name) {
		
		logger.info("Incoming message from user id {} : {}",uid,msg);
		JsonObject json = msg.getJson();  							
		UserInMessage mes = new Gson().fromJson(json.toString(), UserInMessage.class); 
		sessionHandler.handleInMsg(session,uid,name,mes);

	}
	
	

}