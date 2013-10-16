package edu.buffalo.cse601.project1;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author adarshramakrishna
 *
 */
public class StatisticsComputer {

	public static void main(String[] args){
		double[] sample1 = {9,10,11,12};
		double[] sample2 = {2, 4, 6,8};
		double[] sample3 = {1, 3, 5, 7, 9};
		double[] sample4 = {5 ,9, 3, 8, 3};
		
		StatisticsComputer obj = new StatisticsComputer();
		System.out.println("Mean of sample1: " + obj.calculateMean(sample1));
		System.out.println("Mean of sample2: " + obj.calculateMean(sample2));
		
		System.out.println("Variance of sample1: " + obj.calculateVariance(sample1));
		System.out.println("Variance of sample2: " + obj.calculateVariance(sample2));
		
		System.out.println("T-stats of sample1 and sample2: " + obj.calculateTStatistics(sample1, sample2));
		System.out.println("F-stats of sample1 and sample2: " + obj.calculateFStatistics(sample1, sample2, sample3, sample4));
		
		
	}
	
	/*
	 * Function to calculate Degree of freedom for two data-sets
	 */
	public int 	calculateDegreeOfFreedom(double dataSet1[], double dataSet2[]) {
		
		double var1 = calculateVariance(dataSet1);
		double var2 = calculateVariance(dataSet2);
		double degreeOfFreedom = 0.0;

		double num;
		double den;

		num = Math.pow(((var1/dataSet1.length) + (var2/dataSet2.length)), 2);

		den = ((Math.pow((var1/dataSet1.length), 2) ) /(dataSet1.length -1) ) + ((Math.pow((var2/dataSet2.length), 2) ) /(dataSet2.length -1) );


		degreeOfFreedom = num/den;
		//print(degreeOfFreedom);
		return (int) degreeOfFreedom;
		}
	
	/*
	 * Function to calculate Degree of freedom for a given sample data-set
	 */
	public int calculateDegreeOfFreedom(int sizeOfSampleDataSet) {
		return sizeOfSampleDataSet-1;
	}
	
	/*
	 * Function to calculate mean of the given samples 
	 */
	
	public double calculateMean(double sample[]){	
		double sum = 0.0;
		for(double val : sample){
			sum = sum + val;
		}
		double mean = sum/sample.length;
		
		return mean;
	}
	
	/*
	 * Function to calculate variance of the given samples
	 */
	
	public double calculateVariance(double sample[]){
		double variance = 0.0;
		double sumOfSquaredDiff = 0.0;
		double mean = calculateMean(sample);
		for(double val : sample){
			sumOfSquaredDiff += Math.pow((val-mean), 2);
		}
		variance = sumOfSquaredDiff/(sample.length - 1);
		
		return variance;
	}
	
	/*
	 * Function to calculate T-statistics for two data-sets
	 * 
	 *  tstat = 			abs(mean(sample1) - mean(sample2))
	 *          ----------------------------------------------------------------------
	 *          SqrRt(variance(sample1)/size(sample1) + variance(sample2)/size(sample2))  
	 */
	
	public double calculateTStatistics(double sample1[], double sample2[]){
		double tStatisticsResult = 0.0;
		double mean1 = calculateMean(sample1);
		double mean2 = calculateMean(sample2);
		double variance1 = calculateVariance(sample1);
		double variance2 = calculateVariance(sample2);
		
		tStatisticsResult = (mean1-mean2) / (Math.sqrt((variance1/sample1.length)+ (variance2/sample2.length)));
		
		return tStatisticsResult;
		
	}
	
	/*
	 * Function to calculate F-statistics for two data-sets 
	 * 
	 * fstat = Sum of squares between the samples / Degree of freedom of samples
	 * 		----------------------------------------------------------------------
	 *        Sum of squares within the samples / Degree of freedom of errors
	 */		 
	
	public double calculateFStatistics(double sample1[], double sample2[], double sample3[], double sample4[]){
		double fStatisticsResult = 0.0;
		double sumOfSquaresBetween = 0.0;
		double sumOfSquaresWithin = 0.0;
		double meanOfSquaresBetween = 0.0;
		double meanOfSquaresWithin = 0.0;
		int degreeOfFreedomOfSamples = 0;
		int degreeOfFreedomOfErrors = 0;
		
		List<double[]> samples = new ArrayList<double[]>();
		samples.add(sample1);
		samples.add(sample2);
		samples.add(sample3);
		samples.add(sample4);
		int numberOfSamples = samples.size();
		double overallMeanSum = 0.0;
		double overallMean = 0.0;
		int totalSamples = 0;
		
		for(double[] sample: samples){
			overallMeanSum += calculateMean(sample);
			totalSamples += sample.length;
		}
		overallMean = overallMeanSum/ numberOfSamples;
		degreeOfFreedomOfSamples = numberOfSamples-1;
		degreeOfFreedomOfErrors = totalSamples - numberOfSamples;
		
		double temp1 = 0, temp2 = 0;
		for(double[] sample: samples){
			for(int j = 0; j < sample.length ; j++){
			temp1 = 0;
			// calculate difference between mean of each sample and overallMean
			temp1 = (calculateMean(sample) - overallMean);
			temp1 = Math.pow(temp1, 2);
	
			sumOfSquaresBetween += temp1;
	
			temp2 = 0;
			// calculate difference between each instance of sample and the mean of the sample
			temp2 = sample[j] - calculateMean(sample);
			temp2 = Math.pow(temp2, 2);
			sumOfSquaresWithin += temp2;
			}
		}
		meanOfSquaresBetween = sumOfSquaresBetween/degreeOfFreedomOfSamples;
		meanOfSquaresWithin = sumOfSquaresWithin/degreeOfFreedomOfErrors;
		
		fStatisticsResult = meanOfSquaresBetween/meanOfSquaresWithin;
		
		return fStatisticsResult;
	}
	
	/*
	 * Function to calculate Covariance for two data-sets 
	 * Assumption: Size of both data-sets are same
	 */
	public double calculateCovariance(double sample1[], double sample2[]){
		double covarianceResult = 0.0;
		double mean1 = 0.0;
		double mean2 = 0.0;
		if(sample1.length != sample2.length) {
			System.out.println("Invalid data-sets to calculate covariance/correlation. Data-sets of same size is expected");
			System.exit(0);
		}
		int sampleSize = sample1.length;
		double sumOfampleMeanDiff = 0.0;
		
		mean1 = calculateMean(sample1);
		mean2 = calculateMean(sample2);
		
		for(int i = 0; i < sampleSize; i++){
			sumOfampleMeanDiff += ((sample1[i] - mean1)*(sample2[i] - mean2));
		}
			
		covarianceResult = sumOfampleMeanDiff/(sampleSize-1);
		return covarianceResult;
	}
	
	
	/*
	 * Function to calculate Correlation for two data-sets 
	 */
	public double calculateCorrelation(double sample1[], double sample2[]){
		double correlationResult = 0.0;
		double variance1 = 0.0;
		double variance2 = 0.0;
		double covariance = calculateCovariance(sample1, sample2);
		variance1 = calculateVariance(sample1);
		variance2 = calculateVariance(sample2);
		correlationResult = covariance/(Math.sqrt(variance1 * variance2));
		
		return correlationResult;
	}
	
}
