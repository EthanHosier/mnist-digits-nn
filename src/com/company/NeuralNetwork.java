package com.company;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class NeuralNetwork implements Serializable {

    private int[] dimensions;
    private double learningRate;

    private ArrayList<double[]> activations, dOfLossWRTHiddenActivations, biases, sumOfBiasPDs, zVals ;
    private ArrayList<double[][]> weights, sumOfWeightPDs;

    //label of current image being processed by NN during training.
    private int currentLabel;

    private double[] currentGroundTruths;

    //constructor
    public NeuralNetwork(int[] dimensions, double learningRate){
        this.dimensions = dimensions;
        this.learningRate = learningRate;

        //initialize ArrayLists:

        activations = new ArrayList<>(dimensions.length);
        for (int i = 0; i < dimensions.length; i ++){
            activations.add(new double[dimensions[i]]);
        }


        //All layers in each layer apart from first have biases, thus also have sumOfBiasPDs, and zVals
        biases = new ArrayList<>(dimensions.length - 1);
        zVals = new ArrayList<>(dimensions.length - 1);
        sumOfBiasPDs = new ArrayList<>(dimensions.length - 1);
        //Start at index 1 to skip input layer
        for (int i = 0; i < dimensions.length - 1; i ++){
            biases.add(new double[dimensions[i+1]]);
            zVals.add(new double[dimensions[i+1]]);
            sumOfBiasPDs.add(new double[dimensions [i+1]]);
        }

        //each 2D array is a matrix: each row = 1 layer of weights to particular neuron from all activations from previous layer
        //Num of 'weight layers' = num of neuron layers - 1
        weights = new ArrayList<>(dimensions.length - 1);
        sumOfWeightPDs = new ArrayList<>(dimensions.length - 1);
        for (int i = 0; i < dimensions.length - 1; i ++){
            weights.add(new double[dimensions[i + 1]][dimensions[i]]);
            sumOfWeightPDs.add(new double[dimensions[i + 1]][dimensions[i]]);
        }

        //initialize hiddenDelActivations arrays
        dOfLossWRTHiddenActivations = new ArrayList<>(dimensions.length - 2);
        for (int i = 0; i < dimensions.length - 2; i ++){
            dOfLossWRTHiddenActivations.add(new double[dimensions[i + 1]]);
        }

        currentGroundTruths = new double[activations.get(activations.size() - 1).length];
        initializeAllWeightsAndBiases();
    }

    private void initializeAllWeightsAndBiases() {
        //METHOD ONLY TO BE CALLED AT START
        //set all biases to 0.0
        for (int i = 0; i < biases.size(); i++) {
            for (int j = 0; j < biases.get(i).length; j++) {
                biases.get(i)[j] = 0.0;
            }
        }

        //initialize all weights
        for (int i = 0; i < weights.size(); i++) {
            for (int j = 0; j < weights.get(i).length; j++) {
                for (int k = 0; k < weights.get(i)[j].length; k++) {
                    // +/- sqrt(2/n) where n is the number of nodes in the prior layer (FOR ReLU)
                    weights.get(i)[j][k] = ThreadLocalRandom.current().nextDouble(-2 / Math.sqrt(activations.get(i).length), 2 / Math.sqrt(activations.get(i).length));
                }
            }
        }
    }

    public void feedData(MnistMatrix image){
        //set input activations with values corresponding with image
        //y
        for (int i = 0; i < image.getNumberOfRows(); i ++ ){
            //x
            for (int j = 0; j < image.getNumberOfColumns(); j ++){
                activations.get(0)[i * image.getNumberOfColumns() + j] = image.getValue(i,j) / 255; //we want the val between 0 and 1
            }
        }

        currentLabel = image.getLabel();

        //format currentGroundTruths: 1 for i == label, 0 for other
        for (int i = 0; i < activations.get(activations.size() - 1).length; i ++){
            if (currentLabel == i){
                currentGroundTruths[i] = 1;
            }else{
                currentGroundTruths[i] = 0;
            }
        }

        resetDOfLossWRTHiddenActivations();
    }

    //ReLU(x) = max(0,x)
    private double ReLU(double z){
        return Math.max(0,z);
    }

    //d ReLU(x) w.r.t x = {1 for x > 0; 0 for x <= 0}
    private double getReluDerivative(double x){
        if (x > 0){
            return 1;
        }else{
            return 0;
        }
    }

    private double[] getReLUVector(double[] zVals){
        double[] ReLUVector = new double[zVals.length];
        for (int i = 0; i < zVals.length; i ++){
            ReLUVector[i] = ReLU(zVals[i]);
        }
        return ReLUVector;
    }

    public double getNNLoss(){
        //returns the Cross Entropy Error Loss for the network, for this particular piece of training data

        double loss = 0;

        for (int i = 0; i < activations.get(activations.size() - 1).length; i ++){
            loss += currentGroundTruths[i] * Math.log(activations.get(activations.size() - 1)[i]);
        }

        return -loss;
    }

    private double getIndividualSoftMaxVal(int zi, double[] allZVals){
        //returns softmax value of ONLY zi input
        double denominator = 0;
        for (int j = 0; j < allZVals.length; j ++){
            denominator += Math.exp(allZVals[j]);
        }
        return Math.exp(allZVals[zi]) / denominator;
    }

    private double[] getSoftMaxVector(double[] zVals){
        //returns array of soft max val for all inputted z vals (relative to each other)
        double[] softMaxVector = new double[zVals.length];

        for (int i = 0; i < softMaxVector.length; i++){
            softMaxVector[i] = getIndividualSoftMaxVal(i,zVals);
        }

        return softMaxVector;
    }

    private double[] getWeightedSums(double[][] weights, double[] previousActivations){
        //calculates the dot product of weights[][] applied to previousActivations[], and returns it

        double[] weightedSums = new double[weights.length];
        double tempSum;

        //for each row of weights
        for (int i = 0; i < weights.length; i ++){
            tempSum = 0;
            //for each weight within that row
            for (int j = 0; j < weights[i].length; j ++){
                tempSum += weights[i][j] * previousActivations[j];
            }
            weightedSums[i] = tempSum;
        }

        return weightedSums;
    }

    private double[] getZVals(double[] weightedSums, double[] biases){
        double zVals[] = new double[weightedSums.length];
        for (int i = 0; i < weightedSums.length; i ++){
            zVals[i] = weightedSums[i] + biases[i];
        }
        return zVals;
    }

    public void forwardPropagate(){
        //forward propagates ONCE, from (already set) input to output neurons

        double tempWeightedSums[];
        double tempZVals[];
        for (int i = 0; i < activations.size() - 1; i ++){
            tempWeightedSums = getWeightedSums(weights.get(i),activations.get(i));
            tempZVals = getZVals(tempWeightedSums,biases.get(i));
            zVals.set(i,tempZVals);

            //if output layer, softMax function
            if (i == activations.size() - 2){
                activations.set(i + 1,getSoftMaxVector(tempZVals));
            }else{
                //if not, ReLU
                activations.set(i + 1, getReLUVector(tempZVals));
            }

        }

    }

    public void resetDOfLossWRTHiddenActivations(){
        for (int i = 0; i < dOfLossWRTHiddenActivations.size(); i ++){
            for (int j = 0; j < dOfLossWRTHiddenActivations.get(i).length; j ++){
                dOfLossWRTHiddenActivations.get(i)[j] = 0.0;
            }
        }
    }

    public void backpropogate(){

        //backpropagate through NN ONCE, setting derivatives

        backpropagateOutputLayer();
        backPropagateHiddenLayers();

    }


    private void backpropagateOutputLayer(){
        double tempBiasPD;

        //for each neuron in output layer
        for (int i = 0; i < activations.get(activations.size() - 1).length; i ++){

            tempBiasPD = activations.get(activations.size() - 1)[i] - currentGroundTruths[i];
            sumOfBiasPDs.get(sumOfBiasPDs.size() - 1)[i] += tempBiasPD;

            //for each weight which comes to this neuron from previous layer
            for (int j = 0; j < weights.get(weights.size()-1)[i].length; j ++){
                sumOfWeightPDs.get(sumOfWeightPDs.size()-1)[i][j] += tempBiasPD * activations.get(activations.size() - 2)[j];

                dOfLossWRTHiddenActivations.get(dOfLossWRTHiddenActivations.size() - 1)[j] += tempBiasPD * weights.get(weights.size() - 1)[i][j];
            }
        }

    }

    private void backPropagateHiddenLayers(){
        double tempBiasPD;
        //for every hidden layer
        for (int i = activations.size() - 2; i > 0; i --){
            //for each neuron in this hidden layer

            for (int j = 0; j < activations.get(i).length; j ++){

                tempBiasPD = getReluDerivative(zVals.get(i - 1)[j]) * dOfLossWRTHiddenActivations.get(i-1)[j];
                sumOfBiasPDs.get(i - 1)[j] += tempBiasPD;

                //for each weight which comes to this neuron from previous layer
                for (int k = 0; k < weights.get(i - 1)[j].length; k++){
                    sumOfWeightPDs.get(i - 1)[j][k] += tempBiasPD * activations.get(i - 1)[k];

                    //only do this if not on final hidden layer
                    if (i > 1){
                        dOfLossWRTHiddenActivations.get(i - 2)[k] += tempBiasPD * weights.get(i-1)[j][k];
                    }

                }

            }
        }
    }

    public void resetSumOfBiasesAndWeightsPDs(){
        //sets vals of all these to 0.0
        //to be used at start of new BATCH of training data

        //reset all sumOfBiasPDs
        for(int i = 0; i < sumOfBiasPDs.size(); i ++){
            for (int j = 0; j < sumOfBiasPDs.get(i).length; j ++){
                sumOfBiasPDs.get(i)[j] = 0.0;
            }
        }

        //reset all sumOfWeightPDs
        for (int i = 0; i <  sumOfWeightPDs.size(); i ++){
            for (int j = 0; j <  sumOfWeightPDs.get(i).length; j ++){
                for (int k = 0; k <  sumOfWeightPDs.get(i)[j].length; k ++){

                    sumOfWeightPDs.get(i)[j][k] = 0.0;
                }
            }
        }

    }

    public void setNewWeightsAndBiases(int batchSize){
        //updates the new weights and biases accordingly (through gradient descent)



        //set all new weight vals
        for (int i = 0; i < weights.size(); i ++){
            for (int j = 0; j < weights.get(i).length; j ++){
                for (int k = 0; k < weights.get(i)[j].length; k ++){
                    weights.get(i)[j][k] -= learningRate * 1/batchSize * sumOfWeightPDs.get(i)[j][k];
                }
            }
        }


        //set all new bias vals
        for(int i = 0; i < biases.size(); i ++){
            for (int j = 0; j < biases.get(i).length; j ++){
                biases.get(i)[j] -= learningRate * 1/batchSize * sumOfBiasPDs.get(i)[j];
            }
        }


    }
    public int getAnswer(){
        //returns value of brightest activation neuron (highest val)

        int brightestNeuron = 0;

        for (int i = 1; i < activations.get(activations.size() -1).length; i ++){
            if (activations.get(activations.size() - 1)[i] > activations.get(activations.size() - 1)[brightestNeuron]){
                brightestNeuron = i;
            }
        }
        return brightestNeuron;
    }

    public void train(int numOfEpochs, MnistMatrix[] allImages){

        int totalTestSize = 50000;
        int batchSize = 500;
        double loss;
        int numCorrect;
        int indexOn;
        int epochs = numOfEpochs;
        for(int e = 0; e < epochs; e ++){

            for (int i = 0; i < totalTestSize / batchSize; i ++) {
                loss = 0;
                numCorrect = 0;
                for (int j = 0; j < batchSize; j++) {
                    indexOn = i * batchSize + j;
                    feedData(allImages[indexOn]);
                    forwardPropagate();
                    backpropogate();
                    loss += getNNLoss();
                    if (getAnswer() == allImages[indexOn].getLabel()) {
                        numCorrect += 1;
                    }
                    resetDOfLossWRTHiddenActivations();
                }

                setNewWeightsAndBiases(batchSize);
                System.out.println("Batch: " + ((e * (totalTestSize/batchSize) + i) + 1) + "/" + epochs * (totalTestSize/batchSize) + "     % Correct: " + ((double) numCorrect / batchSize) * 100 + "     Avg Loss: " + loss / batchSize);
                resetSumOfBiasesAndWeightsPDs();
            }
        }

        FileHandler.saveNetwork(this,"res/NNSaveFile15Epochs.txt");

    }

    public void test(MnistMatrix[] testImages){
        //tests array of images
        int numCorrect = 0;
        for (int i = 0; i < testImages.length; i ++){
            feedData(testImages[i]);
            forwardPropagate();
            System.out.print("Image " + (i + 1) + ": ");
            if (getAnswer() == testImages[i].getLabel()){
                System.out.print("Correct!");
                numCorrect += 1;
            }else{
                System.out.print("Incorrect!");
            }
            System.out.print("    % Correct so far: " + (((double)numCorrect / (i + 1)) * 100));
            System.out.println();
        }
    }

    public double[] getAllOutputs(){
        return activations.get(activations.size() - 1);
    }


}
