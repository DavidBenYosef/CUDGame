package endpoints;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.json.JsonObject;
import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import javax.websocket.CloseReason.CloseCodes;

import com.google.gson.Gson;

import game.SessionHandler;
import game.Util;
import message.JsonDecoder;
import message.JsonEncoder;
import message.JsonMsg;
import message.MessageUtil;
import message.UserInMessage;
import message.UserOutMessage;

@ClientEndpoint(encoders = { JsonEncoder.class }, decoders = { JsonDecoder.class })
public class AgentEndpoint {

	// Session userSession = null;

	private int votersNum;
	//private int timePerRound;

	private String name;

	public AgentEndpoint(String name) {
		this.name = name;
	}

	public void connect(URI endpointURI) {
		try {
			WebSocketContainer container = ContainerProvider.getWebSocketContainer();
			container.connectToServer(this, endpointURI);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@OnOpen
	public void onOpen(Session userSession) {
		// System.out.println("opening websocket");
		// this.userSession = userSession;
	}

	@OnClose
	public void onClose(Session userSession, CloseReason reason) {
		// System.out.println("closing websocket");
		// this.userSession = null;
	}

	@OnMessage
	public void onMessage(Session session, JsonMsg msg) {
		// System.out.println("Message for "+name+": "+msg);
		JsonObject json = msg.getJson();
		UserOutMessage mes = new Gson().fromJson(json.toString(), UserOutMessage.class);

		switch (mes.getType()) {
		case "NEW_GAME_INFO":
			//timePerRound = mes.getTimePerRound();
			votersNum = mes.getTotalVoters();

			break;
		case "VOTERS_INFO":
			votersNum = mes.getVoters();

			break;
		case "NEW_ROUND":
			processStep(session, mes);
			break;
		case "DECIDED":
		case "DEADLINE":
//			try {
//				session.close(new CloseReason(CloseCodes.NORMAL_CLOSURE, mes.getType()));
//			} catch (IOException e) {
//			}
			break;
		default:
			break;
		}

		// if (this.messageHandler != null) {
		// this.messageHandler.handleMessage(session,msg);
		// }
	}

	private void processStep(Session session, UserOutMessage mes) {
		

		int[] orderedCand = mes.getOrderedCands();

		Integer currentSelection = mes.getCurrentSelection();

		Map<Integer, Integer> results = new HashMap<>(mes.getResults());

		// System.out.println(name+": orderedCand:
		// "+Arrays.toString(orderedCand)+" currentSelection: " +
		// currentSelection+" RESULTS: "+results);

		int round = mes.getRound();
		// what if
		// resultCopy.put(currentSelection, resultCopy.get(currentSelection)+1);

		// pwf if we dont change
		// List<Integer> pwf = Util.generatePWF(results, mes.getRound(),
		// voters);

		Integer nextSelection = null;
		
		boolean isCurrentPWWithNoChange = Util.isPW(results.get(currentSelection), round - 1, votersNum);

		if (!isCurrentPWWithNoChange)
		{
			for (int cand : orderedCand) {
				if (!currentSelection.equals(cand)) {
					boolean willBePW = Util.isPW(results.get(cand) + 1, round - 1, votersNum);
					if (willBePW) {
						nextSelection = cand;
						break;
					}
				}
			}
		}
		
		
//		System.out.println(name + ": round: " + round + " orderedCand: " + Arrays.toString(orderedCand) + " selected: "
//				+ currentSelection + " ,results: " + results + " nextSelection: " + nextSelection);
		UserInMessage inMsg = new UserInMessage("SELECT", round, nextSelection);

		// try {
		// Thread.sleep(timePerRound*1000);
		// } catch (InterruptedException e) {
		//
		// e.printStackTrace();
		// }

		MessageUtil.sendJson(session, inMsg);
	}

	// public void addMessageHandler(MessageHandler msgHandler) {
	// this.messageHandler = msgHandler;
	// }

	// public void sendMessage(String message) {
	// this.userSession.getAsyncRemote().sendText(message);
	// }

	// public static interface MessageHandler {
	//
	// public void handleMessage(Session session,JsonMsg msg);
	// }
}