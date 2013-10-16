package edu.buffalo.cse601.project1;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.Variance;


public class MainClass {

	/**
	 * @param args
	 * @author priyanka
	 * @throws IOException
	 */

	static DatabaseHandler dbHandler;
	static StatisticsComputer stats;
	private final static int TEST_CASE_COUNT = 5; 
	
	public static void main(String[] args) throws IOException,
			ClassNotFoundException, SQLException {
		dbHandler = new DatabaseHandler();
		stats = new StatisticsComputer();
		// while(true) {
		// System.out.println("Menu");
		// System.out.println("----------------------");
		// System.out.println("1:Results of part 2");
		// System.out.println("2:Results of part 3");
		// System.out.println("3:Exit");
		// System.out.println("Enter your option:");
		// BufferedReader reader = new BufferedReader(new
		// InputStreamReader(System.in));
		// String in = reader.readLine();
		// if(Integer.parseInt(in) == 1) {
		// part2();
		// }else if(Integer.parseInt(in) == 2) {
		//
		// } else {
		// System.out.println("Exiting");
		// System.exit(0);
		// }
		// reader.close();
		// }
		part2();

		Set<Double> informativeGenes = getInformativeGenes("ALL");
		test3(informativeGenes);
	}

	public static void test3(Set<Double> informativeGeneSet)
			throws SQLException {
		double tStat = 0.0;
		double probability = 0.0;
		List<double[]> testArray = new ArrayList<double[]>();
		List<TreeMap<Double, Double>> allTestSamples = dbHandler.getTestData();

		// investigate test data for its presence in informative gene
		for (int i = 0; i < allTestSamples.size(); i++) {
			for (Iterator<Double> it = allTestSamples.get(i).keySet()
					.iterator(); it.hasNext();) {
				// if sample data is not found in informative gene, remove it..
				if (!informativeGeneSet.contains(it.next())) {
					it.remove();
				}
			}
		}

		for (int i = 0; i < allTestSamples.size(); i++) {
			//populate test gene data to array
			double[] infoTestGeneArray = new double[informativeGeneSet.size()];
			int index = 0;
			for (Iterator<Double> it = allTestSamples.get(i).values()
					.iterator(); it.hasNext();) {
				infoTestGeneArray[index] = it.next();
				index++;
			}
			testArray.add(infoTestGeneArray);
		}

		double[] probabilityArr = new double[TEST_CASE_COUNT];

		Map<Integer, ArrayList<Double>> allDiseaseMap = dbHandler
				.getSampleExpressionForDisease("ALL");

		Map<Integer, ArrayList<Double>> withOutDiseaseMap = dbHandler
				.getSampleExpressionWithoutDisease("ALL");

		Map<Integer, HashMap<Integer, Double>> sampleExpression = dbHandler
				.getSamplesGeneExpression();

		for (int testCase = 0; testCase < TEST_CASE_COUNT; testCase++) {
			int outerIndex = 0;
			int size = informativeGeneSet.size();
			double[] correlationCoeffA = new double[allDiseaseMap.size()];

			for (Entry<Integer, ArrayList<Double>> geneEntry : allDiseaseMap.entrySet()) {
				double[] genetempExpression = new double[size];
				// map of sample ids
				for (Entry<Integer, HashMap<Integer, Double>> expression : sampleExpression
						.entrySet()) {
					if (geneEntry.getKey().intValue() == expression.getKey().intValue()) {
						int index = 0;
						// filter from informative genes
						for (Entry<Integer, Double> gene : expression.getValue()
								.entrySet()) {
							if (informativeGeneSet.contains((double) (gene
									.getKey()))) {
								genetempExpression[index] = gene.getValue();
								index++;
							}
						}
					}
				}
				correlationCoeffA[outerIndex] = stats.calculateCorrelation(
						genetempExpression, testArray.get(testCase));
				outerIndex++;
			}

			double[] correlationCoeffB = new double[withOutDiseaseMap.size()];
			outerIndex = 0;
			for (Entry<Integer, ArrayList<Double>> withOutDisease : withOutDiseaseMap
					.entrySet()) {
				double[] expression = new double[size];
				// map of sample_ids
				for (Entry<Integer, HashMap<Integer, Double>> e : sampleExpression
						.entrySet()) {
					if (withOutDisease.getKey().intValue() == e.getKey().intValue()) {
						int index = 0;
						// filter from informative genes
						for (Entry<Integer, Double> gene : e.getValue()
								.entrySet()) {
							if (informativeGeneSet.contains((double) (gene
									.getKey()))) {
								expression[index] = gene.getValue();
								index++;
							}
						}
					}
				}

				correlationCoeffB[outerIndex] = stats.calculateCorrelation(
						expression, testArray.get(testCase));
				outerIndex++;
			}

			 tStat = stats.calculateTStatistics(correlationCoeffA,
					correlationCoeffB);
			TDistribution tDistribution = new TDistribution(
					stats.calculateDegreeOfFreedom(correlationCoeffA,
							correlationCoeffB));
			probability = 1 - tDistribution.cumulativeProbability(tStat);
			probabilityArr[testCase] = probability;

			System.out.println("Test case # " + testCase + " has p-value of T-test as :" + probability + " . Hence it is "
					+ (probability < 0.01));
		}

	}

	public static Set<Double> getInformativeGenes(String disease)
			throws SQLException {
		int informativeGeneCount = 0;
		// all the genes with corresponding samples and expression
		Map<Integer, HashMap<Integer, Double>> geneExpression = dbHandler
				.getGenesSampleExpression();


		Map<Integer, ArrayList<Double>> allDiseaseMap = dbHandler
				.getSampleExpressionForDisease(disease);


		Map<Integer, ArrayList<Double>> notAllDiseaseMap = dbHandler
				.getSampleExpressionWithoutDisease(disease);


		Set<Integer> allDiseaseSet = allDiseaseMap.keySet();
		Set<Integer> notAllDiseaseSet = notAllDiseaseMap.keySet();


		Set<Double> informativeGeneSet = new HashSet<Double>();
		for (Entry<Integer, HashMap<Integer, Double>> expression : geneExpression.entrySet()) {

			Set<Integer> allTheGenes = new HashSet<Integer>();

			HashMap<Integer, Double> expressionValues = expression.getValue();

			Set<Integer> notAllTheGenes = new HashSet<Integer>();
			allTheGenes.addAll(allDiseaseSet);
			notAllTheGenes.addAll(notAllDiseaseSet);

			Iterator itr = allTheGenes.iterator();
			double[] geneArrayForAll = new double[allTheGenes.size()];
			int i = 0;
			while (itr.hasNext()) {
				int temp = (Integer) itr.next();
				geneArrayForAll[i] = expressionValues.get(temp);
				i++;
			}

			int j = 0;

			Iterator itr2 = notAllTheGenes.iterator();
			double[] geneArrayForNotAll = new double[notAllTheGenes.size()];
			while (itr2.hasNext()) {
				int temp = (Integer) itr2.next();
				geneArrayForNotAll[j] = expressionValues.get(temp);
				j++;
			}
			TDistribution tDistribution = new TDistribution(
					stats.calculateDegreeOfFreedom(geneArrayForAll, geneArrayForNotAll));
			// calculate t statistics
			double t = stats.calculateTStatistics(geneArrayForAll, geneArrayForNotAll);

			// calculate p-value of t-test
			double probability = 1 - Math.abs(tDistribution.cumulativeProbability(t));

			if (probability < 0.01) {
				informativeGeneCount++;
				informativeGeneSet.add((double) (expression.getKey()));
				System.out.println(expression.getKey() + "\t" + probability);
			}
		}
		System.out.println("Number of informative genes is "
				+ informativeGeneCount);
		return informativeGeneSet;
	}

	public static void part2() throws SQLException {

		System.out.println("Results of query1");
		System.out.println("The number of patients having tumor:"
				+ dbHandler.getCountOfPatientsForDisease("tumor"));
		System.out.println("The number of patients having leukemia:"
				+ dbHandler.getCountOfPatientsForDisease("leukemia"));
		System.out.println("The number of patients having ALL:"
				+ dbHandler.getCountOfPatientsForDisease("ALL"));
		part2Query2();
		part2Query3();
		part2Query4();
		part2Query5();
		part2Query6();
	}

	public static void part2Query6() throws SQLException {
		Map<Integer, ArrayList<Double>> sampleExpressionMap = dbHandler
				.getSampleExpressionMap("0007154", "ALL");
		// double[][] ALLExpressions = new double[3][];
		int i = 0, j = 0, count = 0;
		double sumOfCorrelationsWithAll = 0.0;
		double averageCorrelation = 0.0;
		// for(Entry<Integer,ArrayList<Double>>
		// entry:sampleExpressionMap.entrySet()) {
		// if(i == 2)
		// break;
		// ArrayList<Double> value = entry.getValue();
		// ALLExpressions[i] = new double[value.size()];
		// for(int j=0;j<value.size();j++)
		// ALLExpressions[i][j] = value.get(j);
		// i++;
		// }
		for (Entry<Integer, ArrayList<Double>> entry1 : sampleExpressionMap
				.entrySet()) {
			j = 0;
			ArrayList<Double> value1 = entry1.getValue();
			double[] sample1 = new double[value1.size()];
			for (int temp1 = 0; temp1 < value1.size(); temp1++) {
				sample1[temp1] = value1.get(temp1);
			}
			for (Entry<Integer, ArrayList<Double>> entry2 : sampleExpressionMap
					.entrySet()) {
				if (j > i) {
					ArrayList<Double> value2 = entry2.getValue();
					double[] sample2 = new double[value2.size()];
					for (int temp2 = 0; temp2 < value2.size(); temp2++) {
						sample2[temp2] = value2.get(temp2);
					}
					sumOfCorrelationsWithAll += stats.calculateCorrelation(
							sample1, sample2);
					count++;
				}
				j++;
			}
			i++;
		}
		System.out.println("count=" + count + "  samples= "
				+ sampleExpressionMap.size());
		averageCorrelation = sumOfCorrelationsWithAll / count;
		System.out
				.println("The average correlation between two patients with ALL: "
						+ averageCorrelation);

		Map<Integer, ArrayList<Double>> sampleExpressionMapAML = dbHandler
				.getSampleExpressionMap("0007154", "AML");
		count = 0;
		double sumOfCorrelationsForALLAndAML = 0.0;
		double averageCorrelationForALLAndAML = 0.0;

		for (Entry<Integer, ArrayList<Double>> entry1 : sampleExpressionMap
				.entrySet()) {
			ArrayList<Double> value1 = entry1.getValue();
			double[] sample1 = new double[value1.size()];
			for (int temp1 = 0; temp1 < value1.size(); temp1++) {
				sample1[temp1] = value1.get(temp1);
			}
			for (Entry<Integer, ArrayList<Double>> entry2 : sampleExpressionMapAML
					.entrySet()) {
				ArrayList<Double> value2 = entry2.getValue();
				double[] sample2 = new double[value2.size()];
				for (int temp2 = 0; temp2 < value2.size(); temp2++) {
					sample2[temp2] = value2.get(temp2);
				}
				sumOfCorrelationsForALLAndAML += stats.calculateCorrelation(
						sample1, sample2);
				count++;
			}
		}
		System.out.println("count=" + count + "  samples= "
				+ sampleExpressionMapAML.size());
		averageCorrelationForALLAndAML = sumOfCorrelationsForALLAndAML / count;
		System.out
				.println("The average correlation between patients with ALL and patients with AML: "
						+ averageCorrelationForALLAndAML);

	}

	public static void part2Query5() throws SQLException {
		double[] sampleALL = dbHandler.getExpressionsWithDisease("0007154",
				"ALL");
		double[] sampleAML = dbHandler.getExpressionsWithDisease("0007154",
				"AML");
		double[] sampleCT = dbHandler.getExpressionsWithDisease("0007154",
				"colon tumor");
		double[] sampleBT = dbHandler.getExpressionsWithDisease("0007154",
				"breast tumor");
		System.out
				.println("F statistic between patients with ALL, AML, Colon tumor and breast tumor:"
						+ stats.calculateFStatistics(sampleALL, sampleAML,
								sampleCT, sampleBT));

	}

	public static void part2Query4() throws SQLException {
		double[] sample1 = dbHandler
				.getExpressionsWithDisease("0012502", "ALL");
		double[] sample2 = dbHandler.getExpressionsWithoutDisease("0012502",
				"ALL");
		System.out.println("T statistic between patients with and without ALL:"
				+ stats.calculateTStatistics(sample1, sample2));
	}

	public static void part2Query2() throws SQLException {
		ArrayList<String> types = dbHandler.getTypeOfDrugsForDisease("tumor");
		System.out.println("The types of drugs applied to tumor");
		for (String type : types) {
			System.out.println(type);
		}
	}

	public static void part2Query3() throws SQLException {
		ArrayList<Integer> result = dbHandler.getExpressionValues("00002",
				"ALL", "001");
		System.out.println("Result of query 3 contains:" + result.size()
				+ " expressions. The values are:");
		for (int i : result) {
			System.out.print(i + "\t");
		}
	}

}
