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

	// FirFilter
	public FirFilter firFilterTLX, firFilterTLY, firFilterTLZ;
	public FirFilter firFilterTRX, firFilterTRY, firFilterTRZ;

	// Thread
	public FFTThread fftThread;
	
	// neural network
	public MultiLayerPerceptronNN nnet;
	private boolean isTrained = false;
	public LocationRecognition locRecog;
	

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
		  
		// FIR filter
		firFilterTLX = new FirFilter("FirFilterTL.fcf");
		firFilterTLY = new FirFilter("FirFilterTL.fcf");
		firFilterTLZ = new FirFilter("FirFilterTL.fcf");
		firFilterTRX = new FirFilter("FirFilterTR.fcf");
		firFilterTRY = new FirFilter("FirFilterTR.fcf");
		firFilterTRZ = new FirFilter("FirFilterTR.fcf");
		
		// neural network
		nnet = new MultiLayerPerceptronNN("myModel.nnet", GlobalConstants.NNINPUTNUM, 18, GlobalConstants.NNOUTPUTNUM);
		isTrained = nnet.getIsTrained();
		System.out.println("isTrained = "+(isTrained?"true":"false"));
		System.out.println("create a new neural network");
		
		// location recognition
		locRecog = new LocationRecognition(2);

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
	    
		    float filterTL = (float)firFilterTLX.filter((double)theOscMessage.get(i*3+0).floatValue());
		    float filterTR = (float)firFilterTRX.filter((double)theOscMessage.get(i*3+0).floatValue());
		    bufX[i] = filterTL + (filterTR - filterTL)/2;
	    
		    filterTL = (float)firFilterTLY.filter((double)theOscMessage.get(i*3+1).floatValue());
		    filterTR = (float)firFilterTRY.filter((double)theOscMessage.get(i*3+1).floatValue());
		    bufY[i] = filterTL + (filterTR - filterTL)/2;
	    
		    filterTL = (float)firFilterTLZ.filter((double)theOscMessage.get(i*3+2).floatValue());
		    filterTR = (float)firFilterTRZ.filter((double)theOscMessage.get(i*3+2).floatValue());
		    bufZ[i] = filterTL + (filterTR - filterTL)/2;
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
		    bufferFFT(dataX, dataY, dataZ, -1);
		    
		    // predict
		    if (!GlobalConstants.testingSet.isEmpty()) {
		    	DataSetRow testDataRow = GlobalConstants.testingSet.getRowAt(0);
		    	DataSet testSet = new DataSet(GlobalConstants.NNINPUTNUM, GlobalConstants.NNOUTPUTNUM);
		    	testSet.addRow(testDataRow);
		    	GlobalConstants.testingSet.removeRowAt(0);
		    	double predictLoc = nnet.testNeuralNetwork(testSet);
		    	// send to locationRecognition
		    	//System.out.println("new prediction");
		    	if (locRecog.addToRecog(predictLoc)) {
		    		// robust location
		    		double location = locRecog.getDetectLocation();
		    		System.out.println("get a robust new location " + location);
		    		
		    		// send a OSC message
		    		OscMessage myMessage = new OscMessage("/loc");
		    		myMessage.add((float)location); 
		    		oscP5.send(myMessage, remoteLocation); 
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
			    bufferFFT(dataX, dataY, dataZ, fftIndex);
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
		    	// train model
		    	System.out.println("train NN model...");
		    	nnet.trainModel(GlobalConstants.trainingSet);
		    	System.out.println("NN model trained finished");
		    	// save training set to local file
		    	GlobalConstants.trainingSet.save(Paths.get("files", "trainingSet").toAbsolutePath().toString());
		    	// set isTrained and fftIndex
		    	isTrained = true;
		    	System.out.println("set isTrained to true");
		    }
 	    }
	}

	void bufferFFT(float[] fftDataX, float[] fftDataY, float[] fftDataZ, int index) {
		fftThread = new FFTThread(this, 10, GlobalConstants.BUFFERSIZE * Buffer.BUFFERNUM, GlobalConstants.SAMPLERATE, fftDataX, fftDataY, fftDataZ, index);
		fftThread.start();
	}
	
	public static void main(String args[]) {
	    PApplet.main(new String[] { "--present", "magPadJavaV1.ProcessingSketch" });
	}
}
