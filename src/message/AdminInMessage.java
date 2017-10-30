package message;

public class AdminInMessage {

	private String type;
	private Integer voters;
	private Integer rounds;
	private Integer timePerRound;
	private String candsSet;
	private Boolean flag;
	private String text;
	private Integer gamesLimit;
	private Integer scoreLimit;
	private Integer introGames;
	private Integer agentsNum;
	private Integer agentTimer;

//	public AdminInMessage (String type, String text)
//	{
//		this.type = type;
//		this.text = text;
//	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}


	public Integer getRounds() {
		return rounds;
	}

	public void setRounds(Integer rounds) {
		this.rounds = rounds;
	}

	public String getCandsSet() {
		return candsSet;
	}

	public void setCandsSet(String candsSet) {
		this.candsSet = candsSet;
	}
	public Integer getVoters() {
		return voters;
	}
	public void setVoters(Integer voters) {
		this.voters = voters;
	}
	public Integer getTimePerRound() {
		return timePerRound;
	}
	public void setTimePerRound(Integer timePerRound) {
		this.timePerRound = timePerRound;
	}
	public Boolean getFlag() {
		return flag;
	}
	public void setFlag(Boolean flag) {
		this.flag = flag;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
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
	public Integer getAgentsNum() {
		return agentsNum;
	}
	public void setAgentsNum(Integer agentsNum) {
		this.agentsNum = agentsNum;
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
