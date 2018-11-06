package App;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import data.Tuple;

import util.TupleReader;
import util.TupleWriter;
import util.Node;
import util.indexNode;
import util.leafNode;


public class IndexTreeBuilder {


	
	
	
	private int order;
	private String tableName;
	private String attribute;
	private boolean isClustered;

	public IndexTreeBuilder(String tableName, String attribute, int order, boolean isClustered) {

		this.tableName = tableName;
		this.order = order;
		this.attribute = attribute;
		this.isClustered = isClustered;

	}

	//	class tuplePair{
	//		public int pageNumber;
	//		public int tupleIndex;
	//		
	//		public tuplePair(int pageNumber, int tupleIndex) {
	//			this.pageNumber = pageNumber;
	//			this.tupleIndex = tupleIndex;
	//		}
	//		
	//	}

	public Map<Integer, List<Integer[]>> buildMap() throws Exception{
		
		Map<Integer, List<Integer[]>> map = new HashMap<>();
		
		TupleReader reader = new TupleReader(tableName);
		int maxTupleNumber = reader.getNumberOfMaxTuples();

		Tuple cur = reader.readNextTuple();
		int tupleNumbers = 0;

		//handle cluster or not
		
		while ( cur!= null) {
			int tupleIndex = tupleNumbers % maxTupleNumber;
			int pageNumber = tupleNumbers / maxTupleNumber;
			int value = (int)cur.getData()[cur.getSchema().get(tableName+"."+attribute)];
			if (map.get(value) == null) {
				List<Integer[]> list= new ArrayList<>();
				list.add(new Integer[] {pageNumber, tupleIndex});
				map.put(value, list);
			} else {
				map.get(value).add(new Integer[] {pageNumber, tupleIndex});
			}

			cur = reader.readNextTuple();
			tupleNumbers++;

		}

		return map;

	}
	
	
	//main method to test
	public static void main(String[] args) throws Exception {
		IndexTreeBuilder builder = new IndexTreeBuilder("Boats", "E", 10,false);
		builder.build();
		
		
	}

	public void reCluster () {
		
	}
	
	
	public void build() throws Exception {
		
		if (isClustered) {
			reCluster () ;
		}

		//1 generate map
		Map<Integer, List<Integer[]>> map  = buildMap();
		
		int[] keys = new int[map.size()];
		int i=0;
		for (Integer key: map.keySet()) {
			keys[i++] = key;
		}
		Arrays.sort(keys);
		
		String path = "src/samples/indexes/indextest";
		TupleWriter write = new TupleWriter(path);//path needs to be changed to real path later
		int pageIndex = 0;
		
		/*write header page*/
		
		System.out.println("head page");
		Node headerNode = new indexNode();
		headerNode.addressNumber = pageIndex;
		List<Integer> data = headerNode.getDatalist();
		write.writePage(data);
		pageIndex++;
		
		
		System.out.println("leaf page");
		
		/*write leaf page*/
		/*the next position of key*/
		int keyPosition = 0;
		Deque<Node> nodeQueue = new LinkedList<>();
		
		while (keyPosition < keys.length) {
			/*check the rest number of children*/
			int restChildren = keys.length - keyPosition;
			int keySize = 2 * order;
			/*2d < children < 3d*/
			if (restChildren > 2 * order && restChildren < 3 * order ) {
				keySize = restChildren /2;
			} else if (restChildren < 2* order) {
				keySize = restChildren;
			}
			
			Node leafNode = new leafNode();
			leafNode.addressNumber = pageIndex;
			leafNode.generate(map, keys, keyPosition, keySize);
			data = leafNode.getDatalist();
			write.writePage(data);
			
			nodeQueue.add(leafNode);
			pageIndex++;
			keyPosition += keySize;
			
		}
		
		boolean childLayer = true;
		
		/*write index page*/
		while (!nodeQueue.isEmpty()) {
			int size = nodeQueue.size();
			if (size == 1 && !childLayer ) {
				pageIndex--;
				break;
			}
			int start = 0;
			/*generate index node for this level*/
			while (start < size) {
				childLayer = false;
				/*check the rest number of children*/
				int restChildren = size - start;
				int keySize = 2 * order +1;
				/*2d +1 < children < 3d +2 */
				if (restChildren > 2 * order +1  && restChildren < 3 * order +2 ) {
					keySize = restChildren /2;
				} else if (restChildren < 2* order +1) {
					keySize = restChildren;
				}
				
				Node indexNode = new indexNode();
				indexNode.addressNumber = pageIndex;
				
				int counter = keySize;
				while(counter>0) {
					indexNode.addChildNode(nodeQueue.poll());
					counter--;
				}
				
				
				indexNode.generate();
				data = indexNode.getDatalist();
				write.writePage(data);
				
				nodeQueue.add(indexNode);
				pageIndex++;
				start += keySize;
				
			}	
			System.out.println("level");
		}
	
		
		write.close();
		
		
		/*rewrite root node*/
		
		

		
		
		
		
		
		
		
		
		
		
//		@Override
//		public List<Integer> initNewPage (int order, int[] dataEntryIndex, Map<Integer, List<Integer[]>> map, int[] keys) {
//			
//			List<Integer> list = new ArrayList<>();
//			list.add(0);
//			
//			
//			/*check the rest number of children*/
//			
//			int restChildren = keys.length - dataEntryIndex[0];
//			int loopsize = 2 * order;
//			/*2d < children < 3d*/
//			if (restChildren > 2* order && restChildren < 3* order ) {
//				loopsize = restChildren /2;
//			} else if (restChildren < 2* order) {
//				loopsize = restChildren;
//			}
//			list.add(loopsize);
//			
//			while (loopsize > 0) {
//				int key = keys[dataEntryIndex[0]++];
//				list.add(key);
//				List<Integer[]> values = map.get(key);
//				for (Integer[] pair : values) {
//					list.add(pair[0]);
//					list.add(pair[1]);
//				}
//				loopsize--;
//			}
			
			
			
//			return list;
//			
//		}
		
		
		
		
//		
//		
//		
//		
//		Page leafPage = new leafPage();
//		
//		Deque<Integer> keyQueue = new LinkedList<>();
//		Deque<Integer> valueQueue = new LinkedList<>();
//		int[] dataEntryIndex = new int[0];
//		
//		while (dataEntryIndex[0] < map.size()) {
//			data = leafPage.initNewPage(order,dataEntryIndex,map,keys);
//			write.writePage(data);
//			keyQueue.add(data.get(2));
//			valueQueue.add(pageIndex);
//			pageIndex++;
//		}

		
		
		
		
		
	







		//		//2 open channel, write header page, generate leaf node page
		//		write(int pagenumber, int position, int number);
		//		//3 generate index node page
		//		//4 generate root node page

	}

}
