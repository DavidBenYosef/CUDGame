package message;

public class AdminOutMessage {


	private String text;
	private Boolean gameActiveFlag;
	private Integer voters;
	private Integer rounds;
	private Integer timePerRound;
	private String candsSet;
	private Integer gamesLimit;
	private Integer scoreLimit;
	private Integer introGames;
	private Integer agentTimer;

	public AdminOutMessage (String text)
	{
		this.text = text;	
	}
	
	public AdminOutMessage (boolean gameActiveFlag,int voters,int rounds,int timePerRound,String candsSet,Integer gamesLimit,Integer scoreLimit,Integer introGames,Integer agentTimer)
	{
		this.gameActiveFlag = gameActiveFlag;
		this.voters = voters;	
		this.rounds = rounds;	
		this.timePerRound = timePerRound;	
		this.candsSet = candsSet;	
		this.gamesLimit = gamesLimit;	
		this.scoreLimit = scoreLimit;
		this.introGames = introGames;
		this.agentTimer = agentTimer;
	}
	

	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}


	public Boolean isGameActiveFlag() {
		return gameActiveFlag;
	}


	public void setGameActiveFlag(Boolean gameActiveFlag) {
		this.gameActiveFlag = gameActiveFlag;
	}


	public Integer getVoters() {
		return voters;
	}


	public void setVoters(Integer voters) {
		this.voters = voters;
	}


	public Integer getRounds() {
		return rounds;
	}


	public void setRounds(Integer rounds) {
		this.rounds = rounds;
	}


	public Integer getTimePerRound() {
		return timePerRound;
	}


	public void setTimePerRound(Integer timePerRound) {
		this.timePerRound = timePerRound;
	}


	public String getCandsSet() {
		return candsSet;
	}


	public void setCandsSet(String candsSet) {
		this.candsSet = candsSet;
	}


	public Integer getGamesLimit() {
		return gamesLimit;
	}


	public void setGamesLimit(Integer gamesLimit) {
		this.gamesLimit = gamesLimit;
	}


	public Integer getScoreLimit() {
		return scoreLimit;
	}


	public void setScoreLimit(Integer scoreLimit) {
		this.scoreLimit = scoreLimit;
	}

	public Integer getIntroGames() {
		return introGames;
	}

	public void setIntroGames(Integer introGames) {
		this.introGames = introGames;
	}

	public Integer getAgentTimer() {
		return agentTimer;
	}

	public void setAgentTimer(Integer agentTimer) {
		this.agentTimer = agentTimer;
	}

}
