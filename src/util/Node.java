package util;

/**
 *  parent class of other node class
 *  it will be used when generating index tree
 *  
 *  @author Xiaoxing Yan
 */
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

public class Node {

	/*store the minimum key of all subtrees*/
	private int minimumKey;
	/*store the address of this node -- page number*/
	public int addressNumber;
	/*store keys in acending order*/
	public List<Integer> keys;
	/*store necessary values*/
	private List<Integer> datalist;
	/*store all children nodes*/
	public List<Node> children;


	/**
	 * constructor to init related fields
	 * 
	 */
	public Node() {
		keys = new ArrayList<Integer>();
		datalist = new ArrayList<Integer>();;
		children = new ArrayList<Node>();
	}

	/*setter method to set minimum key*/
	public void setMinumumKey(int key) {
		this.minimumKey = key;
	}
	
	/*getter method to set minimum key*/
	public int getMinumumKey() {
		return minimumKey;
	}

	/*getter method to get address number*/
	public int getAddressNumber () {
		return addressNumber;
	}
	
	/*getter method to get data list*/
	public List<Integer> getDatalist() {
		return datalist;
	};

	/*add child to children list*/
	public void addChildNode(Node child) {

	}

	/*generate key list*/
	public void generateKeys() {

	}

	/*generate this node*/
	public void generate() {

	}
	
	/*generate this node*/
	public void generate(Map<Integer, List<Integer[]>> map, int[] keys, int keyPosition, int keySize) {

	}


}
