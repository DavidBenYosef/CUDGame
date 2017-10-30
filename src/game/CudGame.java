package game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import blocks.GameDetails;
import blocks.Voter;
import endpoints.DbConnector;
import message.MessageUtil;
import message.UserInMessage;
import message.UserOutMessage;

public class CudGame {

	private final Logger logger = LoggerFactory.getLogger(CudGame.class);
	private Random randomGenerator = new Random();

	private Map<String, Voter> voters;

	protected GameDetails gameDetails;

	private int roundsLeft;

	private Map<Integer, Integer> results;

	// private List<Integer> pwf;

	//private RoundChecker roundChecker;

	private List<String> votersResponse = new ArrayList<String>();

	// private List<String> voteChanges = new ArrayList<String>();

	private boolean isFinished;
	private boolean isRunning;

	private int ID;

	public boolean isRunning() {
		return isRunning;
	}

	public Map<String, Voter> getVoters() {
		return voters;
	}

	public CudGame(GameDetails gameDetails, Map<String, Voter> voters) {
		this.voters = voters;
		this.gameDetails = gameDetails;
		this.ID = gameDetails.getId();
		this.roundsLeft = gameDetails.getRounds();
		// try {
		// DbConnector.setGameStatus(ID, "RUNNING");
		// } catch (SQLException e) {
		// e.printStackTrace();
		// }
	}

	public void startGame() {

		logger.info("GAME STARTED: " + ID);
		logger.info(ID + ": New game is running: {}", ID);
		isRunning = true;
		results = Collections.synchronizedMap(new HashMap<Integer, Integer>());

		int[][] profiles = null;

		profiles = DbConnector.getCandsSet(gameDetails.getPrefSet(), voters.size());
		gameDetails.setCandsNum(profiles[0].length);

		Util.initCountMap(results, 1, gameDetails.getCandsNum());

		int count = 0;
		Set<Entry<String, Voter>> entrySet = voters.entrySet();
		synchronized (voters) {
			for (Map.Entry<String, Voter> entry : entrySet) {
				Voter voter = entry.getValue();
				voter.setGameId(ID);
				voter.setOrderedCands(profiles[count++]);
				int selectedCand = voter.getOrderedCands()[0];
				voter.setSelectedCand(selectedCand);
				results.put(selectedCand, results.get(selectedCand) + 1);

			}
		}

		gameDetails.setTruthfulResults(new HashMap<Integer, Integer>(results));
		gameDetails.setActualVoters(voters.size());

		UserOutMessage mes = new UserOutMessage(gameDetails);

		MessageUtil.notifyAll(voters, mes);
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
		}
		// pwf = Util.generatePWF(results, roundsLeft.get(),
		// gameDetails.getActualVoters());

		startNextRound();

	}

	// public void checkRound(String uid, int round) {
	//
	// if (isFinished || round > roundsLeft) {
	// return;
	// }
	// synchronized (votersResponse) {
	// }
	// }

	private void processSelections(List<String> votersResponse, int round) {

		//roundChecker.timer.cancel();
		logger.info(ID + " round " + round + ": All voted!");

		List<String> voteChanges = new ArrayList<String>();
		for (String voterId : votersResponse) {
			Voter voter = voters.get(voterId);
			if (voter.getNextCand() != null) {
				voteChanges.add(voterId);
				voter.addVoteChange();
			}
			DbConnector.saveVoterAction(voter, round);
		}

		String changedVoter = null;
		if (!voteChanges.isEmpty()) {
			int rand = randomGenerator.nextInt(voteChanges.size());
			changedVoter = voteChanges.get(rand);
		}

		if (changedVoter != null) {

			Voter voter = voters.get(changedVoter);

			DbConnector.updateSelectedVoter(voter, round);

			int oldCand = voter.getSelectedCand();
			int newCand = voter.getNextCand();
			results.put(oldCand, results.get(oldCand) - 1);
			voter.setSelectedCand(newCand);
			results.put(newCand, results.get(newCand) + 1);
			gameDetails.addVoteChange();
			voter.sendInfo("{VOTE_SUBMITTED} <img src=\"gfx/v.png\" />");
			for (String voterId : voteChanges) {
				if (!voterId.equals(changedVoter)) {
					voters.get(voterId).sendInfo("{VOTE_NO_CHANGE} <img src=\"gfx/x.png\" />");
				}
			}
		} else {
			UserOutMessage msg = new UserOutMessage("{NO_ONE_CHANGE}");
			MessageUtil.notifyAll(voters, msg);
		}

		// pwf = Util.generatePWF(results, roundsLeft.get(),
		// gameDetails.getActualVoters());

		try {
			Thread.sleep(2500);
		} catch (InterruptedException e) {

		}
		roundsLeft--;
		startNextRound();

	}

	private void startNextRound() {

		logger.info(ID + ": Starting round: {}", roundsLeft);
		// logger.info("PWF: {}", pwf);

		int winner = Util.getWinner(results);
		boolean isDeadline = roundsLeft == 0;

		isFinished = (winner > 0 || isDeadline);

		if (isFinished) {
			logger.info(ID + ": Game " + gameDetails.getId() + " finished!");

			endGame(winner);

		} else {

			// votersResponse.clear();

			// voteChanges.clear();

			// roundsLeft--;

			Set<Entry<String, Voter>> entrySet = voters.entrySet();
			synchronized (voters) {

				for (Map.Entry<String, Voter> entry : entrySet) {

					Voter voter = entry.getValue();

					UserOutMessage mes = new UserOutMessage("NEW_ROUND", voter, "{WANT_TO_CHANGE}", roundsLeft,
							results);

					voter.sendJson(mes);

				}
			}

			//roundChecker = new RoundChecker(this);
		}
	}

	public void removeVoter(String uid) {
		if (!isFinished) {
			Voter voter = voters.get(uid);
			if (voter != null) {

				int selectedCand = voter.getSelectedCand();
				results.put(selectedCand, results.get(selectedCand) - 1);
				int votersNum;
				List<String> votersResponseClone = null;
				synchronized (voters) {
					voters.remove(uid);
					DbConnector.saveVoter(voter, roundsLeft);
					votersNum = voters.size();
				}

				if (votersNum > 0) {
					gameDetails.setActualVoters(votersNum);
					MessageUtil.notifyAll(voters, new UserOutMessage(votersNum));
					synchronized (votersResponse) {
						votersResponse.remove(uid);
						if (votersResponse.size() == votersNum) {
							votersResponseClone = new ArrayList<>(votersResponse);
							votersResponse.clear();
						}
					}
					if (votersResponseClone != null) {
						processSelections(votersResponseClone, roundsLeft);
					}
				} else {
					endGame(0);
				}

			} else {
				logger.error(ID + ": Remove voter invoked for {} but it does not exist in voters: {} ", uid, voters);
			}
		}
	}

	public void forceNextRound() {
		List<String> votersResponseClone = null;
		synchronized (votersResponse) {
			votersResponseClone = new ArrayList<>(votersResponse);
			votersResponse.clear();
		}
		if (votersResponseClone != null) {
			Set<String> missingVoters = new HashSet<>(voters.keySet());
			missingVoters.removeAll(votersResponseClone);
			logger.info(ID + " round: {} Missing voters: {}", roundsLeft, missingVoters);
			processSelections(votersResponseClone, roundsLeft);
		}
	}

	public void userSelection(String uid, UserInMessage mes) {
		int round = mes.getRound();
		Voter voter = voters.get(uid);
		if (voter != null && round == roundsLeft) {
			// System.out.println("Game: "+ID+" "+uid+": round:"+round+" Voters:
			// "+voters);
			Integer selectedCand = mes.getSelectedCand();

			voter.setNextCand(selectedCand);

			List<String> votersResponseClone = null;
			synchronized (votersResponse) {
				votersResponse.add(uid);
				logger.info(
						ID + " round " + round + ": userSelection for uid {} resp count: {} voters: " + voters.size(),
						uid, votersResponse.size(), voters.size());
				if (votersResponse.size() == voters.size()) {
					votersResponseClone = new ArrayList<>(votersResponse);
					votersResponse.clear();
				}
			}
			if (votersResponseClone != null) {
				processSelections(votersResponseClone, round);
			}
		} else {
			logger.error("NULL Game: " + ID + " " + uid + " round:" + round + "Voters: " + voters);
		}
	}

	public void endGame(int winner) {

		Set<Entry<String, Voter>> entrySet = voters.entrySet();
		synchronized (voters) {
			for (Map.Entry<String, Voter> entry : entrySet) {

				Voter voter = entry.getValue();
				String type;

				if (winner > 0) {

					if (!gameDetails.isIntro()) {
						voter.setScore(Util.calculateScore(voter.getOrderedCands().length,
								voter.getOrderedCandsList().indexOf(voter.getSelectedCand())));
					}
					type = "DECIDED";
				} else {

					type = "DEADLINE";
				}

				UserOutMessage mes = new UserOutMessage(type, voter, null, roundsLeft, results);

				voter.sendJson(mes);

				DbConnector.saveVoter(voter, null);

			}
		}
		if (winner > 0) {
			gameDetails.setPoa(Util.generatePoa(gameDetails.getTruthfulResults(), winner));
			gameDetails.setConvTime(gameDetails.getRounds() - roundsLeft);
		}
		gameDetails.setWinner(winner);

		DbConnector.saveGame(gameDetails);

		SessionHandler.getInstance().removeGame(gameDetails.getId(), voters);
		logger.info("GAME FINISHED: " + ID);
	}

}
