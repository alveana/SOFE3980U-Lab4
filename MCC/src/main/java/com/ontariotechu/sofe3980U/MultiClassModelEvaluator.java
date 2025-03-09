package com.ontariotechu.sofe3980U;

import com.opencsv.CSVReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class MultiClassModelEvaluator {
    public static void main(String[] args) {
        String modelFile = "model.csv";
        System.out.println("Evaluating " + modelFile);
        evaluateModel(modelFile);
    }

    public static void evaluateModel(String filename) {
        int numClasses = 5; // Assuming classes are {1,2,3,4,5}
        int[][] confusionMatrix = new int[numClasses][numClasses];
        double crossEntropy = 0;
        int count = 0;
        
        try (CSVReader reader = new CSVReader(new FileReader(filename))) {
            reader.readNext(); // Skip header
            String[] line;
            
            while ((line = reader.readNext()) != null) {
                int actualClass = Integer.parseInt(line[0]) - 1; // Convert 1-based to 0-based index
                double[] probabilities = new double[numClasses];
                
                for (int i = 0; i < numClasses; i++) {
                    probabilities[i] = Double.parseDouble(line[i + 1]);
                }
                
                int predictedClass = argMax(probabilities);
                confusionMatrix[actualClass][predictedClass]++;
                crossEntropy += Math.log(probabilities[actualClass] + 1e-10);
                count++;
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error reading file " + filename + ": " + e.getMessage());
            return;
        }
        
        crossEntropy = -crossEntropy / count;
        System.out.println("Cross Entropy: " + crossEntropy);
        printConfusionMatrix(confusionMatrix);
    }

    private static int argMax(double[] probabilities) {
        int maxIndex = 0;
        for (int i = 1; i < probabilities.length; i++) {
            if (probabilities[i] > probabilities[maxIndex]) {
                maxIndex = i;
            }
        }
        return maxIndex;
    }
    
    private static void printConfusionMatrix(int[][] matrix) {
        System.out.println("Confusion Matrix:");
        for (int[] row : matrix) {
            for (int value : row) {
                System.out.print(value + " ");
            }
            System.out.println();
        }
    }
}
