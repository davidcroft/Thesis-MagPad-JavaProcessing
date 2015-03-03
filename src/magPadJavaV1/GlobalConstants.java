package magPadJavaV1;

import org.neuroph.core.data.DataSet;

public class GlobalConstants {
	// OSC
	public static String SENDHOST = "";
	public static final int SENDPORT = 3001;
	public static final int RECVPORT = 3000;
	
	// FFT
	public static final int BUFFERSIZE = 32;
	public static final int SAMPLERATE = 100;
	public static final int FFTFOREACHPOS = 20;
	
	// Neural Network
	public static final int TRAININGPOSNUM = 6;	// number of positions for training
	public static final int NNINPUTNUM = 12;
	public static final int NNOUTPUTNUM = 1;
	public static DataSet trainingSet = new DataSet(NNINPUTNUM*3, NNOUTPUTNUM);
	public static DataSet testingSet = new DataSet(NNINPUTNUM*3, NNOUTPUTNUM);
}
