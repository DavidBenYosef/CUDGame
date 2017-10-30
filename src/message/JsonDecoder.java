package message;

import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

public class JsonDecoder implements Decoder.Text<JsonMsg>{

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init(EndpointConfig arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public JsonMsg decode(String string) throws DecodeException {
		JsonObject jsonObject = Json.createReader(new StringReader(string)).readObject();
        return  new JsonMsg(jsonObject);
	}

	@Override
	public boolean willDecode(String string) {
		 try {
	            Json.createReader(new StringReader(string)).readObject();
	            return true;
	        } catch (JsonException ex) {
	            ex.printStackTrace();
	            return false;
	        }
	}

}
