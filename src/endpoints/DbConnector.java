package endpoints;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import blocks.GameDetails;
import blocks.Voter;

public class DbConnector {

	// static DataSource datasource;
	private static DbConnector INSTANCE;

	static HikariConfig config = new HikariConfig("/hikari.properties");

	private HikariDataSource ds;

	private static Object mutex = new Object();

	// static AtomicInteger id = new AtomicInteger(10000);

	static final Logger logger = LoggerFactory.getLogger(DbConnector.class);

	private DbConnector() {
		ds = new HikariDataSource(config);
	};

	// public static void closeDS() {
	// if (ds != null) {
	// ds.close();
	// }
	// ds = null;
	// }

	public static Connection getConnection() {
		Connection conn = null;

		if (INSTANCE == null) {
			synchronized (mutex) {
				if (INSTANCE == null) {
					INSTANCE = new DbConnector();
				}
			}
		}

		try {
			conn = INSTANCE.ds.getConnection();

		} catch (SQLException e) {
			logger.info("ERROR in getConnection: " + e);
		}
		// DbConnector
		// if(DbConnector.getInstance()
		//
		// if (ds == null) {
		// synchronized (DbConnector.class) {
		// logger.info("getConnection() create data source");
		//
		// ds
		// }
		// }
		// try {
		// conn = ds.getConnection();
		// } catch (SQLTransientConnectionException e) {
		// logger.error("ERROR while getting connection:", e);
		// closeDS();
		// conn = getConnection();
		// }

		return conn;
	}

	public static int[][] getCandsSet(String tableName, int rowNum) {

		logger.info("getCandsSet()", tableName, rowNum);
		String sql = "Select * from set_" + tableName + " ORDER BY RAND() LIMIT 1";

		ResultSet rs = null;
		int[][] profiles = new int[rowNum][];

		try (Connection conn = getConnection()) {

			PreparedStatement stmt = conn.prepareStatement(sql);
			// setObjects(stmt, new Object[] { rowNum });

			for (int row = 0; row < rowNum; row++) {
				rs = stmt.executeQuery();

				// int count = 0;
				rs.next();
				String[] strArray = rs.getString("profile").split(",");
				int[] profile = new int[strArray.length];
				for (int i = 0; i < strArray.length; i++) {
					profile[i] = Integer.parseInt(strArray[i]);
				}
				profiles[row] = profile;
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		logger.info("getCandsSet generated profiles: {}", Arrays.deepToString(profiles));
		return profiles;
	}

	// public static GameDetails getNextGame1() throws SQLException {
	//
	// GameDetails game = null;
	//
	// String sql = "SELECT * from games where status is null limit 1";
	// try (Connection conn = getConnection();
	// Statement stmt = conn.createStatement();) {
	//
	// ResultSet rs = stmt.executeQuery(sql);
	//
	// if (rs.next()) {
	// game = new GameDetails();
	// game.setId(rs.getInt("id"));
	// game.setVoters(rs.getInt("voters"));
	// game.setRounds(rs.getInt("rounds"));
	// game.setTimePerRound(rs.getInt("timePerRound"));
	// game.setPrefSet(rs.getString("prefset"));
	// }
	// }
	//
	// return game;
	// }

	public static UserInfo getUserInfo(String uid) {
		UserInfo info = new UserInfo();
		String sql = "SELECT count(*) as totalGames,sum(score) as totalScore from voters where uid = '" + uid + "'";
		// String sql = "select (select count(*) from voters where uid = "+uid+"
		// )as games,AVG(score)*count(*)/5 as totalavg from (select * from
		// (select * from voters where uid = "+uid+" order by score desc) as
		// ordered limit 5) as total; ";

		try (Connection conn = getConnection()) {

			PreparedStatement stmt = conn.prepareStatement(sql);
			// stmt.setObject(1, uid);
			// stmt.setObject(2, uid);
			ResultSet rs = stmt.executeQuery(sql);

			if (rs.next()) {
				info.totalGames = rs.getInt("totalGames");
				info.totalScore = rs.getInt("totalScore");

			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return info;

	}

	public static void testConnection() throws SQLException {

		String sql = "select 1 from voters limit 1";

		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql);) {

			stmt.executeQuery(sql);

		}
	}

	public static void addGame(GameDetails gameDetails) {

		// gameDetails.setId(id.getAndIncrement());

		String sql = "INSERT INTO games (voters,rounds,timePerRound,prefset, status,start_date,is_intro) VALUES (? ,? ,? ,?, 'RUNNING',sysdate(),?)";
		Object[] objects = new Object[] { gameDetails.getVoters(), gameDetails.getRounds(),
				gameDetails.getTimePerRound(), gameDetails.getPrefSet(), gameDetails.isIntro() ? 1 : null };

		try (Connection conn = getConnection()) {

			PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			setObjects(stmt, objects);
			int affectedRows = stmt.executeUpdate();

			if (affectedRows == 0) {
				throw new SQLException("Creating game failed, no rows affected.");
			}

			ResultSet generatedKeys = stmt.getGeneratedKeys();
			if (generatedKeys.next()) {
				gameDetails.setId(generatedKeys.getInt(1));
			} else {
				throw new SQLException("Creating game failed, no ID obtained.");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void saveVoter(Voter voter, Integer disconnectionRound) {

		String sql = "INSERT INTO voters (name,uid,score,game_id,disconnection_round,vote_change_requests,vote_change_for_pw,vote_change_for_npw) VALUES (? ,? ,? ,?,?,?,?,?)";
		Object[] objects = new Object[] { voter.getName(), voter.getUid(), voter.getScore(), voter.getGameId(),
				disconnectionRound, voter.getVoteChanges(), voter.getVoteChangeForPW(),
				voter.getVoteChangeForNPW() };
		executeUpdateSQL(sql, objects);
	}

	public static void saveVoterAction(Voter voter, int round) {

		String sql = "INSERT INTO voter_actions (game_id,voter_id,round,prefset,selected_cand,next_selected_cand) VALUES (? ,? ,? ,?,?,?)";

		Object[] objects = new Object[] { voter.getGameId(), voter.getUid(), round,
				Arrays.toString(voter.getOrderedCands()), voter.getSelectedCand(), voter.getNextCand() };
		executeUpdateSQL(sql, objects);

	}

	public static void updateSelectedVoter(Voter voter, int round) {

		String sql = "UPDATE voter_actions set changed_flag = 1 where game_id= ? and voter_id = ? and round = ?";

		Object[] objects = new Object[] { voter.getGameId(), voter.getUid(), round };
		executeUpdateSQL(sql, objects);

	}

	// public static void setGameStatus1(int id, String status) throws
	// SQLException {
	//
	// String sql = "UPDATE games set status = ? , start_date = sysdate() where
	// id = ?";
	//
	// Object[] objects = new Object[] { status, id };
	// executeUpdateSQL(sql, objects);
	//
	// }

	public static void saveGame(GameDetails game) {

		String sql = "UPDATE games set status = 'FINISHED',end_date = sysdate(),cands_num= ?,conv_time = ?, actual_voters=?,agentsNum =?, winner=?, vote_changes=?,poa=? where id = ?";

		Object[] objects = new Object[] { game.getCandsNum(), game.getConvTime(), game.getActualVoters(), game.getAgentsNum(),
				game.getWinner(), game.getVoteChanges(), game.getPoa(), game.getId() };

		executeUpdateSQL(sql, objects);

	}

	private static void setObjects(PreparedStatement stmt, Object[] objects) throws SQLException {
		int i = 1;
		if (objects != null) {
			for (Object object : objects) {
				stmt.setObject(i++, object);
			}
		}
	}

	public static void executeUpdateSQL(String sql, Object[] objects) {

		try (Connection conn = getConnection();) {

			PreparedStatement stmt = conn.prepareStatement(sql);
			setObjects(stmt, objects);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public static Integer getTotalGames() {
		String sql = "select count(*) as total  from games; ";

		Integer total = null;

		try (Connection conn = getConnection()) {

			PreparedStatement stmt = conn.prepareStatement(sql);
			ResultSet rs = stmt.executeQuery(sql);

			if (rs.next()) {
				total = rs.getInt("total");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return total;
	}

	// public static void addGame1(Integer voters, Integer rounds, Integer
	// timePerRound, String candsSet)
	// throws SQLException {
	//
	// String sql = "INSERT INTO games (voters,rounds,timePerRound,prefset)
	// values (" + voters + "," + rounds + ","
	// + timePerRound + ",'" + candsSet + "')";
	// executeUpdateSQL(sql);
	//
	// }

	// public static void saveVoter(Voter voter) throws SQLException {
	// logger.info("saveVoter() enter for voter " + voter.getUid());
	// String sql = "INSERT INTO voters
	// (name,uid,score,game_id,vote_change_requests,vote_change_for_pw,vote_change_for_npw)
	// values ('"
	// + voter.getName() + "', " + voter.getUid() + ", " + voter.getScore() +
	// "," + voter.getGameId() + ","
	// + voter.getVoteChanges() + "," + voter.getVoteChangeForPW() + "," +
	// voter.getVoteChangeForNPW() + ")";
	// logger.info(sql);
	// executeUpdateSQL(sql);
	// }
	//
	// public static void saveVoterAction(Voter voter, int round, boolean
	// isChanged) throws SQLException {
	// logger.info("saveVoterLog() enter for voter {}", voter.getUid());
	// int changedFlag = isChanged ? 1 : 0;
	//
	// String sql = "INSERT INTO voter_actions
	// (game_id,voter_id,round,prefset,selected_cand,next_selected_cand,
	// changed_flag) values ('"
	// + voter.getGameId() + "', '" + voter.getUid() + "', " + round + ", '"
	// + Arrays.toString(voter.getOrderedCands()) + "'," +
	// voter.getSelectedCand() + "," + voter.getNextCand()
	// + "," + changedFlag + ")";
	// logger.info(sql);
	// executeUpdateSQL(sql);
	// }
	//
	// public static void setGameStatus(int id, String status) throws
	// SQLException {
	//
	// String sql = "UPDATE games set status = '" + status + "' where id = " +
	// id;
	// executeUpdateSQL(sql);
	//
	// }
	//
	// public static void saveGame(GameDetails game) throws SQLException {
	//
	// String sql = "UPDATE games set status = 'FINISHED',run_date =
	// sysdate(),conv_time = " + game.getConvTime()
	// + ", actual_voters=" + game.getActualVoters() + ", winner=" +
	// game.getWinner() + ", vote_changes="
	// + game.getVoteChanges() + ",poa=" + game.getPoa() + " where id = " +
	// game.getId();
	// executeUpdateSQL(sql);
	//
	// }

	// public static String executeUpdateSQL1(String sql) throws SQLException {
	// Connection conn = null;
	// Statement stmt = null;
	// String error = null;
	// // try {
	// try {
	// conn = getConnection();
	// stmt = conn.createStatement();
	// stmt.executeUpdate(sql);
	//
	// }
	//
	// finally {
	// if (conn != null)
	// try {
	// conn.close();
	// } catch (Exception ignore) {
	// }
	// }
	// return error;
	// }
	public static class UserInfo {
		private Integer totalGames;
		private Integer totalScore;

		public Integer getTotalGames() {
			return totalGames;
		}

		public Integer getTotalScore() {
			return totalScore;
		}
	}
}
