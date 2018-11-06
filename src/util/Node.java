package util;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

public class Node {
	
	private int minimumKey;
	public int addressNumber;
	
	public List<Integer> keys;
	private List<Integer> datalist;
	public List<Node> children;
	
	
	
	public Node() {
		keys = new ArrayList<Integer>();
		datalist = new ArrayList<Integer>();;
		children = new ArrayList<Node>();
	}
	
	public void setMinumumKey(int key) {
		this.minimumKey = key;
	}
	public int getMinumumKey() {
		return minimumKey;
	}
	
	public int getAddressNumber () {
		return addressNumber;
	}
	public List<Integer> getDatalist() {
		return datalist;
	};
	
	public void addChildNode(Node child) {
		
	}
	
	public void generateKeys() {
		
	}
	
	public void generate() {
		
	}
	public void generate(Map<Integer, List<Integer[]>> map, int[] keys, int keyPosition, int keySize) {
		
	}
	
//	
//	public List<Integer> initNewPage(){
//		List<Integer> list = new ArrayList<>();
//		return list;
//	}
//	
//	public List<Integer> initNewPage (int order, int[] dataEntryIndex, Map<Integer, List<Integer[]>> map, int[] keys) {
//		
//		List<Integer> list = new ArrayList<>();
//		
//		return list;
//		
//	}
	

}
