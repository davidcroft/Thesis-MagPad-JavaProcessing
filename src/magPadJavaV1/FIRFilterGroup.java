package magPadJavaV1;

public class FIRFilterGroup {
	// FirFilter
	public FirFilter firFilterUpX, firFilterUpY, firFilterUpZ;				// up magnet
	public FirFilter firFilterDownX, firFilterDownY, firFilterDownZ;		// down magnet
	public FirFilter firFilterLeftX, firFilterLeftY, firFilterLeftZ;		// left magnet
	public FirFilter firFilterRightX, firFilterRightY, firFilterRightZ;		// right magnet
	
	public FIRFilterGroup() {
		// FIR filter init
		// up
		firFilterUpX = new FirFilter("up.fcf");
		firFilterUpY = new FirFilter("up.fcf");
		firFilterUpZ = new FirFilter("up.fcf");
		// down
		firFilterDownX = new FirFilter("down.fcf");
		firFilterDownY = new FirFilter("down.fcf");
		firFilterDownZ = new FirFilter("down.fcf");
		// left
		firFilterLeftX = new FirFilter("left.fcf");
		firFilterLeftY = new FirFilter("left.fcf");
		firFilterLeftZ = new FirFilter("left.fcf");
		// right
		firFilterRightX = new FirFilter("right.fcf");
		firFilterRightY = new FirFilter("right.fcf");
		firFilterRightZ = new FirFilter("right.fcf");
	}
	
	public float applyToFilter(float val, int axisIndex) {
		float filterUp = 0;
		float filterDown = 0;
		float filterLeft = 0;
		float filterRight = 0;
		// 0 for x; 1 for y and 2 for z axis respectively
		switch (axisIndex) {
	        case 0:
	        	filterUp = (float)firFilterUpX.filter((double)val);
	        	filterDown = (float)firFilterDownX.filter((double)val);
	        	filterLeft = (float)firFilterLeftX.filter((double)val);
	        	filterRight = (float)firFilterRightX.filter((double)val);
	        	break;
	        case 1:
	        	filterUp = (float)firFilterUpY.filter((double)val);
	        	filterDown = (float)firFilterDownY.filter((double)val);
	        	filterLeft = (float)firFilterLeftY.filter((double)val);
	        	filterRight = (float)firFilterRightY.filter((double)val);
	        	break;
	        case 2:
	        	filterUp = (float)firFilterUpZ.filter((double)val);
	        	filterDown = (float)firFilterDownZ.filter((double)val);
	        	filterLeft = (float)firFilterLeftZ.filter((double)val);
	        	filterRight = (float)firFilterRightZ.filter((double)val);
	        	break;
	        default: break;
		}
		float ave1 = filterUp + (filterDown - filterUp) / 2;
		float ave2 = filterLeft + (filterRight - filterLeft) / 2;
		return ave1 + (ave2 - ave1)/2;
	}
	
	
}
