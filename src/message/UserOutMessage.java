package message;

import java.util.Map;

import blocks.GameDetails;
import blocks.Voter;

public class UserOutMessage {

	private String text;
	private String type;
	private int[] orderedCands;	
	private Integer currentSelection;
	private Integer round;
	private Integer totalRounds;
	private Map<Integer, Integer> results;
	private Integer voters;
	private Integer totalVoters;
	private Integer gameId;
	private Integer timePerRound;
	private Boolean isIntro;

	
	
	
	//On voter first connect - get pending game details
	public UserOutMessage(GameDetails nextGame)
	{		
		this.type = "NEW_GAME_INFO";
		this.text = "{GAME_START}";
		this.totalVoters = nextGame.getVoters();
		this.gameId = nextGame.getId();
		this.timePerRound = nextGame.getTimePerRound();
		this.totalRounds = nextGame.getRounds();
		this.isIntro =nextGame.isIntro();
	}
	
	//On other voter connect / disconnect - update number of voters
	public UserOutMessage(Integer voters)
	{		
		this.type="VOTERS_INFO";
		this.voters = voters;
	}
//	public UserOutMessage(String type,String text) {
//		this.type = type;
//		this.text = text;
//	}
	
	public UserOutMessage(String text) {
		this.type = "INFO";
		this.text = text;
	}
	


//send round data
	public UserOutMessage(String type, Voter voter, String text, Integer round, Map<Integer, Integer> results) {
		this.type = type;
		this.orderedCands=voter.getOrderedCands();
		this.currentSelection = voter.getSelectedCand();
		this.round = round;
		this.results = results;
		this.text = text;
	}

	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}

	public int[] getOrderedCands() {
		return orderedCands;
	}

	public void setOrderedCands(int[] orderedCands) {
		this.orderedCands = orderedCands;
	}

	public int getRound() {
		return round;
	}

	public void setRound(Integer round) {
		this.round = round;
	}

	public Map<Integer, Integer> getResults() {
		return results;
	}

	public void setResults(Map<Integer, Integer> results) {
		this.results = results;
	}

	public Integer getVoters() {
		return voters;
	}

	public void setVoters(Integer voters) {
		this.voters = voters;
	}

	public Integer getTotalVoters() {
		return totalVoters;
	}

	public void setTotalVoters(Integer totalVoters) {
		this.totalVoters = totalVoters;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Integer getCurrentSelection() {
		return currentSelection;
	}
	public void setCurrentSelection(Integer currentSelection) {
		this.currentSelection = currentSelection;
	}
	public Integer getGameId() {
		return gameId;
	}
	public void setGameId(Integer gameId) {
		this.gameId = gameId;
	}
	public Integer getTimePerRound() {
		return timePerRound;
	}
	public void setTimePerRound(Integer timePerRound) {
		this.timePerRound = timePerRound;
	}

//	public String getMyscore() {
//		return myscore;
//	}
//
//	public void setMyscore(String myscore) {
//		this.myscore = myscore;
//	}

	public Integer getTotalRounds() {
		return totalRounds;
	}

	public void setTotalRounds(Integer totalRounds) {
		this.totalRounds = totalRounds;
	}

	public Boolean getIsIntro() {
		return isIntro;
	}

	public void setIsIntro(Boolean isIntro) {
		this.isIntro = isIntro;
	}

}
