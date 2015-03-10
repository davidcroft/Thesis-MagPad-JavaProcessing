package magPadJavaV1;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.*;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.BackPropagation;
import org.neuroph.util.TransferFunctionType;

public class MultiLayerPerceptronNN { 
	private static NeuralNetwork<BackPropagation> nnet = new NeuralNetwork<BackPropagation>();
	private String modelPath;
	private MultiLayerPerceptron myMlPerceptron;
	private boolean isTrained = false;
	
	public MultiLayerPerceptronNN(String fileName, int inputNum, int middleNum, int outputNum) {
		// load trained neural network saved with Neuroph Studio
		System.out.println("filename: "+fileName);
		modelPath = Paths.get("files", fileName).toAbsolutePath().toString();
		
		// check if model has been trained in the system
		File f = new File(modelPath);
		if(f.exists() && !f.isDirectory()) {
			// load model
			nnet = NeuralNetwork.createFromFile(modelPath);
			// set isTrained
			isTrained = true;
			// test loaded neural network
			System.out.println("load neural network, set isTrained");	
		} else {
			// create multi layer perceptron
			List<Integer> list = new ArrayList<>();
			list.add(inputNum);
			list.add(middleNum);
			list.add(6);
			list.add(outputNum);
			myMlPerceptron = new MultiLayerPerceptron(list, TransferFunctionType.SIGMOID);
			//myMlPerceptron.getLearningRule().setNeuralNetwork(nnet);
			BackPropagation learningRule = new BackPropagation();
			learningRule.setLearningRate(0.3);
			learningRule.setMaxError(0.00001);
			learningRule.setMaxIterations(50000);
			myMlPerceptron.setLearningRule(learningRule);
			//myMlPerceptron.getLearningRule().setLearningRate(0.3);
			//myMlPerceptron.getLearningRule().setMaxIterations(20000);
			// set isTrained
			isTrained = false;
			System.out.println("create a new neural network, set isTrained");
		}
		
		/*DataSet testingSet = new DataSet(12, 1);
		double[] input = new double[]{0.005333011, 0.0089612305, 0.0053863763, 0.061282232, 0.07752936, 
				0.02682797, 0.024474371, 0.019980587, 0.008886063, 0.0029110478, 0.0013217365, 0.0010026528 };
		double[] output = new double[]{0.16};
		testingSet.addElement(new SupervisedTrainingElement(input, output));
		System.out.println("Testing loaded neural network");
		testNeuralNetwork(nnet, testingSet);*/
	}
	
	public boolean getIsTrained() {
		return isTrained;
	}
	
	public void trainModel(DataSet trainingSet) {
		// learn the training set
		myMlPerceptron.learn(trainingSet);

		// save trained neural network
		myMlPerceptron.save(modelPath);

		// load saved neural network
		nnet = NeuralNetwork.createFromFile(modelPath);
		
		// set isTrained
		isTrained = true;

		// test loaded neural network
		System.out.println("Testing loaded neural network");
		testNeuralNetwork(trainingSet);
		
	}

	public double testNeuralNetwork(DataSet testSet) {
		if (isTrained) {
			//System.out.println("testNeuralNetwork");
			/*for(DataSetRow dataRow : testSet.getRows()) {
				nnet.setInput(dataRow.getInput());
				nnet.calculate();
				double[] networkOutput = nnet.getOutput();
				//System.out.println("Input: " + Arrays.toString(dataRow.getInput()) );
				//System.out.println(" Output: " + Arrays.toString(networkOutput));
				System.out.println(" Output: " + networkOutput[0]*25);
			}*/
			
			DataSetRow dataRow = testSet.getRowAt(0);
			nnet.setInput(dataRow.getInput());
			nnet.calculate();
			double[] networkOutput = nnet.getOutput();
			double result = networkOutput[0]*GlobalConstants.MAXWIDTH;
			//System.out.println(" Output: " + result);
			return result;
		} else {
			return -1;
		}
	}
}
