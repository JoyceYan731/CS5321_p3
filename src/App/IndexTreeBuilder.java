package App;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import data.DataBase;
import data.Dynamic_properties;
import data.IndexNote;
import data.Tuple;

import util.TupleReader;
import util.TupleWriter;
import util.Node;
import util.indexNode;
import util.leafNode;
import java.util.Collections;
import java.util.Comparator;


/**
 * This class provides function:
 * 
 * build index tree for every table
 * 
 * @author Xiaoxing Yan
 *
 */



public class IndexTreeBuilder {

	private int order;
	private String tableName;
	private String attribute;
	private boolean isClustered;
	private Map<String, Integer> schema;
	private String indexFilePath;
	/*store temporary sorted file*/
	private String clusterFilePath;
	private Map<String, IndexNote> indexInfoRoster;


//	//main method to test
//	public static void main(String[] args) throws Exception {
//		//IndexTreeBuilder builder = new IndexTreeBuilder("Boats", "E", 10, true);
//		IndexTreeBuilder builder = new IndexTreeBuilder();
//		builder.build();
//
//
//	}

	/** 
	 * This method is a constructor which is to
	 * init file path and related field
	 * 
	 * @param tableInfo decide which table we want to read
	 * 
	 */
	public IndexTreeBuilder() {

		//for test
//		Map<String, IndexNote> temp = new HashMap<String, IndexNote>();
//		temp.put("Boats", new IndexNote("E",false,10));
//		temp.put("Sailors", new IndexNote("A",true,15));
//
//
//		indexInfoRoster = temp;

		indexInfoRoster = DataBase.getInstance().getIndexInfos();

		/* tempPath = "src/samples/temp" */
		clusterFilePath = Dynamic_properties.tempPath;
		/* indexedPath = inputPath + "/db/indexes" */
		indexFilePath = Dynamic_properties.indexedPath;


	}

	/**
	 * build index tree for every table
	 * 
	 * @throws Exception
	 */
	public void build() throws Exception {
		for (String str : indexInfoRoster.keySet()) {

			this.tableName = str;
			this.order = indexInfoRoster.get(str).getOrder();
			this.attribute = indexInfoRoster.get(str).getColumn();
			this.isClustered = indexInfoRoster.get(str).isClustered();

			buildHelper();

		}
	}

	/**
	 * helper function to build index tree for every table
	 * 
	 * @throws Exception
	 */

	public void buildHelper() throws Exception {

		if (isClustered) {
			reCluster () ;
		}

		/*generate map to store <key, List< [pageid, tupleid] >>*/
		Map<Integer, List<Integer[]>> map  = buildMap();

		int[] keys = new int[map.size()];
		int i=0;
		for (Integer key: map.keySet()) {
			keys[i++] = key;
		}
		Arrays.sort(keys);

		//"src/samples/indexes/indextest";
		String path = indexFilePath + "/" + tableName + "."+attribute;
		TupleWriter write = new TupleWriter(path);
		int pageIndex = 0;

		/*write header page*/
		System.out.println("header page");
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

		int leafNumber = pageIndex-1;
		boolean childLayer = true;

		/*write index page*/
		while (!nodeQueue.isEmpty()) {
			int size = nodeQueue.size();
			/*handle case that the only have one leaf node
			 * and it will generate a index tree like (index node without key) --> (leaf node)
			 * */
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

		/*rewrite root node*/
		headerNode.getDatalist().add(pageIndex);
		headerNode.getDatalist().add(leafNumber);
		headerNode.getDatalist().add(order);

		write.reWritePage(0, headerNode.getDatalist());
		write.close();



	}

	/**
	 * build map to store <key, List< [pageid, tupleid] >>
	 * 
	 * @return map
	 * @throws Exception
	 */
	public Map<Integer, List<Integer[]>> buildMap() throws Exception{

		Map<Integer, List<Integer[]>> map = new HashMap<>();

		TupleReader reader = null;
		/*if needs clustered, read tuples from temporary sorted files*/
		if (isClustered) {
			reader = new TupleReader(clusterFilePath, schema);
		} else {
			reader = new TupleReader(tableName);
		}

		int maxTupleNumber = reader.getNumberOfMaxTuples();

		Tuple cur = reader.readNextTuple();
		int tupleNumbers = 0;


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


	/**
	 * generate new sorted file
	 * 
	 * @throws Exception
	 */
	public void reCluster () throws Exception {

		/*read all tuples and store them in memory*/
		List<Tuple> tuples = new ArrayList<>();
		TupleReader read = new TupleReader(tableName);

		Tuple cur = read.readNextTuple();
		schema = cur.getSchema();
		while (cur != null) {
			tuples.add(cur);
			cur = read.readNextTuple();
		}

		Collections.sort(tuples, new Comparator<Tuple>(){

			@Override
			public int compare(Tuple o1, Tuple o2) {
				int index = o1.getSchema().get(tableName+"."+ attribute);
				int data1 = (int)o1.getData()[index];
				int data2 = (int)o2.getData()[index];

				if (data1 == data2) {
					return 0;
				}
				return data1 < data2 ? -1:1;
			}
		});

		clusterFilePath = clusterFilePath +"/"+tableName;
		
		TupleWriter write = new TupleWriter(clusterFilePath);

		for (int i=0; i< tuples.size(); i++) {
			write.writeTuple(tuples.get(i));

		}
		write.writeTuple(null);

	}



}
