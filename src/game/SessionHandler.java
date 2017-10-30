package game;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Properties;

import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.Session;

import blocks.GameDetails;
import blocks.Voter;
import endpoints.AgentEndpoint;
import endpoints.DbConnector;
import endpoints.DbConnector.UserInfo;
import message.AdminInMessage;
import message.AdminOutMessage;
import message.MessageUtil;
import message.UserInMessage;
import message.UserOutMessage;

public class SessionHandler {
	private static SessionHandler INSTANCE;
	private static Object mutex = new Object();

	private static AtomicInteger agentCounter = new AtomicInteger(1);

	public static SessionHandler getInstance() {
		if (INSTANCE == null) {
			synchronized (mutex) {
				if (INSTANCE == null) {
					INSTANCE = new SessionHandler();
				}
			}
		}
		return INSTANCE;
	}

	private AgentCreator agentCreator;

	private final Logger logger = LoggerFactory.getLogger(SessionHandler.class);

	private Map<String, Integer> activeVoters = new ConcurrentHashMap<>();

	private Map<String, Voter> pendingVoters = new ConcurrentHashMap<>();

	private List<Integer> pendingIntroGames = Collections.synchronizedList(new ArrayList<Integer>());

	private Map<Integer, CudGame> games = new ConcurrentHashMap<>();

	private Session adminSession;

	// private GameDetails nextGame;

	private boolean gameActiveFlag;
	private int votersNum;
	private int rounds;
	private int timePerRound;
	private String candsSet;
	private Integer gamesLimit;
	private Integer scoreLimit;
	private Integer introGames;
	private Integer agentTimer;

	private String ServerURI;

	private static final String AGENT = "AGENT";

	private SessionHandler() {
		loadProperties();
	}

	private void loadProperties() {
		Properties prop = new Properties();
		String filename = "config.properties";
		try (InputStream input = SessionHandler.class.getClassLoader().getResourceAsStream(filename)) {
			if (input == null) {
				logger.error("Sorry, unable to find " + filename);
				return;
			}

			// load a properties file
			prop.load(input);

			gameActiveFlag = Boolean.parseBoolean(prop.getProperty("gameActiveFlag"));
			votersNum = Integer.parseInt(prop.getProperty("voters"));
			rounds = Integer.parseInt(prop.getProperty("rounds"));
			timePerRound = Integer.parseInt(prop.getProperty("timePerRound"));
			candsSet = prop.getProperty("candsSet");
			if (prop.getProperty("gamesLimit") != null) {
				gamesLimit = new Integer(prop.getProperty("gamesLimit"));
			}
			if (prop.getProperty("scoreLimit") != null) {
				scoreLimit = new Integer(prop.getProperty("scoreLimit"));
			}
			if (prop.getProperty("introGames") != null) {
				introGames = new Integer(prop.getProperty("introGames"));
			}
			if (prop.getProperty("agentTimer") != null) {
				agentTimer = new Integer(prop.getProperty("agentTimer"));
			}

		} catch (IOException ex) {
			logger.error("Failed to load properties", ex);
		}

	}

	public void setAdmin(Session session) {
		adminSession = session;
		AdminOutMessage mes = new AdminOutMessage(gameActiveFlag, votersNum, rounds, timePerRound, candsSet, gamesLimit,
				scoreLimit, introGames, agentTimer);
		MessageUtil.sendJson(adminSession, mes);
	}

	public void addSession(Session session, String uid, String name) {

		if (!gameActiveFlag) {
			disconnectUser(session, uid, "{GAME_INACTIVE}");
			return;
		}

		boolean isHuman = !uid.startsWith(AGENT);
		UserInfo userInfo = null;
		boolean isIntro = false;
		if (isHuman) {
			userInfo = DbConnector.getUserInfo(uid);
			isIntro = introGames != null && userInfo.getTotalGames() < introGames;

		}
		if (userInfo != null && (gamesLimit != null && userInfo.getTotalGames() >= gamesLimit
				|| scoreLimit != null && userInfo.getTotalScore() >= scoreLimit)) {
			StringBuilder builder = new StringBuilder();
			builder.append("{GAMES_LIMIT} ").append("<span></span>").append("{TOTAL_GAMES} ").append("<span></span>")
					.append(userInfo.getTotalGames()).append(", ").append("<span></span>").append(" {TOTAL_AVG} ")
					.append("<span></span>").append(userInfo.getTotalScore());

			disconnectUser(session, uid, builder.toString());
			return;
		}

		if (activeVoters.get(uid) == null && pendingVoters.get(uid) == null) {
			addPendingVoter(session, uid, name, isHuman, isIntro);
		}

		else {
			disconnectUser(session, uid, "{USER} <span>" + uid + "</span> {ALREADY_CONNECTED}");
		}

	}

	public void addPendingVoter(Session session, String uid, String name, boolean isHuman, boolean isIntro) {
		// CudGame game = null;
		Voter voter = new Voter();
		voter.setName(name);
		voter.setUid(uid);
		voter.setSession(session);

		String text = "{HI} <span>" + voter.getName() + "</span> {WAIT_FOR_GAME}";
		UserOutMessage mes = new UserOutMessage(text);
		mes.setTotalVoters(votersNum);
		voter.sendJson(mes);
		if (isHuman || pendingIntroGames.isEmpty()) {
			if (isIntro) {
				addVoterToPendingIntroGame(voter);
			} else {
				addVoterToPendingGame(voter);
			}
		} else {
			addAgentToPendingIntroGame(voter);
		}
	}

	public void addAgentToPendingIntroGame(Voter voter) {

		Integer gameNum = pendingIntroGames.get(0);
		CudGame game = games.get(gameNum);
		synchronized (game) {
			if (!game.isRunning()) {
				Map<String, Voter> voters = game.getVoters();
				voters.put(voter.getUid(), voter);
				activeVoters.put(voter.getUid(), gameNum);
				MessageUtil.notifyAll(voters, new UserOutMessage(voters.size()));
				if (voters.size() == votersNum) {
					pendingIntroGames.remove(gameNum);
					game.startGame();
				}
			}
		}
	}

	public void addVoterToPendingIntroGame(Voter voter) {
		GameDetails nextGame = createNewGame(true);
		nextGame.setAgentsNum(votersNum - 1);
		Map<String, Voter> gameVoters = new ConcurrentHashMap<>();
		gameVoters.put(voter.getUid(), voter);
		CudGame game = new CudGame(nextGame, gameVoters);
		voter.sendJson(new UserOutMessage(gameVoters.size()));
		games.put(nextGame.getId(), game);
		activeVoters.put(voter.getUid(), nextGame.getId());
		pendingIntroGames.add(nextGame.getId());
		createAgents(votersNum - 1, ServerURI);
	}

	public void addVoterToPendingGame(Voter voter) {
		Map<String, Voter> gameVoters = null;
		synchronized (pendingVoters) {

			pendingVoters.put(voter.getUid(), voter);
			MessageUtil.notifyAll(pendingVoters, new UserOutMessage(pendingVoters.size()));
			if (pendingVoters.size() == votersNum) {
				if (agentCreator != null) {
					agentCreator.timer.cancel();
				}
				// System.out.println("Im here! voter: "+voter.getUid()+"
				// pendingVoters:"+pendingVoters.size());
				gameVoters = Collections.synchronizedMap(new HashMap<String, Voter>(pendingVoters));
				pendingVoters.clear();

			} else {
				if (agentCreator != null) {
					agentCreator.timer.cancel();
				}
				//agentCreator = new AgentCreator(agentTimer);
			}
		}

		logger.info("addPendingVoter pendingVoters : {}", pendingVoters);
		if (gameVoters != null) {

			GameDetails nextGame = createNewGame(false);
			int agentsNum =0;
			for (String voterUid : gameVoters.keySet()) {
				activeVoters.put(voterUid, nextGame.getId());
				if(voterUid.startsWith(AGENT))
				{
					agentsNum++;	
				}
			}
			nextGame.setAgentsNum(agentsNum);
			CudGame game = new CudGame(nextGame, gameVoters);
			// System.out.println("Starting game "+nextGame.getId()+"
			// with
			// voters:"+gameVoters);
			games.put(nextGame.getId(), game);
			game.startGame();

		}
	}

	private GameDetails createNewGame(boolean isIntro) {
		GameDetails gameDetails = new GameDetails();
		gameDetails.setVoters(votersNum);
		gameDetails.setRounds(rounds);
		gameDetails.setTimePerRound(timePerRound);
		gameDetails.setPrefSet(candsSet);
		gameDetails.setIntro(isIntro);
		DbConnector.addGame(gameDetails);
		return gameDetails;
	}

	public void removeSession(Session session, String uid) {

		if (pendingVoters.get(uid) != null) {
			pendingVoters.remove(uid);

			MessageUtil.notifyAll(pendingVoters, new UserOutMessage(pendingVoters.size()));
		}

		Integer gameId = activeVoters.get(uid);

		if (gameId != null) {

			if (games.containsKey(gameId)) {
				games.get(gameId).removeVoter(uid);
			}
			activeVoters.remove(uid);

		}
		logger.info("removeSession pendingVoters : {}", pendingVoters);
		logger.info("removeSession allVoters : {}", activeVoters);

	}

	public void sendAdminMsg(String text) {
		MessageUtil.sendJson(adminSession, new AdminOutMessage(text));
	}

	public void handleInMsg(Session session, String uid, String name, UserInMessage mes) {

		switch (mes.getType()) {
		case "NEWGAME":
			userNewGame(session, uid, name);
			break;
		case "MYSCORE":
			userMyScore(session, uid);
			break;
		case "SELECT":
			userSelect(uid, mes);
			break;
		default:
			break;
		}

	}

	private void userSelect(String uid, UserInMessage mes) {

		Integer gameId = activeVoters.get(uid);
		if (gameId != null) {
			CudGame game = games.get(gameId);
			if (game != null) {
				game.userSelection(uid, mes);
			} else {
				logger.error("game is null for gameId" + gameId);
			}
		} else {
			logger.error("gameId is null for uid" + uid + "ActiveVoters: " + activeVoters + " Pending: " + pendingVoters
					+ " games: " + games);
		}
	}

	private void userMyScore(Session session, String uid) {
		StringBuilder builder = new StringBuilder();

		UserInfo userInfo = DbConnector.getUserInfo(uid);
		builder.append("{TOTAL_GAMES} ").append("<span></span>").append(userInfo.getTotalGames()).append(", ")
				.append("<span></span>");
		builder.append(" {TOTAL_SCORE} ").append("<span></span>").append(userInfo.getTotalScore());

		UserOutMessage scoreMsg = new UserOutMessage(builder.toString());
		MessageUtil.sendJson(session, scoreMsg);
	}

	private void userNewGame(Session session, String uid, String name) {
		// if (nextGame == null) {
		// logger.info("Next game is empty");
		// disconnectUser(session, "{NO_GAMES}");
		//
		// } else {

		addSession(session, uid, name);
		// }
	}

	public void disconnectUser(Session session, String uid, String closeReason) {

		try {
			session.close(new CloseReason(CloseCodes.NORMAL_CLOSURE, closeReason));

		} catch (IOException e) {

		}
	}

	public void removeGame(int id, Map<String, Voter> voters) {

		for (Entry<String, Voter> entry : voters.entrySet()) {
			activeVoters.remove(entry.getKey());
			Voter voter = entry.getValue();
			if (voter.getUid().startsWith(AGENT)) {
				disconnectUser(voter.getSession(), entry.getKey(), "GAME FINISHED");
			}
		}
		logger.info("allVoters: " + activeVoters);

		logger.info("Removed game id {}", id);
		games.remove(id);

		logger.info("Current games: " + games);
	}

	public void adminMsg(Session session, AdminInMessage mes) {

		switch (mes.getType()) {
		case "STATUS":
			getStatus();
			break;
		case "TEST":
			testConnection();
			break;
		// case "ADD":
		// addNewGame(mes);
		// break;
		case "TOTAL":
			getTotalGames();
			break;
		// case "REMOVE":
		// removeGame();
		// break;
		case "SWITCH":
			switchGame(mes);
			break;
		case "ADD_AGENT":
			int num = 1;
			Integer agentsNum = mes.getAgentsNum();
			if (agentsNum != null) {
				num = agentsNum;
			}
			createAgents(num, mes.getText());
			break;
		// case "SET_LIMIT":
		// setLimit(mes);
		// break;
		default:
			break;
		}

	}

	private void switchGame(AdminInMessage mes) {
		gameActiveFlag = mes.getFlag();
		ServerURI = mes.getText();
		if (gameActiveFlag) {

			votersNum = mes.getVoters();
			rounds = mes.getRounds();
			timePerRound = mes.getTimePerRound();
			candsSet = mes.getCandsSet();
			gamesLimit = mes.getGamesLimit();
			scoreLimit = mes.getScoreLimit();
			introGames = mes.getIntroGames();
			agentTimer = mes.getAgentTimer();

			sendAdminMsg("Game switched on!");

		} else {
			if (!pendingVoters.isEmpty()) {
				Set<String> keySet = pendingVoters.keySet();
				for (String key : keySet) {
					disconnectUser(pendingVoters.get(key).getSession(), key, "{GAME_OFF}");
				}

			}
			// nextGame = null;
			sendAdminMsg("Game switched off!");
		}
	}

	// private void removeGame() {
	// if (nextGame != null) {
	// try {
	// Set<Entry<String, Voter>> entrySet = pendingVoters.entrySet();
	// synchronized (pendingVoters) {
	// for (Map.Entry<String, Voter> entry : entrySet) {
	// disconnectUser(entry.getValue().getSession(), "{PENDING_GAME_OFF}");
	// }
	// }
	// DbConnector.setGameStatus(nextGame.getId(), "REMOVED");
	//
	// sendAdminMsg(null, "Pending Game " + nextGame.getId() + " Removed!");
	// nextGame = DbConnector.getNextGame();
	// } catch (SQLException e) {
	// sendAdminMsg(null, "Database error");
	// e.printStackTrace();
	// }
	// } else {
	// sendAdminMsg(null, "No pending game");
	// }
	// }

	private void getTotalGames() {

		Integer total = DbConnector.getTotalGames();
		sendAdminMsg("Total games: " + total);

	}

	// private void addNewGame(AdminInMessage mes) {
	// try {
	//// int num = 1;
	//// Integer gamesNum = mes.getGamesNum();
	//// if (gamesNum != null) {
	//// num = gamesNum;
	//// }
	//// for (int i = 0; i < num; i++) {
	//// DbConnector.addGame(mes.getVoters(), mes.getRounds(),
	// mes.getTimePerRound(), mes.getCandsSet());
	//// }
	//// sendAdminMsg(null, num + " Game(s) added successfully!");
	//
	// } catch (SQLException e) {
	// sendAdminMsg(null, "Database error");
	// e.printStackTrace();
	// }
	// }

	private void testConnection() {
		try {
			sendAdminMsg("Checking connection..");
			DbConnector.testConnection();
			sendAdminMsg("Connected successfully!");
		} catch (SQLException e) {
			sendAdminMsg(e.getMessage());
			logger.error("Connection failed!", e);
		}
	}

	private void getStatus() {
		StringBuilder builder = new StringBuilder();
		builder.append("STATUS: " + pendingVoters.size() + " Pending voters: " + pendingVoters.keySet() + " "
				+ games.size() + " Running games: " + games.keySet());

		sendAdminMsg(builder.toString());
	}

	public String createAgent() {
		return createAgent(ServerURI);
	}

	public String createAgent(String URI) {
		String agentId = AGENT + String.format("%05d", agentCounter.get());
		boolean connected = false;
		while (!connected) {
			try {
				logger.info("Creating agent " + agentId);
				AgentEndpoint agentEndPoint = new AgentEndpoint(agentId);
				agentEndPoint.connect(new URI("ws://" + URI + "/User/" + agentId + "/" + agentId));
				agentCounter.incrementAndGet();
				connected = true;
			} catch (Exception e) {
				logger.error("Create agent " + agentId + " failed, retrying.. ", e);
			}
		}
		return agentId;
	}

	public void createAgents(int agentsNum, String URI) {
		for (int i = 0; i < agentsNum;i++) {
			createAgent(URI);
		}
	}


}
