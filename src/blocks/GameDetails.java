package blocks;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class GameDetails {
			
	private Integer id;
	private int voters;
	private int rounds;
	private int timePerRound;
	private String prefSet;
	private Integer candsNum;
	private Integer convTime;
	private int actualVoters;
	private int winner;
	private AtomicInteger voteChanges=new AtomicInteger();
	private Integer agentsNum;
	private Integer poa;
	private Map<Integer, Integer> truthfulResults;
	private boolean isIntro;
	
	public int getVoters() {
		return voters;
	}
	public void setVoters(int voters){
		this.voters = voters;
	}
	public int getRounds() {
		return rounds;
	}
	public void setRounds(int rounds) {
		this.rounds = rounds;
	}
	public String getPrefSet() {
		return prefSet;
	}
	public void setPrefSet(String prefSet) {
		this.prefSet = prefSet;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public int getTimePerRound() {
		return timePerRound;
	}
	public void setTimePerRound(int timePerRound) {
		this.timePerRound = timePerRound;
	}
	public Integer getConvTime() {
		return convTime;
	}
	public void setConvTime(Integer convTime) {
		this.convTime = convTime;
	}
	public void addVoteChange() {
		voteChanges.incrementAndGet();
	}
	public int getVoteChanges() {
		return voteChanges.get();
	}

	public Integer getPoa() {
		return poa;
	}
	public void setPoa(Integer poa) {
		this.poa = poa;
	}
	public Map<Integer, Integer> getTruthfulResults() {
		return truthfulResults;
	}
	public void setTruthfulResults(Map<Integer, Integer> truthfulResults) {
		this.truthfulResults = truthfulResults;
	}
	public int getActualVoters() {
		return actualVoters;
	}
	public void setActualVoters(int actualVoters) {
		this.actualVoters = actualVoters;
	}
	public int getWinner() {
		return winner;
	}
	public void setWinner(int winner) {
		this.winner = winner;
	}
	public Integer getCandsNum() {
		return candsNum;
	}
	public void setCandsNum(Integer candsNum) {
		this.candsNum = candsNum;
	}
	public boolean isIntro() {
		return isIntro;
	}
	public void setIntro(boolean isIntro) {
		this.isIntro = isIntro;
	}
	public Integer getAgentsNum() {
		return agentsNum;
	}
	public void setAgentsNum(Integer agentsNum) {
		this.agentsNum = agentsNum;
	}
}
