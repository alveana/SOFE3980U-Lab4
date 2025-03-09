package com.ontariotechu.sofe3980U;

import com.opencsv.CSVReader;

import java.io.FileReader;
import java.io.IOException;

public class ModelEvaluator {
    public static void main(String[] args) {
        String[] modelFiles = {"model_1.csv", "model_2.csv", "model_3.csv"};
        double bestError = Double.MAX_VALUE;
        String bestModel = "";

        for (String modelFile : modelFiles) {
            System.out.println("Evaluating " + modelFile);
            double[] metrics = calculateMetrics(modelFile);
            System.out.printf("MSE: %.6f, MAE: %.6f, MARE: %.6f%%\n\n", metrics[0], metrics[1], metrics[2]);
            
            if (metrics[0] < bestError) { // Using MSE as the primary selection criteria
                bestError = metrics[0];
                bestModel = modelFile;
            }
        }
        System.out.println("The best model with the lowest error is: " + bestModel);
    }

    public static double[] calculateMetrics(String filename) {
        double mse = 0, mae = 0, mare = 0;
        int count = 0;
        double epsilon = 1e-10; // Small number to avoid division by zero
        
        try (CSVReader reader = new CSVReader(new FileReader(filename))) {
            reader.readNext(); // Skip header
            String[] line;
            while ((line = reader.readNext()) != null) {
                double actual = Double.parseDouble(line[0]);
                double predicted = Double.parseDouble(line[1]);
                double error = actual - predicted;
                
                mse += Math.pow(error, 2);
                mae += Math.abs(error);
                mare += Math.abs(error) / (Math.abs(actual) + epsilon);
                count++;
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error reading file " + filename + ": " + e.getMessage());
        }
        
        return new double[]{mse / count, mae / count, (mare / count) * 100};
    }
}

