package util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class indexNode extends Node{
	//	private int minimumKey;
	//	private int addressNumber;
	//	public int pageNumber;
	//	public List<Integer> keys;
	//	private List<Integer> datalist;
	//	public List<Node> children;
	private List<Integer> childrenIndex;

	public indexNode() {
		super();
		childrenIndex = new ArrayList<Integer>();
	}
	
	public void addChildNode(Node child) {
		this.children.add(child);
		
	}

	public void generate() {
		//	private int minimumKey;
		//	private int addressNumber;
		//	public int pageNumber;
		//	public List<Integer> keys;
		//	private List<Integer> datalist;
		//	public List<Node> children;
		
		this.getDatalist().add(1);
		
		/*if this index node only has one child*/
		if (children.size() == 1) {
			this.getDatalist().add(0);
			this.getDatalist().add(children.get(0).addressNumber);
		} else {
			
			this.getDatalist().add(children.size()-1);
			this.setMinumumKey(children.get(0).getMinumumKey());//没孩子的情况？？ -- 不会吧
			
			Node leftNode = children.get(0);
			childrenIndex.add(leftNode.addressNumber);
			int i = 1;
			while (i < children.size()) {
				Node rightNode = children.get(i);
				childrenIndex.add(rightNode.addressNumber);
				keys.add(rightNode.getMinumumKey());
				leftNode = rightNode;
				i++;
			}
			
			buildDataList();
			
		}
		
		
	}
	
	public void buildDataList() {
		for (Integer key : keys) {
			this.getDatalist().add(key);
		}
		
		for(Integer index : childrenIndex) {
			this.getDatalist().add(index);
		}
	}
}
