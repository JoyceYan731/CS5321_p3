package App;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import data.Tuple;
import util.Page;
import util.TupleReader;
import util.TupleWriter;
import util.headerPage;
import util.leafPage;

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
		IndexTreeBuilder builder = new IndexTreeBuilder("Boats", "D", 1,false);
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
		
		TupleWriter write = new TupleWriter("path");//path needs to be changed to real path later
		int pageIndex = 0;
		
		Page headPage = new headerPage();
		List<Integer> data = headPage.initNewPage();
		write.writePage(data);
		pageIndex++;
		
		/*write leaf page*/
		Page leafPage = new leafPage();
		
		Deque<Integer> keyQueue = new LinkedList<>();
		Deque<Integer> valueQueue = new LinkedList<>();
		int[] dataEntryIndex = new int[0];
		
		while (dataEntryIndex[0] < map.size()) {
			data = leafPage.initNewPage(order,dataEntryIndex,map,keys);
			write.writePage(data);
			keyQueue.add(data.get(2));
			valueQueue.add(pageIndex);
			pageIndex++;
		}

		
		
		
		
		
	







		//		//2 open channel, write header page, generate leaf node page
		//		write(int pagenumber, int position, int number);
		//		//3 generate index node page
		//		//4 generate root node page

	}

}
