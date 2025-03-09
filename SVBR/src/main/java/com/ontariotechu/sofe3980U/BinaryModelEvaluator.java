package com.ontariotechu.sofe3980U;

import com.opencsv.CSVReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class BinaryModelEvaluator {
    private static final double THRESHOLD = 0.5;
    private static final int ROC_POINTS = 100;
    
    public static void main(String[] args) {
        String[] modelFiles = {"model_1.csv", "model_2.csv", "model_3.csv"};
        double bestAUC = 0;
        String bestModel = "";

        for (String modelFile : modelFiles) {
            System.out.println("Evaluating " + modelFile);
            double[] metrics = evaluateModel(modelFile);
            System.out.printf("BCE: %.6f, Accuracy: %.6f, Precision: %.6f, Recall: %.6f, F1 Score: %.6f, AUC: %.6f\n\n", 
                              metrics[0], metrics[1], metrics[2], metrics[3], metrics[4], metrics[5]);
            
            if (metrics[5] > bestAUC) { // Using AUC as the primary selection criteria
                bestAUC = metrics[5];
                bestModel = modelFile;
            }
        }
        System.out.println("The best model with the highest AUC-ROC is: " + bestModel);
    }

    public static double[] evaluateModel(String filename) {
        int tp = 0, tn = 0, fp = 0, fn = 0;
        double bce = 0;
        int count = 0;
        int nPositive = 0, nNegative = 0;
        double[] trueLabels, predictedScores;
        
        try{CSVReader reader = new CSVReader(new FileReader(filename));
            reader.readNext(); // Skip header
            String[] line;
            int index = 0;
            
            // Count rows first to initialize arrays
            while ((line = reader.readNext()) != null) count++;
            
            if (count == 0) {
                System.err.println("Error: The CSV file is empty or only contains headers.");
                return new double[]{0, 0, 0, 0, 0, 0};
            }
            
            trueLabels = new double[count];
            predictedScores = new double[count];
            
            reader.close();
            reader = new CSVReader(new FileReader(filename)); // Reopen file
            reader.readNext(); // Skip header again
            
            while ((line = reader.readNext()) != null) {
                double actual = Double.parseDouble(line[0]);
                double predicted = Double.parseDouble(line[1]);
                
                trueLabels[index] = actual;
                predictedScores[index] = predicted;
                
                // BCE Calculation
                bce += actual * Math.log(predicted + 1e-10) + (1 - actual) * Math.log(1 - predicted + 1e-10);
                
                // Count positives and negatives
                if (actual == 1) nPositive++;
                else nNegative++;
                
                // Confusion matrix based on threshold
                if (predicted >= THRESHOLD) {
                    if (actual == 1) tp++;
                    else fp++;
                } else {
                    if (actual == 1) fn++;
                    else tn++;
                }
                index++;
            }
            reader.close();
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error reading file " + filename + ": " + e.getMessage());
            return new double[]{0, 0, 0, 0, 0, 0};
        }
        
        // Compute metrics
        bce = -bce / count;
        double accuracy = (double) (tp + tn) / count;
        double precision = tp + fp > 0 ? (double) tp / (tp + fp) : 0;
        double recall = tp + fn > 0 ? (double) tp / (tp + fn) : 0;
        double f1Score = precision + recall > 0 ? 2 * (precision * recall) / (precision + recall) : 0;
        double auc = computeAUC(trueLabels, predictedScores, nPositive, nNegative);
        
        return new double[]{bce, accuracy, precision, recall, f1Score, auc};
    }
    
    private static double computeAUC(double[] trueLabels, double[] predictedScores, int nPositive, int nNegative) {
        double[] x = new double[ROC_POINTS + 1];
        double[] y = new double[ROC_POINTS + 1];
        
        for (int i = 0; i <= ROC_POINTS; i++) {
            double th = i / 100.0;
            int tp = 0, fp = 0;
            
            for (int j = 0; j < trueLabels.length; j++) {
                if (predictedScores[j] >= th) {
                    if (trueLabels[j] == 1) tp++;
                    else fp++;
                }
            }
            x[i] = (double) fp / Math.max(1, nNegative);
            y[i] = (double) tp / Math.max(1, nPositive);
        }
        
        double auc = 0;
        for (int i = 1; i <= ROC_POINTS; i++) {
            auc += (y[i - 1] + y[i]) * Math.abs(x[i - 1] - x[i]) / 2;
        }
        return auc;
    }
}
