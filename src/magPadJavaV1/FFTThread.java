package magPadJavaV1;

import processing.core.*;
import ddf.minim.analysis.*;

public class FFTThread implements Runnable {
	  Thread thread;
	  PApplet m_parent;
	  int pauseTime;  // Time to wait between loops
	  FFT fft;
	  float[] fft_ptx;
	  int fft_size;
	  
	  int NORMMAXVAL = 7000;
	  
	  public FFTThread(PApplet parent, int pt, int fftSize, int sampleRate, float[] ptX) {
		  m_parent = parent; 
		  m_parent.registerDispose(this);
		  pauseTime = pt;
		  // init fft
		  fft = new FFT(fftSize, sampleRate);
		  fft_ptx = ptX;
		  fft_size = fftSize;
		  /*for(int i = 0; i < fftSize; i++) {
	      	println(ptX[i]);
	    	}*/
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
		  }
	    
		  // do something threaded here
		  fft.forward(fft_ptx);
	    
		  m_parent.background(255);
		  // normalization
	    
		  // find the max frequency value
		  float maxVal = 0;
		  for (int i = 1; i < fft.specSize(); i++) {
			  maxVal = Math.max(maxVal, fft.getBand(i));
		  }
		  //float normRate = ((float)(height-80))/maxVal;
		  float normRate = ((float)(m_parent.height-80))/NORMMAXVAL;
		  //float normRate = 1;
	    
		  System.out.println("maxVal: "+maxVal);
		  System.out.println("FFT size: "+fft.specSize());
		  for(int i = 0; i < fft.specSize(); i++) {
			  int barWidth = m_parent.width / fft.specSize();
			  // draw the line for frequency band i, scaling it up a bit so we can see it
			  //line(i, height, i, height - fft.getBand(i)*150 );
			  m_parent.rect(i*barWidth, m_parent.height-fft.getBand(i)*normRate, barWidth, fft.getBand(i)*normRate);
			  //text(fft.getBand(i), i*barWidth, height-fft.getBand(i)*normRate-30); 
		  }
	    
		  // Wait a little (give other threads time to run)
		  try {
			  Thread.sleep(pauseTime);
		  } catch(InterruptedException e) {}
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