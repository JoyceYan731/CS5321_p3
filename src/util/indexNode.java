package util;

/**
 *  index node class
 *  
 *  it will be used when generating index tree
 *  
 *  @author Xiaoxing Yan
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class indexNode extends Node{
	
	/*store all indexes for children*/
	private List<Integer> childrenIndex;

	/**
	 * constructor to init related fields
	 * 
	 */
	public indexNode() {
		super();
		childrenIndex = new ArrayList<Integer>();
	}
	
	@Override
	public void addChildNode(Node child) {
		this.children.add(child);	
	}

	@Override
	public void generate() {
	
		this.getDatalist().add(1);
		
		/*if this index node only has one child*/
		if (children.size() == 1) {
			this.getDatalist().add(0);
			this.getDatalist().add(children.get(0).addressNumber);
		} else {
			
			this.getDatalist().add(children.size()-1);
			this.setMinumumKey(children.get(0).getMinumumKey());//what if without child?
			
			Node leftNode = children.get(0);
			childrenIndex.add(leftNode.addressNumber);
			int i = 1;
			while (i < children.size()) {
				Node rightNode = children.get(i);
				childrenIndex.add(rightNode.addressNumber);
				/*choose key by using the smallest search key found in the leftmost leaf of this subtree*/
				keys.add(rightNode.getMinumumKey());
				leftNode = rightNode;
				i++;
			}
			
			buildDataList();
			
		}
		
		
	}
	
	/**
	 * generate data list for this index node
	 */
	public void buildDataList() {
		
		for (Integer key : keys) {
			this.getDatalist().add(key);
		}
		
		for(Integer index : childrenIndex) {
			this.getDatalist().add(index);
		}
	}
}
