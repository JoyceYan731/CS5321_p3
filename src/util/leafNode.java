package util;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

public class leafNode extends Node{


	public leafNode() {
		super();
	}

	@Override
	/**
	 * 
	 * this method is for initialization of related fields in this leaf node
	 * 
	 * @param map -- key:attribute value, value: list of <pageid, tupleid>
	 * @param keys -- sorted array of keys
	 * @param keySize -- the number of keys in this node
	 * @param keyPostion -- the next position of key
	 * 
	 * 
	 */
	public void generate(Map<Integer, List<Integer[]>> map, int[] keys, int keyPosition, int keySize) {
		this.getDatalist().add(0);
		this.getDatalist().add(keySize);
		this.setMinumumKey(keys[keyPosition]);

		while (keySize > 0) {
			int key = keys[keyPosition++];
			this.getDatalist().add(key);
			List<Integer[]> values = map.get(key);
			for (Integer[] pair : values) {
				this.getDatalist().add(pair[0]);
				this.getDatalist().add(pair[1]);
			}
			keySize--;
		}


	}


}
