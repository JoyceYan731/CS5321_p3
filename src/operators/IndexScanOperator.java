package operators;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import data.DataBase;
import data.Dynamic_properties;
import data.Tuple;
import util.TupleReader;

/**
 * This is the class of Physical IndexScanOperator. When constructing the physical query plan from
 * logical query plan, we check if the scan conditions of the logical scan operator are suitable for
 * index tree-based way of searching tuples. If (part of) these conditions are, we can construct the
 * IndexScanOperator by given table name, table alias, the index column as well as the lowerBound and
 * upper bound.
 * 
 * For example, if the scan conditions for a scan logical operator with tableName to be "Sailors" is
 * "S.A > 12 AND S.B = 3", and there is an index for table sailors indexed by the column of A, we are
 * able to build the constructor by 
 * 
 *     new IndexScanOperator("Sailors", "S", "A", 13, null);
 *     
 * This IndexScanOperator has the public method "getNextTuple()". Every time it is called,
 * this operator will return the next row of Sailors which are qualified for "S.A > 12". 
 * The way that IndexScanOperator finds tuples is to look up the index tree of Sailors 
 * in "input/db/indexes" directory and find the tuples guided by the addresses and rids in it.
 * 
 * @author Ruoxuan Xu
 *
 */
public class IndexScanOperator extends Operator{
	private final int BUFFER_SIZE = 4096;
	
	/** tableName, alias and table address for this operator */
	private String tableName;
	private String tableAddress;
	private String tableAliase;
	
	/**  tr is the tupleReader that are used by this operator to read tuples from data */
	private TupleReader tr;
	/** the capacity of each page to store tuples */
	private int tuplePerPage; 
	/** the column which serves as the key of index */
	private String column;
	
	/** Below are fields used to read the data from indexes */
	private File indexFile;
	private FileChannel fcin;
	private ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
	private int rootIndex;      
	private int numDataEntries; // the real-time number of data entries on the leaf page being read
    private int leafNum = -1; // when not being initialized, the total number of leaf pages is negative.
  	
	/** 
	 * Basic info of the index : clustered or Not; lowerBound (closed); upperBound(closed); queueOfTuples 
	 * are used for storing the data entries in the middle state of series of "getNextTuple()" calls.
	 */
	private boolean isClustered;
	private Integer upperBound;
	private Integer lowerBound;
	/** Queue of series of Tuple IDs which are targeted at the same key*/
	private Queue<Integer> queueOfTuples;
	
	/** When visiting the logicalScanVisitor, A new visitor should be used to discriminate 
	 * the "S.A" from S.A > 10, such that the index attribute is obvious and the lowerBound and UpperBound are obvious.
	
	 * eg: tableName: Sailors; tableAliase: S; indexColumn A; 
	 * When the constructor is called, Sailors.A must have been indexed.
	 */
	public IndexScanOperator(String tableName, String tableAliase, String indexColumn, Integer lowerBound, Integer upperBound) {
		/*Instantiate the table Name and aliase and DataBase-related field*/
		this.tableName = tableName;
		this.tableAddress = DataBase.getInstance().getAddresses(tableName);
        this.tableAliase = tableAliase;
        this.column = indexColumn;
        LinkedList<String> attributes = DataBase.getInstance().getSchema(tableName);
		this.schema = new HashMap<String, Integer>();
		for (int i=0; i< attributes.size(); i++) {
			StringBuilder sb = new StringBuilder();
			sb.append(tableAliase);
			sb.append(".");
			sb.append(attributes.get(i));
			schema.put(sb.toString(), i);
		}
		
		/*Decide the index we are going to search is Clustered or Not */
		isClustered = DataBase.getInstance().getIndexInfos().
				get(tableName).isClustered();	
		this.upperBound = upperBound == null ? Integer.MAX_VALUE : upperBound;
		this.lowerBound = lowerBound == null ? Integer.MIN_VALUE : lowerBound;
		
		/* if isClustered, read from the temporary directory of the table */
		if (isClustered) {
			this.tableAddress = Dynamic_properties.tempPath + "/"+ tableName;  
		}
		
		/* Initialize tuple reader according to the tableAddress and schema */
		this.tr = new TupleReader(tableAddress, schema);
		tuplePerPage = tr.getNumberOfMaxTuples();
		/* IndexFile */
		this.indexFile = new File(Dynamic_properties.indexedPath + "/" + tableName + "." + indexColumn);

		/* set the name of this operator */
		StringBuilder sb2 = new StringBuilder();
		sb2.append("idxScan-").append(tableAliase);
		name = sb2.toString();
	}
	
	/** Find the address of the leafNode whose key range  contains the target key
	 *  
	 * @param key: the target to locate the leaf page;
	 * @return the address of the leaf page where the key might be found 
	 */
	private int findLeafPage(int key) {
		try {
			if (fcin == null || !fcin.isOpen()) {
				/*get the channel of source file*/ 
				fcin = new RandomAccessFile(indexFile, "r").getChannel();
				buffer.clear();
				fcin.read(buffer); 
				buffer.clear();
				rootIndex = buffer.getInt();
				leafNum = buffer.getInt();
			} 
			return indexSearch(key, this.rootIndex);
		}catch (FileNotFoundException e) {
			e.printStackTrace();
			e.getMessage();
		} catch (IOException e) {
			e.printStackTrace();
			e.getMessage();
		}
		return -1;	
	}

	/** Search for the address of the leaf page within which the key might be 
	 * found, during traversing through the index pages.
	 * 
	 * @param key: the target to locate the leaf page 
	 * @param address: the address of the beginning index node to search leaf page
	 * @return the address of the leaf page where the key might be found
	 */
	private int indexSearch(int key, int address) throws IOException {
		fcin.position(address * BUFFER_SIZE);
		buffer.clear();
		fcin.read(buffer);
		buffer.clear();
		// Base Case: if the page is a leaf page
		if (buffer.getInt() == 0) {
			buffer.clear();
			return address;
		} else {
		// if this page is index page
			int numKeys = buffer.getInt();
			// count record the index of keys in Node being visited.
			int count = 0;
			for (count = 0; count < numKeys; count++) {
				int keyInNode = buffer.getInt();
				if (key < keyInNode) {
					break;
					// at this time, count is the index of pointer which >= key.
				}
			}
			int newAddress = buffer.getInt((2 + numKeys + count) * 4);
			return indexSearch(key, newAddress);
		}
	}
	 
	/**
	 * The first call of this function is only after the fcin has been located to the leaf address 
	 * where the target key is within the range of the leaf keys.
	 * 
	 * Every time it is called, it will return the next qualified (i.e lowerBound <= key <= upperBound) 
	 * queue of tupleIDs, and those in the same queue are from the same data entry and corresponding 
	 * to the same key, which is just another form of the <key, list of rids> pair on the leaf page.
	 * 
	 * When readNextTupleIDQueue() is being called again and again thus the leaf page has been read out,
	 * it will load a new page right behind the former one. If it again gets a leaf page, calling readNextTupleIDQueue()
	 * will still get queue of tupleIDs whose key is in the required range, or it gets an index page, which
	 * means it finishes all reading of leafpages, hence it returns null.
	 * 
	 * @return the next qualified (i.e lowerBound <= key <= upperBound) queue of tupleIDs.
	 *         rid = <pageId + tupleId>
	 *         tupleID = pageId * tupleNumPerPage + tupleId
	 *         
	 * @throws IOException
	 */
	private Queue<Integer> readNextTupleIDQueue() throws IOException{
		// corner case 1: if it is at the beginning of a new page
		if (buffer.position() == 0) {  
			//if this new page is index page, we have finished all scanning of dataEntries in the leafNode
			if (buffer.getInt() == 1) { 
				return null; 
			} else {
			// else, renew the numDataEntries which are to be checked on the current page.
				numDataEntries = buffer.getInt();
			}
		} 
		if (numDataEntries == 0) {  // end of one leaf page
			buffer.clear();
			fcin.read(buffer);
			buffer.clear();
			return readNextTupleIDQueue();
		} 
		// read and check data entries
		int key = buffer.getInt();
		int length = buffer.getInt();
		if (key >= lowerBound && key <= upperBound) {
			Queue<Integer> res = new LinkedList<>();
			for (int i = 0; i < length; i++) {
				res.add(tuplePerPage * buffer.getInt() + buffer.getInt());
			}
			numDataEntries--;
			return res;
		} else if (key < lowerBound) {
			buffer.position(buffer.position() + length * 4 * 2);
			numDataEntries--;
			return readNextTupleIDQueue();
		} else {
			// in this case, key is larger than upperBound, reading on is not likely 
			// to get a qualified key, therefore we return null instantly.
			return null;
		}
		
	}
	
	/**
	 * @return the Tuple which has the key in the specified range.
	 */

	@Override
	public Tuple getNextTuple() {
		try { // if the table index is not clustered
			if (!isClustered) {
				if (queueOfTuples == null) { 
					// if leafNum > 0, then findLeafPage has been called, queueOfTuples has been initialized.
					// This means we have reached the end of data entry, also the end of qualified data.
					if (leafNum > 0) {
						fcin.close();
					    return null;
					}
					// Here is the first time we call "getNextTuple()"
					int leafAddress = findLeafPage(lowerBound);  // at this time leafNum is assigned with non-negative value
					fcin.position(leafAddress * BUFFER_SIZE);
					buffer.clear();
					fcin.read(buffer);	
					buffer.clear();
					queueOfTuples = readNextTupleIDQueue();
				}  else if (queueOfTuples.isEmpty()) {
					queueOfTuples = readNextTupleIDQueue();
				}
				// after update queueOfTuples, check if it is still null.
				if (queueOfTuples == null) {
					fcin.close();
					return null;
				} 
				
				// at this time, queue is sure to be not null nor empty
				int tupleID = queueOfTuples.poll();
				tr.resetFileChannel(tupleID);
				return tr.readNextTuple();
				
			} else { // if it is clustered
				if (queueOfTuples == null) { // when queueOfTuples is not initialized
					int leafAddress = findLeafPage(lowerBound);  // at this time leafNum is assigned with non-negative value
					fcin.position(leafAddress * BUFFER_SIZE);
					buffer.clear();
					fcin.read(buffer);
					buffer.clear();
					queueOfTuples = readNextTupleIDQueue();
					// during the initialization of queueOfTuples, if (queriedOfTuples is null) then no data entries will be qualified
					// return null;
					if (queueOfTuples == null) {
						fcin.close();
						return null;
					}
					// here tr has been marked to the most left entry which are qualified
					int tupleID = queueOfTuples.poll();
					tr.resetFileChannel(tupleID);
					return tr.readNextTuple(); // it can not be null, because it was the data entry from leaf node of the index tree.
				} else {
					Tuple readFromTable = tr.readNextTuple();
					if (readFromTable == null) {
						fcin.close();
						return null;
					}
					int target = (int)readFromTable.getData()[tr.getSchema().get(tableAliase + "." + column)];
					if (target >= lowerBound && target <= upperBound) {
						return readFromTable;
					} else { // target is sure to be larger then upperBound
						fcin.close();
						return null;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			e.getMessage();
		}
		return null;
	}

	@Override
	public void reset() {
		try {
			if (fcin.isOpen()) {
				fcin.close();
			}
			buffer.clear();
			this.queueOfTuples = null;
			this.leafNum = -1;
			this.numDataEntries = 0;
			tr.reset();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}