package game;

import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoundChecker {
    Timer timer;
    private final Logger logger = LoggerFactory.getLogger(RoundChecker.class);
    public RoundChecker(CudGame game) {
        timer = new Timer();
        int time = game.gameDetails.getTimePerRound()*1000+10000;
        timer.schedule(new CheckTask(game), time);
        logger.info("RoundChecker timer started for "+time/1000+" seconds");
	}

    class CheckTask extends TimerTask {
       CudGame game;
    	
    	public CheckTask(CudGame game) {
    		this.game=game;
		}

		public void run() {
			logger.info("RoundChecker found stuck game: "+game.gameDetails.getId());
			game.forceNextRound();		
        }
    }
}