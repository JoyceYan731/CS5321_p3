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


public class IndexScanOperator extends Operator{
	
	private final int BUFFER_SIZE = 4096;
	// for tableName and alias
	private String tableName;
	private String tableAddress;
	private String tableAliase;
	
	// for IO of tables
	private TupleReader tr;
	private int tuplePerPage; 
	private String column;
	
	// for IO of indexes path
	private File indexFile;
	private FileChannel fcin;
	private ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
	private int rootIndex;      
	private int numDataEntries; // the real-time number of data entries on the leaf page being read
    private int leafNum = -1; // when not being initialized, the total number of leaf pages is negative.
  	
	// for basic info of the index : clustered or Not; lowerBound; upperBound
	private boolean isClustered;
	private Integer upperBound;
	private Integer lowerBound;
	private Queue<Integer> queueOfTuples;
	
	// When visiting the logicalScanVisitor, A new visitor should be used to discriminate 
	// the "S.A" from S.A > 10, such that the index attribute is obvious and the lowerBound and UpperBound are obvious.
	
	// eg: tableName: Sailors; tableAliase: S; sortColumn A; 
	// When the constructor is called, Sailors.A is sure be indexed.
	public IndexScanOperator(String tableName, String tableAliase, String indexColumn, Integer lowerBound, Integer upperBound) {
		//Instantiate the table Name and aliase and DataBase-related field
		this.tableName = tableName;
		this.tableAddress = DataBase.getInstance().getAddresses(tableName);
        this.tableAliase = tableAliase;
        LinkedList<String> attributes = DataBase.getInstance().getSchema(tableName);
		this.schema = new HashMap<String, Integer>();
		for (int i=0; i< attributes.size(); i++) {
			StringBuilder sb = new StringBuilder();
			sb.append(tableAliase);
			sb.append(".");
			sb.append(attributes.get(i));
			schema.put(sb.toString(), i);
		}
		
		//is Clustered or Not
		isClustered = DataBase.getInstance().getIndexInfos().
				get(tableName).isClustered();	
		this.upperBound = upperBound == null ? Integer.MAX_VALUE : upperBound;
		this.lowerBound = lowerBound == null ? 0 : lowerBound;
		
		// if isClustered, read from the temporary directory of the table;
		if (isClustered) {
			this.tableAddress = Dynamic_properties.tempPath + "/sortedTableData" + tableName;  
		}
		
		// Initialize tuple reader according to the tableAddress and schema
		this.tr = new TupleReader(tableAddress, schema);
		tuplePerPage = tr.getNumberOfMaxTuples();
		// IndexFile
		this.indexFile = new File(Dynamic_properties.indexedPath + tableName + "." + indexColumn);

		// set the name of this operator ?? why ?
		StringBuilder sb2 = new StringBuilder();
		sb2.append("idxScan-").append(tableAliase);
		name = sb2.toString();
	}
	
	// return the address of the first leafNode
	private int findLeafPage(int key) {
		try {
			if (fcin == null || !fcin.isOpen()) {
				/*get the channel of source file*/ 
				fcin = new RandomAccessFile(indexFile, "r").getChannel();
				buffer.clear();
				fcin.read(buffer); 
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

	// return the leaf page address during index Search
	private int indexSearch(int key, int address) throws IOException {
		fcin.position(address * BUFFER_SIZE);
		buffer.clear();
		fcin.read(buffer);
		
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
	 
	// when first called, only called after buffer is clear and stores the first valid leaf page
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
			return readNextTupleIDQueue();
		} else {
			return null;
		}
		
	}

	@Override
	public Tuple getNextTuple() {
		try {
			if (!isClustered) {
				if (queueOfTuples == null) { 
					// if leafNum > 0, then findLeafPage has been called, queueOfTuples has been initialized.
					// This is the end of data entry, also the end of qualified data.
					if (leafNum > 0) {
						fcin.close();
					    return null;
					}
					// the first time we call "getNextTuple()"
					int leafAddress = findLeafPage(lowerBound);  // at this time leafNum is assigned with non-negative value
					fcin.position(leafAddress * BUFFER_SIZE);
					buffer.clear();
					fcin.read(buffer);				
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
				int TupleID = queueOfTuples.poll();
				tr.resetFileChannel(TupleID);
				return tr.readNextTuple();
				
			} else { // if it is clustered
				if (queueOfTuples == null) { // when queueOfTuples is not initialized
					int leafAddress = findLeafPage(lowerBound);  // at this time leafNum is assigned with non-negative value
					fcin.position(leafAddress * BUFFER_SIZE);
					buffer.clear();
					fcin.read(buffer);				
					queueOfTuples = readNextTupleIDQueue();
					// during the initialization of queueOfTuples, if (queriedOfTuples is null) then no data entris will be qualified
					// return null;
					if (queueOfTuples == null) {
						fcin.close();
						return null;
					}
					// here tr has been marked to the most left entry which are qualified
					int TupleID = queueOfTuples.poll();
					tr.resetFileChannel(TupleID);
					return tr.readNextTuple(); // it can not be null, because it was the data entry from index tree.
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

// what if the upperBound is null ?
// how to reset ?