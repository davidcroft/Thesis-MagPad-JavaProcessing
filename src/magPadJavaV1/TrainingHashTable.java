package magPadJavaV1;

import java.util.HashMap;

public class TrainingHashTable {
	// Parameters
	private final double TOTALX = 9.4;     // height 9.4 inch
	private final double TOTALY = 7.9;	  // width 7.9 inch
	
	
	// Hashtable
	//Pair<Float, Float> pair = Pair.createPair(1.0, 2.0);
	public final HashMap<Integer, Pair<Double, Double>> ht = new HashMap<Integer, Pair<Double, Double>>();
	public final int len;
	
	TrainingHashTable() {
		int index = 1;
		/*for (int col = 0; col < TRAINTOTALCOL; col++) {
			for (int row = 0; row < TRAINTOTALROW; row++) {
				double x = (0.8 * row + 0.3) / TOTALX;
				double y = (1.2 * col + 0.3) / TOTALY;
				ht.put(index++, Pair.createPair(x, y));
			}
		}*/
		
		// debug
		for (int col = 0; col < GlobalConstants.TRAINTOTALCOL+1; col++) {
			for (int row = 0; row < GlobalConstants.TRAINTOTALROW; row++) {
				double x = (0.4 * row + 1.9) / TOTALX;
				double y = (0.6 * col + 1.5) / TOTALY;
				ht.put(index++, Pair.createPair(x, y));
			}
		}
		
		//ht.put(index, Pair.createPair(-1, -1));
		
		this.len = ht.size();
		System.out.println("Create a hashtable with items of " + len);
	}
	
	Pair<Double, Double> get(int index) {
		if (index >= len) {
			return Pair.createPair(0.0, 0.0);
		}
		return ht.get(index);
	}
}
