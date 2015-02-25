package magPadJavaV1;

import processing.core.*;
import oscP5.*;
import netP5.*;

public class ProcessingSketch extends PApplet {
	private static final long serialVersionUID = 1L;
	
	private String SENDHOST = "";
	private int SENDPORT = 3001;
	private int RECVPORT = 3000;
	private int BUFFERSIZE = 32;
	private int SAMPLERATE = 100;
	
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

	public void setup() {
		// OSC
		// start oscP5, telling it to listen for incoming messages at port 5001 */
		OscProperties properties = new OscProperties();
		properties.setListeningPort(RECVPORT);
		properties.setDatagramSize(102400);
		oscP5 = new OscP5(this,properties);
		// set the remote location to be the localhost on port 3001
		remoteLocation = new NetAddress(SENDHOST,SENDPORT);
		  
		// MagBuffer
		// init buffers
		magDataBuf = new Buffer(BUFFERSIZE);
		  
		// FIR filter
		firFilterTLX = new FirFilter("FirFilterTL.fcf");
		firFilterTLY = new FirFilter("FirFilterTL.fcf");
		firFilterTLZ = new FirFilter("FirFilterTL.fcf");
		firFilterTRX = new FirFilter("FirFilterTR.fcf");
		firFilterTRY = new FirFilter("FirFilterTR.fcf");
		firFilterTRZ = new FirFilter("FirFilterTR.fcf");
		    
	    size(400,400);
	}

	public void draw() {
	}
	
	void oscEvent(OscMessage theOscMessage) 
	{  
	  // get the first value as an integer
	  //String tag = theOscMessage.get(0).stringValue();
	  // get x, y, z values as a float  
	  println("receive osc message");
	  float[] bufX = new float[BUFFERSIZE];
	  float[] bufY = new float[BUFFERSIZE];
	  float[] bufZ = new float[BUFFERSIZE];
	  
	  for (int i = 0; i < BUFFERSIZE; i++) {
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
	  
	  // Buffer FFT
	  // axis par: 1 for x axis, 2 for y axis and 3 for z axis
	  float[] data = magDataBuf.genBufferForFFT(magDataBuf.m_bufferIndex, 1);
	  bufferFFT(data);
	}

	void bufferFFT(float[] fftData) {
		fftThread = new FFTThread(this, 10, BUFFERSIZE * Buffer.BUFFERNUM, SAMPLERATE, fftData);
		fftThread.start();
	}
	
	public static void main(String args[]) {
	    PApplet.main(new String[] { "--present", "magPadJavaV1.ProcessingSketch" });
	}
}
