package magPadJavaV1;

public class LocationRecognition {
	private int m_cirBufSize;
	private int m_bufIndex;
	private double m_meanPrev;
	private double m_meanCurr;
	private double m_prevRecog[];
	
	private double m_location;
	private boolean m_locationReady;
	// status: 0 -- in a robust location and ready to receive location change
	// status: 1 -- in a transition status and wait for get a robust location
	private int m_recogStatus;
	
	public LocationRecognition(int cirBufSize) {
		m_cirBufSize = cirBufSize;
		m_bufIndex = 0;
		m_meanPrev = 0;
		m_meanCurr = 0;
		
		m_location = 0;
		m_locationReady = false;
		
		m_recogStatus = 0;
		m_prevRecog = new double[m_cirBufSize];
	    System.out.println("create a LocationRecognition object");
	}
	
	// when return true, it means a robust new location ready to be read
	public boolean addToRecog(double res) {
		// add to m_prevRecog[]
		if(m_bufIndex < m_cirBufSize) {
			// update m_meanCurr
			m_meanCurr = (m_meanCurr*m_bufIndex+res)/(m_bufIndex+1);
			// push to array
			m_prevRecog[m_bufIndex] = res;
		}

		// detect recogStatus
		updateDetectLocation();

		// update m_bufIndex
		m_bufIndex = m_bufIndex++ % m_cirBufSize;
		
		// return status
		return m_locationReady;
	}
	
	public void updateDetectLocation() {
		// compare m_meanCurr with m_meanPrev to check if get robust result
		if (Math.abs(m_meanCurr-m_meanPrev) >= GlobalConstants.MINRECOGMEAN) {
			// move to a new location
			m_recogStatus = 1;
		} else if (Math.abs(m_meanCurr-m_meanPrev) <= GlobalConstants.MAXRECOGMEAN) {
			// update location
			if (m_recogStatus == 1) {
				m_location = m_meanCurr;
				m_recogStatus = 0;
				m_locationReady = true;
			}
		}
		// update m_meanPrev and m_meanCurr
		m_meanPrev = m_meanCurr;
	}
	
	public double getDetectLocation() {
		// status == 0, get a robust position
		if (m_locationReady) {
			m_locationReady = false;
			return m_location;
		} else {
			return -1;
		}
	}
}
