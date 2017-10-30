package game;

import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import endpoints.UserEndpoint;

public class AgentCreator {
    Timer timer;
    private final Logger logger = LoggerFactory.getLogger(UserEndpoint.class);
    public AgentCreator(int time) {
        timer = new Timer();
        timer.schedule(new CreateTask(), time*1000);
        logger.info("AgentCreator timer started for "+time+" seconds");
	}

    class CreateTask extends TimerTask {

		public void run() {
			String agentId = SessionHandler.getInstance().createAgent();
			logger.info("RoundChecker created new Agent: {}",agentId);		
        }
    }
}