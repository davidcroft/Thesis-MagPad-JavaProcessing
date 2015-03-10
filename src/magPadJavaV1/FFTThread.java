package magPadJavaV1;

import java.util.HashMap;

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
	private int m_fftIndex;
	
	// neural netwrok
	private boolean m_isTrained;
	private HashMap<Integer,Float> ht;
	private final int NORMTAGVAL = 25;
	private final int NORMMAXVAL = 7000;
	
	//String TRAINFILEPATH = "files/test.csv";
	//String TRAINFILEPATH = "files/train.csv";
	  
	public FFTThread(PApplet parent, int pt, int fftSize, int sampleRate, float[] ptX, float[] ptY, float[] ptZ, int index) {
		// pass parameter from parent
		m_parent = parent;
		m_fftIndex = index;
		if (index == -1) {
			m_isTrained = true;
		} else if (index > 0) {
			m_isTrained = false;
		}  
		  
		// init hashtable
		ht = new HashMap<Integer, Float>();
		/*ht.put(1, ((float)2/NORMTAGVAL));
		ht.put(2, ((float)5/NORMTAGVAL));
		ht.put(3, ((float)8/NORMTAGVAL));
		ht.put(4, ((float)11/NORMTAGVAL));
		ht.put(5, ((float)14/NORMTAGVAL));
		ht.put(6, ((float)17/NORMTAGVAL));
		ht.put(7, ((float)20/NORMTAGVAL));
		ht.put(8, ((float)23/NORMTAGVAL));*/
		  
		/*ht.put(1, ((float)4/NORMTAGVAL));
		ht.put(2, ((float)7/NORMTAGVAL));
		ht.put(3, ((float)9/NORMTAGVAL));
		ht.put(4, ((float)10/NORMTAGVAL));
		ht.put(5, ((float)15/NORMTAGVAL));
		ht.put(6, ((float)18/NORMTAGVAL));
		ht.put(7, ((float)22/NORMTAGVAL));*/
		
		ht.put(1, ((float)3/NORMTAGVAL));
		ht.put(2, ((float)5/NORMTAGVAL));
		ht.put(3, ((float)7/NORMTAGVAL));
		ht.put(4, ((float)9/NORMTAGVAL));
		ht.put(5, ((float)11/NORMTAGVAL));
		ht.put(6, ((float)13/NORMTAGVAL));
		ht.put(7, ((float)15/NORMTAGVAL));
		ht.put(8, ((float)17/NORMTAGVAL));
		  
		  
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
		// normalization
	    
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
			// add an item in trainingSet for model training
			double[] rowInput = new double[GlobalConstants.NNINPUTNUM];
			double[] rowOutput = new double[GlobalConstants.NNOUTPUTNUM];
			  
			int index = 0;
			for (int i = 0; i < 4; i++) {
				if (i == 0) {
					rowInput[index++] = (double)fftX.getBand(3)/NORMMAXVAL;
				} else if (i == 1) {
					rowInput[index++] = (double)fftX.getBand(4)/NORMMAXVAL;
				} else if (i == 2) {
					rowInput[index++] = (double)fftX.getBand(6)/NORMMAXVAL;
				} else if (i == 3) {
					rowInput[index++] = (double)fftX.getBand(7)/NORMMAXVAL;
				}
			}
			for (int i = 0; i < 4; i++) {
				if (i == 0) {
					rowInput[index++] = (double)fftY.getBand(3)/NORMMAXVAL;
				} else if (i == 1) {
					rowInput[index++] = (double)fftY.getBand(4)/NORMMAXVAL;
				} else if (i == 2) {
					rowInput[index++] = (double)fftY.getBand(6)/NORMMAXVAL;
				} else if (i == 3) {
					rowInput[index++] = (double)fftY.getBand(7)/NORMMAXVAL;
				}
			}
			for (int i = 0; i < 4; i++) {
				if (i == 0) {
					rowInput[index++] = (double)fftZ.getBand(3)/NORMMAXVAL;
				} else if (i == 1) {
					rowInput[index++] = (double)fftZ.getBand(4)/NORMMAXVAL;
				} else if (i == 2) {
					rowInput[index++] = (double)fftZ.getBand(6)/NORMMAXVAL;
				} else if (i == 3) {
					rowInput[index++] = (double)fftZ.getBand(7)/NORMMAXVAL;
				}
			}
			rowOutput[0] = (double)ht.get(m_fftIndex);
			// insert into training set 
			GlobalConstants.trainingSet.addRow(new DataSetRow(rowInput, rowOutput));
			System.out.println("insert a row in training set, output: "+rowOutput[0]);
			  
			/*try {
				FileWriter writer = new FileWriter(TRAINFILEPATH, true);
				if (writer != null) {
					for (int i = 0; i < 12; i++) {
						writer.append(Float.toString((float)fft.getBand(i)/NORMMAXVAL));
						writer.append(',');
					}
					// write tag
					writer.append(Float.toString(ht.get(m_fftIndex)));
					writer.append('\n');
					writer.flush();
					writer.close();
				}
				System.out.println("append to the end of training file");
			} catch (IOException e) {}*/
		} else {
			// testing
			/*if (!GlobalConstants.testingSet.isEmpty()) {
				GlobalConstants.testingSet.clear();
			}*/
			// add an item in testingSet for predicting
			double[] rowInput = new double[GlobalConstants.NNINPUTNUM];
			double[] rowOutput = new double[GlobalConstants.NNOUTPUTNUM];
			int index = 0;
			for (int i = 0; i < 4; i++) {
				if (i == 0) {
					rowInput[index++] = (double)fftX.getBand(3)/NORMMAXVAL;
				} else if (i == 1) {
					rowInput[index++] = (double)fftX.getBand(4)/NORMMAXVAL;
				} else if (i == 2) {
					rowInput[index++] = (double)fftX.getBand(6)/NORMMAXVAL;
				} else if (i == 3) {
					rowInput[index++] = (double)fftX.getBand(7)/NORMMAXVAL;
				}
			}
			for (int i = 0; i < 4; i++) {
				if (i == 0) {
					rowInput[index++] = (double)fftY.getBand(3)/NORMMAXVAL;
				} else if (i == 1) {
					rowInput[index++] = (double)fftY.getBand(4)/NORMMAXVAL;
				} else if (i == 2) {
					rowInput[index++] = (double)fftY.getBand(6)/NORMMAXVAL;
				} else if (i == 3) {
					rowInput[index++] = (double)fftY.getBand(7)/NORMMAXVAL;
				}
			}
			for (int i = 0; i < 4; i++) {
				if (i == 0) {
					rowInput[index++] = (double)fftZ.getBand(3)/NORMMAXVAL;
				} else if (i == 1) {
					rowInput[index++] = (double)fftZ.getBand(4)/NORMMAXVAL;
				} else if (i == 2) {
					rowInput[index++] = (double)fftZ.getBand(6)/NORMMAXVAL;
				} else if (i == 3) {
					rowInput[index++] = (double)fftZ.getBand(7)/NORMMAXVAL;
				}
			}
			rowOutput[0] = (double)0;
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
}