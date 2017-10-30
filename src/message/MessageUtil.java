package message;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonObject;
import javax.websocket.EncodeException;
import javax.websocket.Session;

import com.google.gson.Gson;

import blocks.Voter;

public class MessageUtil {

	public static void sendJson(Session session, Object mes) {

		synchronized (session) {
			if (session != null && session.isOpen()) {
				JsonObject jsonObject = Json.createReader(new StringReader(new Gson().toJson(mes))).readObject();

				JsonMsg sendmsg = new JsonMsg(jsonObject);

				try {

					session.getBasicRemote().sendObject(sendmsg);
				} catch (IOException | EncodeException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void notifyAll(Map<String, Voter> voters, UserOutMessage msg) {
		Set<Entry<String, Voter>> entrySet = voters.entrySet();
		synchronized (voters) {
			for (Map.Entry<String, Voter> entry : entrySet) {
				entry.getValue().sendJson(msg);
			}
		}
	}

	public static void notifyOthers(Voter voter, Map<String, Voter> voters, UserOutMessage msg) {
		Set<Entry<String, Voter>> entrySet = voters.entrySet();
		synchronized (voters) {
			for (Map.Entry<String, Voter> entry : entrySet) {
				Voter other = entry.getValue();
				if (!voter.getUid().equals(other.getUid())) {
					other.sendJson(msg);
				}
			}
		}
	}

}
