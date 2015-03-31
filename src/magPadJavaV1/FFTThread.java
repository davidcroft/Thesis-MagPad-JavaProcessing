package magPadJavaV1;

import org.neuroph.core.data.DataSetRow;

import processing.core.*;
import ddf.minim.analysis.*;

public class FFTThread implements Runnable {
	// thread
	private Thread thread;
	private int pauseTime;  // Time to wait between loops
	private PApplet m_parent;
	
	// FFT
	private FFT fftX;
	private FFT fftY;
	private FFT fftZ;
	private float[] fft_ptx;
	private float[] fft_pty;
	private float[] fft_ptz;
	private int fft_size;
	private Pair<Double, Double> m_loc;
	//private int m_fftIndex;
	
	// neural netwrok
	private boolean m_isTrained;
	private final int NORMMAXVAL = 13000;
	
	//String TRAINFILEPATH = "files/test.csv";
	//String TRAINFILEPATH = "files/train.csv";

	public FFTThread(PApplet parent, int pt, int fftSize, int sampleRate, float[] ptX, float[] ptY, float[] ptZ, Pair<Double, Double> location) {
		// pass parameter from parent
		m_parent = parent;
		//m_fftIndex = index;
		m_loc = Pair.createPair(location.getX(), location.getY());
		if (m_loc.getX() == -1 && m_loc.getY() == -1) {
			m_isTrained = true;
		} else {
			m_isTrained = false;
		}
		
		// thread setting
		//m_parent.registerDispose(this);
		pauseTime = pt;
		  
		// init fft
		fftX = new FFT(fftSize, sampleRate);
		fftY = new FFT(fftSize, sampleRate);
		fftZ = new FFT(fftSize, sampleRate);
		fft_ptx = ptX;
		fft_pty = ptY;
		fft_ptz = ptZ;
		fft_size = fftSize;
	}

	public void start() {
		thread = new Thread(this);
		thread.start();
	}

	public void run() {
		// attach a window to fft data
		for (int i = 0; i < fft_size; i++) {
			// attach a window to sample data in order to avoid frequency leakage
			//float window = 0.5 - 0.5*cos(2*3.141593*i/(fft_size-1));
			//float window = 0.35875 + 0.48829*cos(2*3.141593*i/(fft_size-1)) + 0.14128*cos(4*3.141593*i/(fft_size-1)) + 0.01168*cos(6*3.141593*i/(fft_size-1));
			float window = (float) (0.42 - 0.5*Math.cos(2*3.141593*i/(fft_size-1)) - 0.08*Math.cos(4*3.141593*i/(fft_size-1))); 
			fft_ptx[i] = fft_ptx[i] * window;
			fft_pty[i] = fft_pty[i] * window;
			fft_ptz[i] = fft_ptz[i] * window;
		}
	    
		// FFT
		fftX.forward(fft_ptx);
		fftY.forward(fft_pty);
		fftZ.forward(fft_ptz);
	    
		m_parent.background(255);
	    
		// DISPLAY
		//float normRate = ((float)(height-80))/maxVal;
		float normRate = ((float)(m_parent.height/3-50))/NORMMAXVAL;
		//System.out.println("FFT size: "+fft.specSize());
		for(int i = 0; i < fftX.specSize(); i++) {
			int barWidth = m_parent.width / fftX.specSize();
			// draw the line for frequency band i, scaling it up a bit so we can see it
			//line(i, height, i, height - fft.getBand(i)*150 );
			m_parent.rect(i*barWidth, (float)(m_parent.height/3)-fftX.getBand(i)*normRate, barWidth, fftX.getBand(i)*normRate);
			m_parent.rect(i*barWidth, (float)(m_parent.height*2/3)-fftY.getBand(i)*normRate, barWidth, fftY.getBand(i)*normRate);
			m_parent.rect(i*barWidth, (float)(m_parent.height)-fftZ.getBand(i)*normRate, barWidth, fftZ.getBand(i)*normRate);
		}
		
		
		// append data to trainingSet
		if (!m_isTrained) {
			
			// CREATE A FEATURE VECTOR
			
			// add an item in trainingSet for model training
			double[] rowInput = generateFeatureVector();
			double[] rowOutput = new double[GlobalConstants.NNOUTPUTNUM];
			rowOutput[0] = m_loc.getX();
			rowOutput[1] = m_loc.getY();
			
			// insert into training set 
			GlobalConstants.trainingSet.addRow(new DataSetRow(rowInput, rowOutput));
			System.out.println("insert a row in training set, output: X: " + rowOutput[0] + " Y: " + rowOutput[1]);

		} else {
			
			// PREDICTING
			
			// add an item in testingSet for predicting
			double[] rowInput = generateFeatureVector();
			double[] rowOutput = new double[GlobalConstants.NNOUTPUTNUM];
			rowOutput[0] = (double)0;
			rowOutput[1] = (double)0;
			GlobalConstants.testingSet.addRow(rowInput, rowOutput);
			//System.out.println("insert a row in testing set");
		}
	    
		// Wait a little (give other threads time to run)
		try {
			Thread.sleep(pauseTime);
		} catch (InterruptedException e) {}
	}

	public void stop() {
		System.out.println("thread stop");
		thread = null;
	}

	// this will magically be called by the parent once the user hits stop 
	// this functionality hasn't been tested heavily so if it doesn't work, file a bug 
	public void dispose() {
		stop();
	}
	
	
	////////////////////////////////////////////////////////////////////
	// extract FFT spectrum to extract a feature vector
	double[] generateFeatureVector() {
		double[] features = new double[GlobalConstants.NNINPUTNUM];
		
		// raw bands data
		/*int featureIdx = 0;
		int bandIdx = 0;
		for (int i = 0; i < 3; i++) {
			// 3 axis: x, y, z
			for (int j = 0; j < GlobalConstants.NNINPUTNUM/3; j++) {
				if (j == 0) {
					bandIdx = 6;
				} else if (j == 1) {
					bandIdx = 7;
				} else if (j == 2) {
					bandIdx = 10;
				} else if (j == 3) {
					bandIdx = 11;
				} else if (j == 4) {
					bandIdx = 14;
				} else if (j == 5) {
					bandIdx = 15;
				} else if (j == 6) {
					bandIdx = 18;
				} else if (j == 7) {
					bandIdx = 19;
				}
				
				//System.out.println("featureIdex: " + featureIdx + " bandIdx: " + bandIdx);
				if (i == 0) {
					features[featureIdx++] = (double)fftX.getBand(bandIdx)/NORMMAXVAL;
				} else if (i == 1) {
					features[featureIdx++] = (double)fftY.getBand(bandIdx)/NORMMAXVAL;
				} else {
					features[featureIdx++] = (double)fftZ.getBand(bandIdx)/NORMMAXVAL;
				}
			}
		}*/
		
		// raw bands data, capture one band for each magnet
		// 12 features
		int featureIdx = 0;
		int bandIdx = 0;
		for (int i = 0; i < 3; i++) {
			// 3 axis: x, y, z
			for (int j = 0; j < 8; j++) {				
				if (j == 0) {
					bandIdx = 6;
				} else if (j == 1) {
					bandIdx = 7;
				} else if (j == 2) {
					bandIdx = 9;
				} else if (j == 3) {
					bandIdx = 10;
				} else if (j == 4) {
					bandIdx = 14;
				} else if (j == 5) {
					bandIdx = 15;
				} else if (j == 6) {
					bandIdx = 18;
				} else if (j == 7) {
					bandIdx = 19;
				}
				
				//System.out.println("featureIdex: " + featureIdx + " bandIdx: " + bandIdx);
				if (i == 0) {
					features[featureIdx++] = (double)fftX.getBand(bandIdx)/NORMMAXVAL;
				} else if (i == 1) {
					features[featureIdx++] = (double)fftY.getBand(bandIdx)/NORMMAXVAL;
				} else {
					features[featureIdx++] = (double)fftZ.getBand(bandIdx)/NORMMAXVAL;
				}
			}
		}
		
		// Statistical info
		double[] mean = new double[3];
		double[] var  = new double[3];
		double[] maxBand = new double[3];
		double[] maxBandIdx = new double[3];
		double[] kurtosis = new double[3];
		double[][] fftData = new double[3][fft_size/4];
		// init
		mean[0] = mean[1] = mean[2] = 0;
		var[0] = var[1] = var[2] = 0;
		maxBand[0] = maxBand[1] = maxBand[2] = 0;
		maxBandIdx[0] = maxBandIdx[1] = maxBandIdx[2] = 0;
		for (int i = 1; i <= fftData[0].length ; i++) {
			fftData[0][i-1] = (double)fftX.getBand(i)/NORMMAXVAL;		// x
			fftData[1][i-1] = (double)fftY.getBand(i)/NORMMAXVAL;		// y
			fftData[2][i-1] = (double)fftZ.getBand(i)/NORMMAXVAL;		// z
			// mean
			for (int j = 0; j < 3; j++) {
				mean[j] += fftData[j][i-1];
				if(fftData[j][i-1] > maxBand[j]) {
					maxBand[j] = fftData[j][i-1];
					maxBandIdx[j] = i;
				}
			}
		}
		
		// compute mean and add to feature vector
		// 3 features
		for (int i = 0; i < 3; i++) {
			mean[i] /= fftData[0].length;
			features[featureIdx++] = mean[i];
		}
		
		// add maxBandIdx into feature vector
		// 3 features
		for (int i = 0; i < 3; i++) {
			features[featureIdx++] = maxBandIdx[i]/fftData[0].length;
		}
		
		// compute var and add to feature vector
		// 3 features
		for (int i = 0; i < fftData[0].length; i++) {
			for (int j = 0; j < 3; j++) {
				var[j] += Math.pow(fftData[j][i] - mean[j], 2);
				kurtosis[j] += Math.pow(fftData[j][i] - mean[j], 4);
			}
		}
		for (int i = 0; i < 3; i++) {
			var[i] /= fftData[0].length;
			features[featureIdx++] = Math.sqrt(var[i]);
		}
		
		// compute Kurtosis
		// 3 features
		for (int i = 0; i < 3; i++) {
			kurtosis[i] = ((kurtosis[i] / fftData[0].length) / (var[i] * var[i]))/100;
			features[featureIdx++] = kurtosis[i];
		}
		
		// debug
		/*for (int i = 0; i < 36; i++) {
			System.out.println(features[i]);
		}*/
		return features;
	}
}