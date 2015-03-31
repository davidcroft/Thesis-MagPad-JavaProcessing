package magPadJavaV1;

import java.nio.file.Paths;

import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;

import processing.core.*;
import oscP5.*;
import netP5.*;

public class ProcessingSketch extends PApplet {
	private static final long serialVersionUID = 1L;
	
	private int fftCnt = 0;
	private int fftIndex = 0;
	private boolean startFFT = false;
	
	// OSC
	public OscP5 oscP5;
	public NetAddress remoteLocation;

	// Buffers
	public Buffer magDataBuf;
	
	// Fir filters group
	public FIRFilterGroup filterGroup;

	// Thread
	public FFTThread fftThread;
	
	// neural network
	public MultiLayerPerceptronNN nnet;
	private boolean isTrained = false;
	private TrainingHashTable trainTable;
	
	// location prediction result
	public LocationRecognition locRecogX;
	public LocationRecognition locRecogY;
	
	// debug use: retrain model with training data set
	public static DataSet train = new DataSet(GlobalConstants.NNINPUTNUM, GlobalConstants.NNOUTPUTNUM);

	public void setup() {
		// OSC
		// start oscP5, telling it to listen for incoming messages at port 5001 */
		OscProperties properties = new OscProperties();
		properties.setListeningPort(GlobalConstants.RECVPORT);
		properties.setDatagramSize(102400);
		oscP5 = new OscP5(this,properties);
		// set the remote location to be the localhost on port 3001
		remoteLocation = new NetAddress(GlobalConstants.SENDHOST,GlobalConstants.SENDPORT);
		  
		// MagBuffer
		// init buffers
		magDataBuf = new Buffer(GlobalConstants.BUFFERSIZE);
		
		// init fir filters
		filterGroup = new FIRFilterGroup();
		
		// neural network
		nnet = new MultiLayerPerceptronNN("myModel.nnet", GlobalConstants.NNINPUTNUM, 24, GlobalConstants.NNOUTPUTNUM);
		isTrained = nnet.getIsTrained();
		System.out.println("isTrained = "+(isTrained?"true":"false"));
		System.out.println("create a new neural network");
		
		// debug use: retrain model
		/*train = DataSet.load(Paths.get("files", "trainingSet").toAbsolutePath().toString());
		System.out.println("trainSet size: " + train.size());
		//train.saveAsTxt(Paths.get("files", "trainingSetTxt").toAbsolutePath().toString(), ",");
		System.out.println("Train model 1");
		nnet.trainModel(train);
		System.out.println("NN model trained finished 1");
		isTrained = true;*/
		/////////////////////////////
		
		// init hashtable
		trainTable = new TrainingHashTable();
		
		// location recognition
		locRecogX = new LocationRecognition(2);
		locRecogY = new LocationRecognition(2);

	    size(800,600);
	}

	public void draw() {
	}
	
	public void mousePressed() {
		if (mouseButton == LEFT) {
			// start doing fft
			fftIndex++;
			if (fftIndex <= GlobalConstants.TRAININGPOSNUM) {
				startFFT = true;
				System.out.println("start FFT at position "+fftIndex);
			} else {
				startFFT = false;
			}
			fftCnt = 0;
		} else if (mouseButton == RIGHT) {
		    // stop doing fft
			startFFT = false;
			System.out.println("stop FFT");
		}
	}
	
	void oscEvent(OscMessage theOscMessage) {  
		// get the first value as an integer
		//String tag = theOscMessage.get(0).stringValue();
		//get x, y, z values as a float  
		//System.out.println("receive osc message");
		
	    float[] bufX = new float[GlobalConstants.BUFFERSIZE];
	    float[] bufY = new float[GlobalConstants.BUFFERSIZE];
	    float[] bufZ = new float[GlobalConstants.BUFFERSIZE];
	  
	    for (int i = 0; i < GlobalConstants.BUFFERSIZE; i++) {
		    /*bufX[i] = theOscMessage.get(i*3+0).floatValue();
	    	bufY[i] = theOscMessage.get(i*3+1).floatValue();
	    	bufZ[i] = theOscMessage.get(i*3+2).floatValue();*/
	    	bufX[i] = filterGroup.applyToFilter(theOscMessage.get(i*3+0).floatValue(), 0);
	    	bufY[i] = filterGroup.applyToFilter(theOscMessage.get(i*3+1).floatValue(), 1);
	    	bufZ[i] = filterGroup.applyToFilter(theOscMessage.get(i*3+2).floatValue(), 2);
	    }
	    
	    magDataBuf.addToBuffer(bufX, bufY, bufZ);
	  
	    // identify if model has been trained
	    if (isTrained) {
		    // TESTING
		    // axis par: 1 for x axis, 2 for y axis and 3 for z axis
		    float[] dataX = magDataBuf.genBufferForFFT(magDataBuf.m_bufferIndex, 1);
		    float[] dataY = magDataBuf.genBufferForFFT(magDataBuf.m_bufferIndex, 2);
		    float[] dataZ = magDataBuf.genBufferForFFT(magDataBuf.m_bufferIndex, 3);
		    // fftIndex: -1 testing, >=0 training (and fftIndex used for identify training index)
		    Pair<Double, Double> loc = Pair.createPair(-1.0, -1.0);
		    bufferFFT(dataX, dataY, dataZ, loc);
		    
		    // predict
		    if (!GlobalConstants.testingSet.isEmpty()) {
		    	DataSetRow testDataRow = GlobalConstants.testingSet.getRowAt(0);
		    	DataSet testSet = new DataSet(GlobalConstants.NNINPUTNUM, GlobalConstants.NNOUTPUTNUM);
		    	testSet.addRow(testDataRow);
		    	GlobalConstants.testingSet.removeRowAt(0);
		    	double[] predictLoc = nnet.testNeuralNetwork(testSet);
		    	
		    	// normalize location
		    	//predictLoc[0] = predictLoc[0] * GlobalConstants.MAXHEIGHT;
		    	//predictLoc[1] = predictLoc[1] * GlobalConstants.MAXWIDTH;
		    	System.out.println("predict location: " + predictLoc[0] + " " + predictLoc[1]);
		    	
		    	// send to locationRecognition
		    	//System.out.println("new prediction");
		    	if (locRecogX.addToRecog(predictLoc[0]) && locRecogY.addToRecog(predictLoc[1])) {
		    		// robust location
		    		double locX = locRecogX.getDetectLocation();
		    		double locY = locRecogY.getDetectLocation();
		    		System.out.println("get a robust new location: X-" + locX + " Y-" + locY);
		    		
		    		// send a OSC message
		    		//OscMessage myMessage = new OscMessage("/loc");
		    		//myMessage.add((float)location); 
		    		//oscP5.send(myMessage, remoteLocation); 
		    	}
		    }
	    } else {
		    // TRAINING
		    // axis par: 1 for x axis, 2 for y axis and 3 for z axis
		    // begin fft after left click mouse
		    if (startFFT) {
			    float[] dataX = magDataBuf.genBufferForFFT(magDataBuf.m_bufferIndex, 1);
			    float[] dataY = magDataBuf.genBufferForFFT(magDataBuf.m_bufferIndex, 2);
			    float[] dataZ = magDataBuf.genBufferForFFT(magDataBuf.m_bufferIndex, 3);
			    Pair<Double, Double> loc = Pair.createPair(trainTable.get(fftIndex).getX(), trainTable.get(fftIndex).getY());
			    bufferFFT(dataX, dataY, dataZ, loc);
			    if (fftCnt++ >= GlobalConstants.FFTFOREACHPOS) {
				    // stop fft
				    startFFT = false;
				    System.out.println("Pos " + fftIndex + " FFT collection finished!");
			    }
		    }
		    ////////////////////////////////////////
		    // train model in interrupt handler, might have some problem
		    if (fftIndex > GlobalConstants.TRAININGPOSNUM) {
		    	fftIndex = -1;
		    	// save training set to local file
		    	GlobalConstants.trainingSet.save(Paths.get("files", "trainingSet").toAbsolutePath().toString());
		    	GlobalConstants.trainingSet.saveAsTxt(Paths.get("files", "trainingSetTxt").toAbsolutePath().toString(), " ");
		    	// train model
		    	System.out.println("train NN model...");
		    	nnet.trainModel(GlobalConstants.trainingSet);
		    	System.out.println("NN model trained finished");
		    	// set isTrained and fftIndex
		    	isTrained = true;
		    	System.out.println("set isTrained to true");
		    }
 	    }
	}

	void bufferFFT(float[] fftDataX, float[] fftDataY, float[] fftDataZ, Pair<Double, Double> location) {
		fftThread = new FFTThread(this, 10, GlobalConstants.BUFFERSIZE * Buffer.BUFFERNUM, GlobalConstants.SAMPLERATE, fftDataX, fftDataY, fftDataZ, location);
		fftThread.start();
	}
	
	public static void main(String args[]) {
	    PApplet.main(new String[] { "--present", "magPadJavaV1.ProcessingSketch" });
	}
}
