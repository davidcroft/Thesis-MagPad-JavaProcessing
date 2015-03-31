package magPadJavaV1;

import org.neuroph.core.data.DataSet;

public class GlobalConstants {
	// OSC
	//public static String SENDHOST = "128.237.185.246";
	public static String SENDHOST = "169.254.202.224";
	public static final int SENDPORT = 3001;
	public static final int RECVPORT = 3000;
	
	// FFT
	public static final int BUFFERSIZE = 32;
	public static final int SAMPLERATE = 100;
	public static final int FFTFOREACHPOS = 20;
	
	// Neural Network
	public static final int TRAINTOTALROW = 15;
	public static final int TRAINTOTALCOL = 9;
	//public static final int TRAININGPOSNUM = 66;	// number of positions for training
	public static final int TRAININGPOSNUM = TRAINTOTALROW*TRAINTOTALCOL;	// number of positions for training
	public static final int NNINPUTNUM = 36;
	public static final int NNOUTPUTNUM = 2;
	public static DataSet trainingSet = new DataSet(NNINPUTNUM, NNOUTPUTNUM);
	public static DataSet testingSet = new DataSet(NNINPUTNUM, NNOUTPUTNUM);
	public static final double MAXWIDTH = 7.9;			// width 7.9 inch
	public static final double MAXHEIGHT = 9.4;  		// height 9.4 inch
	
	// Machine learning result recognition
	// MINRECOGMEAN is used for lower bound for moving to a new location
	// MAXRECOGMEAN is used for upper bound for getting a robust result
	public static final double MINRECOGMEAN = 0.4;
	public static final double MAXRECOGMEAN = 0.05;
}
