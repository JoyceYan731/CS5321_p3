package util;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

public class leafPage extends Page{
	

	@Override
	public List<Integer> initNewPage (int order, int[] dataEntryIndex, Map<Integer, List<Integer[]>> map, int[] keys) {
		
		List<Integer> list = new ArrayList<>();
		list.add(0);
		
		
		/*check the rest number of children*/
		
		int restChildren = keys.length - dataEntryIndex[0];
		int loopsize = 2 * order;
		/*2d < children < 3d*/
		if (restChildren > 2* order && restChildren < 3* order ) {
			loopsize = restChildren /2;
		} else if (restChildren < 2* order) {
			loopsize = restChildren;
		}
		list.add(loopsize);
		
		while (loopsize > 0) {
			int key = keys[dataEntryIndex[0]++];
			list.add(key);
			List<Integer[]> values = map.get(key);
			for (Integer[] pair : values) {
				list.add(pair[0]);
				list.add(pair[1]);
			}
			loopsize--;
		}
		
		
		
		return list;
		
	}

}
