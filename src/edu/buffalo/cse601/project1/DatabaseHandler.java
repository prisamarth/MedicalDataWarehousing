package edu.buffalo.cse601.project1;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import com.buffalo.edu.project1.StatisticsComputer;

/**
 * 
 * @author priyanka
 * 
 */

public class DatabaseHandler {

	// table names constants
	static final String DIAGNOSIS_TB = "DIAGNOSIS";
	static final String DISEASE_TB = "DISEASE";
	static final String DRUG_TB = "DRUG";
	static final String DRUGUSE_TB = "DRUGUSE";
	static final String CLINICALSAMPLE_TB = "CLINICALSAMPLE";
	static final String PROBE_TB = "PROBE";
	static final String GENEGOCLDM_TB = "GENEGOCLDM";
	static final String MICROARRAY_TB = "MICROARRAY";

	// other constants
	private static final String dbClassName = "com.mysql.jdbc.Driver";
	private static final String CONNECTION = "jdbc:mysql://127.0.0.1/project1";

	public static Statement queryStmt;
	public static StatisticsComputer stats;

	public DatabaseHandler() throws ClassNotFoundException {
		// establish database connection
		try {
			Class.forName(dbClassName);
			Properties p = new Properties();
			p.put("user", "priyanka");
			p.put("password", "priyanka");
			java.sql.Connection c = DriverManager.getConnection(CONNECTION, p);
			queryStmt = c.createStatement();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static int getCountOfPatientsForDisease(String disease)
			throws SQLException {
		String query = "SELECT COUNT(DISTINCT P_ID) AS CNT FROM "
				+ DIAGNOSIS_TB + " WHERE DS_ID IN (SELECT DS_ID FROM "
				+ DISEASE_TB + " WHERE NAME LIKE \'" + disease
				+ "\' OR TYPE LIKE \'" + disease + "\' OR DESCRIPTION LIKE \'"
				+ disease + "\')";
		ResultSet rs = queryStmt.executeQuery(query);
		rs.next();
		return rs.getInt("CNT");
	}

	public static ArrayList<String> getTypeOfDrugsForDisease(String disease)
			throws SQLException {
		String query = "SELECT DISTINCT TYPE FROM " + DRUG_TB
				+ " WHERE DR_ID IN (SELECT DR_ID FROM " + DRUGUSE_TB
				+ " WHERE P_ID IN (SELECT P.P_ID FROM " + DIAGNOSIS_TB
				+ " AS P," + DISEASE_TB
				+ " AS D WHERE P.DS_ID = D.DS_ID AND (D.NAME LIKE \'" + disease
				+ "\' OR D.TYPE LIKE \'" + disease
				+ "\' OR D.DESCRIPTION LIKE \'" + disease + "\')))";
		ResultSet rs = queryStmt.executeQuery(query);
		ArrayList<String> result = new ArrayList<String>();
		while (rs.next()) {
			result.add(rs.getString("TYPE"));
		}
		return result;
	}

	public static ArrayList<Integer> getExpressionValues(String clusterId,
			String disease, String measureUnit) throws SQLException {
		ArrayList<Integer> result = new ArrayList<Integer>();
		queryStmt.executeUpdate("DROP VIEW IF EXISTS SAMPLESALL");
		String query1 = "CREATE VIEW SAMPLESALL AS SELECT S_ID FROM "
				+ CLINICALSAMPLE_TB + " WHERE P_ID IN (SELECT P_ID FROM "
				+ DIAGNOSIS_TB + " AS DI, " + DISEASE_TB
				+ " AS DIS WHERE DI.DS_ID = DIS.DS_ID AND (DIS.NAME LIKE \'"
				+ disease + "\' OR DIS.TYPE LIKE \'" + disease
				+ "\' OR DIS.DESCRIPTION LIKE \'" + disease + "\'))";
		queryStmt.executeUpdate(query1);
		queryStmt.executeUpdate("DROP VIEW IF EXISTS PROBECLUSTER");
		String query2 = "CREATE VIEW PROBECLUSTER AS SELECT P.PB_ID FROM "
				+ PROBE_TB + " AS P, " + GENEGOCLDM_TB
				+ " AS G WHERE P.UID = G.UID AND G.CL_ID = " + clusterId;
		queryStmt.executeUpdate(query2);
		String query3 = "SELECT EXPRESSION FROM "
				+ MICROARRAY_TB
				+ " WHERE MU_ID = "
				+ measureUnit
				+ " AND S_ID IN (SELECT * FROM SAMPLESALL) AND PB_ID IN (SELECT * FROM PROBECLUSTER)";
		ResultSet rs = queryStmt.executeQuery(query3);
		while (rs.next()) {
			result.add(rs.getInt("EXPRESSION"));
		}
		return result;
	}

	public static double[] getExpressionsWithDisease(String goId, String disease)
			throws SQLException {

		queryStmt.executeUpdate("DROP VIEW IF EXISTS SAMPLESDISEASE");
		String query1 = "CREATE VIEW SAMPLESDISEASE AS SELECT S_ID FROM "
				+ CLINICALSAMPLE_TB + " WHERE P_ID IN (SELECT P_ID FROM "
				+ DIAGNOSIS_TB + " AS DI, " + DISEASE_TB
				+ " AS DIS WHERE DI.DS_ID = DIS.DS_ID AND DIS.NAME LIKE \'"
				+ disease + "\')";
		queryStmt.executeUpdate(query1);
		queryStmt.executeUpdate("DROP VIEW IF EXISTS PROBEGO");
		String query2 = "CREATE VIEW PROBEGO AS SELECT P.PB_ID FROM "
				+ PROBE_TB + " AS P, " + GENEGOCLDM_TB
				+ " AS G WHERE P.UID = G.UID AND G.GO_ID = " + goId;
		queryStmt.executeUpdate(query2);
		String query3 = "SELECT EXPRESSION FROM "
				+ MICROARRAY_TB
				+ " WHERE S_ID IN (SELECT S_ID FROM SAMPLESDISEASE) AND PB_ID IN (SELECT PB_ID FROM PROBEGO)";
		ResultSet rs = queryStmt.executeQuery(query3);
		int count = 0;
		while (rs.next()) {
			count++;
		}
		System.out.println("Rows with:" + count);
		double[] result = new double[count];
		rs.beforeFirst();
		for (int i = 0; i < count; i++) {
			rs.next();
			result[i] = rs.getInt("EXPRESSION");
		}
		return result;
	}

	public static double[] getExpressionsWithoutDisease(String goID,
			String disease) throws SQLException {

		queryStmt.executeUpdate("DROP VIEW IF EXISTS SAMPLESWITHOUTDISEASE");
		String query1 = "CREATE VIEW SAMPLESWITHOUTDISEASE AS SELECT S_ID FROM "
				+ CLINICALSAMPLE_TB
				+ " WHERE P_ID IN (SELECT P_ID FROM "
				+ DIAGNOSIS_TB
				+ " AS DI, "
				+ DISEASE_TB
				+ " AS DIS WHERE DI.DS_ID = DIS.DS_ID AND DIS.NAME NOT LIKE \'"
				+ disease + "\')";
		queryStmt.executeUpdate(query1);
		queryStmt.executeUpdate("DROP VIEW IF EXISTS PROBEGO");
		String query2 = "CREATE VIEW PROBEGO AS SELECT P.PB_ID FROM "
				+ PROBE_TB + " AS P, " + GENEGOCLDM_TB
				+ " AS G WHERE P.UID = G.UID AND G.GO_ID = " + goID;
		queryStmt.executeUpdate(query2);
		String query3 = "SELECT EXPRESSION FROM "
				+ MICROARRAY_TB
				+ " WHERE S_ID IN (SELECT S_ID FROM SAMPLESWITHOUTDISEASE) AND PB_ID IN (SELECT PB_ID FROM PROBEGO)";
		ResultSet rs = queryStmt.executeQuery(query3);
		int count = 0;
		while (rs.next()) {
			count++;
		}
		System.out.println("Rows with:" + count);
		double[] result = new double[count];
		rs.beforeFirst();
		for (int i = 0; i < count; i++) {
			rs.next();
			result[i] = rs.getInt("EXPRESSION");
		}
		return result;

	}

	public static Map<Integer, ArrayList<Double>> getSampleExpressionMap(
			String goId, String disease) throws SQLException {
		Map<Integer, ArrayList<Double>> result = new HashMap<Integer, ArrayList<Double>>();
		queryStmt.executeUpdate("DROP VIEW IF EXISTS SAMPLESDISEASE");
		String query1 = "CREATE VIEW SAMPLESDISEASE AS SELECT S_ID FROM "
				+ CLINICALSAMPLE_TB + " WHERE P_ID IN (SELECT P_ID FROM "
				+ DIAGNOSIS_TB + " AS DI, " + DISEASE_TB
				+ " AS DIS WHERE DI.DS_ID = DIS.DS_ID AND DIS.NAME LIKE \'"
				+ disease + "\')";
		queryStmt.executeUpdate(query1);
		queryStmt.executeUpdate("DROP VIEW IF EXISTS PROBEGO");
		String query2 = "CREATE VIEW PROBEGO AS SELECT P.PB_ID FROM "
				+ PROBE_TB + " AS P, " + GENEGOCLDM_TB
				+ " AS G WHERE P.UID = G.UID AND G.GO_ID = " + goId;
		queryStmt.executeUpdate(query2);
		String query3 = "SELECT EXPRESSION,S_ID FROM "
				+ MICROARRAY_TB
				+ " WHERE S_ID IN (SELECT S_ID FROM SAMPLESDISEASE) AND PB_ID IN (SELECT PB_ID FROM PROBEGO)";
		ResultSet rs = queryStmt.executeQuery(query3);
		while (rs.next()) {
			int key = rs.getInt("S_ID");
			ArrayList<Double> expr = result.get(key);
			if (expr == null)
				expr = new ArrayList<Double>();
			expr.add((double) rs.getInt("EXPRESSION"));
			result.put(key, expr);
		}
		return result;
	}

	public static Map<Integer, ArrayList<Double>> getSampleExpressionForDisease(
			String disease) throws SQLException {
		Map<Integer, ArrayList<Double>> result = new HashMap<Integer, ArrayList<Double>>();
		queryStmt.executeUpdate("DROP VIEW IF EXISTS SAMPLESDISEASE");
		String query1 = "CREATE VIEW SAMPLESDISEASE AS SELECT S_ID FROM "
				+ CLINICALSAMPLE_TB + " WHERE P_ID IN (SELECT P_ID FROM "
				+ DIAGNOSIS_TB + " AS DI, " + DISEASE_TB
				+ " AS DIS WHERE DI.DS_ID = DIS.DS_ID AND DIS.NAME LIKE \'"
				+ disease + "\')";
		queryStmt.executeUpdate(query1);
		String query3 = "SELECT EXPRESSION,S_ID FROM " + MICROARRAY_TB
				+ " WHERE S_ID IN (SELECT S_ID FROM SAMPLESDISEASE)";
		ResultSet rs = queryStmt.executeQuery(query3);
		while (rs.next()) {
			int key = rs.getInt("S_ID");
			ArrayList<Double> expr = result.get(key);
			if (expr == null)
				expr = new ArrayList<Double>();
			expr.add((double) rs.getInt("EXPRESSION"));
			result.put(key, expr);
		}
		return result;
	}

	public static Map<Integer, ArrayList<Double>> getSampleExpressionWithoutDisease(
			String disease) throws SQLException {
		Map<Integer, ArrayList<Double>> result = new HashMap<Integer, ArrayList<Double>>();
		queryStmt.executeUpdate("DROP VIEW IF EXISTS SAMPLESWITHOUTDISEASE");
		String query1 = "CREATE VIEW SAMPLESWITHOUTDISEASE AS SELECT S_ID FROM "
				+ CLINICALSAMPLE_TB
				+ " WHERE P_ID IN (SELECT P_ID FROM "
				+ DIAGNOSIS_TB
				+ " AS DI, "
				+ DISEASE_TB
				+ " AS DIS WHERE DI.DS_ID = DIS.DS_ID AND DIS.NAME NOT LIKE \'"
				+ disease + "\')";
		queryStmt.executeUpdate(query1);
		String query3 = "SELECT EXPRESSION,S_ID FROM " + MICROARRAY_TB
				+ " WHERE S_ID IN (SELECT S_ID FROM SAMPLESWITHOUTDISEASE)";
		ResultSet rs = queryStmt.executeQuery(query3);
		while (rs.next()) {
			int key = rs.getInt("S_ID");
			ArrayList<Double> expr = result.get(key);
			if (expr == null)
				expr = new ArrayList<Double>();
			expr.add((double) rs.getInt("EXPRESSION"));
			result.put(key, expr);
		}
		return result;
	}

	public static Map<Integer, HashMap<Integer, Double>> getGenesSampleExpression()
			throws SQLException {

		String query1 = "SELECT UID,S_ID,EXPRESSION FROM GENEEXPRESSION";
		ResultSet rs = queryStmt.executeQuery(query1);

		Map<Integer, HashMap<Integer, Double>> result = new HashMap<Integer, HashMap<Integer, Double>>();
		while (rs.next()) {
			int s_id = rs.getInt("S_ID");
			int UID = rs.getInt("UID");
			double expression = Double.parseDouble(rs.getString("EXPRESSION"));

			HashMap<Integer, Double> sample_expr = result.get(UID);
			if (sample_expr == null)
				result.put(UID, sample_expr = new HashMap<Integer, Double>());

			sample_expr.put(s_id, expression);
		}
		return result;
	}

	public static Map<Integer, HashMap<Integer, Double>> getSamplesGeneExpression()
			throws SQLException {

		String query1 = "SELECT UID,S_ID,EXPRESSION FROM GENEEXPRESSION";
		ResultSet rs = queryStmt.executeQuery(query1);

		Map<Integer, HashMap<Integer, Double>> result = new HashMap<Integer, HashMap<Integer, Double>>();
		while (rs.next()) {
			int s_id = rs.getInt("S_ID");
			int UID = rs.getInt("UID");
			double expression = Double.parseDouble(rs.getString("EXPRESSION"));

			HashMap<Integer, Double> sample_expr = result.get(s_id);
			if (sample_expr == null)
				result.put(s_id, sample_expr = new HashMap<Integer, Double>());

			sample_expr.put(UID, expression);
		}
		return result;
	}

	public static List<TreeMap<Double, Double>> getTestData()
			throws SQLException {
		List<TreeMap<Double, Double>> result = new ArrayList<TreeMap<Double, Double>>();

		double UID, expression;
		TreeMap<Double, Double> geneExpression = new TreeMap<Double, Double>();

		ResultSet resultSet = queryStmt
				.executeQuery("SELECT UID, TEST1 FROM TEST_SAMPLE");

		while (resultSet.next()) {
			UID = (double) resultSet.getInt("UID");
			expression = (double) resultSet.getInt("TEST1");
			geneExpression.put(UID, expression);
		}

		result.add(geneExpression);
		geneExpression = new TreeMap<Double, Double>();
		resultSet = queryStmt.executeQuery("SELECT UID,TEST2 FROM TEST_SAMPLE");

		while (resultSet.next()) {
			UID = (double) resultSet.getInt("UID");
			expression = (double) resultSet.getInt("TEST2");
			geneExpression.put(UID, expression);
		}
		result.add(geneExpression);

		geneExpression = new TreeMap<Double, Double>();
		resultSet = queryStmt.executeQuery("SELECT UID,TEST3 FROM TEST_SAMPLE");

		while (resultSet.next()) {
			UID = (double) resultSet.getInt("UID");
			expression = (double) resultSet.getInt("TEST3");
			geneExpression.put(UID, expression);
		}
		result.add(geneExpression);
		geneExpression = new TreeMap<Double, Double>();
		resultSet = queryStmt.executeQuery("SELECT UID,TEST4 FROM TEST_SAMPLE");

		while (resultSet.next()) {
			UID = (double) resultSet.getInt("UID");
			expression = (double) resultSet.getInt("TEST4");
			geneExpression.put(UID, expression);
		}
		result.add(geneExpression);
		// print(result.get(3).size());
		geneExpression = new TreeMap<Double, Double>();
		resultSet = queryStmt.executeQuery("SELECT UID,TEST5 FROM TEST_SAMPLE");

		while (resultSet.next()) {

			UID = (double) resultSet.getInt("UID");
			expression = (double) resultSet.getInt("TEST5");
			geneExpression.put(UID, expression);

		}
		result.add(geneExpression);
		return result;
	}
}
