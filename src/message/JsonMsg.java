package message;

import java.io.StringWriter;

import javax.json.Json;
import javax.json.JsonObject;

public class JsonMsg {
	private JsonObject json;
	
	public JsonMsg(JsonObject json) {
        this.json = json;
    }
	  @Override
	    public String toString() {
	        StringWriter writer = new StringWriter();
	        Json.createWriter(writer).write(json);
	        return writer.toString();
	    }
	public JsonObject getJson() {
		return json;
	}
	public void setJson(JsonObject json) {
		this.json = json;
	}
}
