package data;



import java.io.File;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;  
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;

public class TupleReader {

	/*the next position we want to read from the buffer*/
	private int bufferPosition;
	/*the next position we want to read from the file to channel*/
	private long filePosition;
	/*check whether the buffer is empty or not*/
	private boolean empty;
	/*buffer store bytes in one page*/
	private ByteBuffer buffer;
	/*store the number of attributes in one tuple*/
	private int attributeNumber;
	/*the number of tuples we can read in the buffer or page*/
	private int tupleNumber;
	/*the file channel we are using*/
	private FileChannel fcin;

	/*for tuple in this table*/
	private String tableInfo;
	private String tableName;
	private String tableAddress;
	private File tableFile;
	private RandomAccessFile readPointer;
	private String tableAliase;
	private LinkedList<String> attributes;


	/*initilization*/
	public TupleReader (String tableInfo) {
		try {
			empty = true;
			filePosition = 0;
			bufferPosition = 0;
			this.tableInfo = tableInfo;
			System.out.println("这里的table参数是"+tableInfo);
			/*create a new buffer with size 4096 bytes*/ 
			buffer = ByteBuffer.allocate(4096); 
			/*open file channel*/
			initFileChannel (tableInfo);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



	private void initFileChannel (String tableInfo) throws Exception {

		//get the path of file -- correct later
		//String infile = "/Users/yanxiaoxing/Desktop/CS4321/project 2/samples/input/db/data/Boats";  

		String[] aimTable = tableInfo.split("\\s+");
		//这个是处理什么corner case的
		if (aimTable.length<1) {
			tableName = null;
		}

		/*get the information of table*/
		//需要设成全局变量吗
		tableName = aimTable[0];
		tableAddress = DataBase.getInstance().getAddresses(tableName);
		tableAliase = aimTable[aimTable.length-1];
		attributes = DataBase.getInstance().getSchema(tableName);
		File tableFile = new File(tableAddress);

		try {
			/*get the channel of source file*/ 
			fcin = new RandomAccessFile(tableFile, "r").getChannel();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			e.getMessage();
		}

	}


	//main method to test
	public static void main(String[] args) throws Exception {
		TupleReader test = new TupleReader("Boats AS B");
		for (int i=0; i< 341; i++) {

			test.readNextTuple();
		}

	}


	public Tuple readNextTuple() throws Exception {

		/*if buffer is empty, it needs to load another page*/
		if (empty) {
			int r = readFromChannel();
			// read方法返回读取的字节数，可能为零，如果该通道已到达流的末尾，则返回-1  
			/*if we have reached the end of file*/
			if (r == -1) {
				return null;  
			} 
			empty = false;
		} 

		/*initilize metadata if this buffer read a new page*/
		checkBuffer();

		/*read one integer every time from the buffer*/
		long[] data = new long[attributeNumber];
		for (int i=0; i<attributeNumber; i++) {
			data[i] = (long)buffer.getInt(bufferPosition);
			bufferPosition += 4;
		}

		/*create a new tuple*/
		Tuple tuple = new Tuple(data, tableAliase, attributes);
		tupleNumber--;

		/* after reading all tuples in one page
		 * load another page to buffer
		 **/
		if (tupleNumber == 0) {
			empty = true;
		}

		return tuple;
	}

	/**
	 * check the buffer state:
	 * 
	 * to read metadata
	 * or to read tuple
	 * 
	 */
	public void checkBuffer() {
		if (bufferPosition < 8) {
			/*initilize metadata*/
			attributeNumber = buffer.getInt(bufferPosition);
			bufferPosition += 4;
			tupleNumber = buffer.getInt(bufferPosition);
			bufferPosition += 4;	
		}
	}


	/**
	 * load one page to buffer 
	 * 
	 * 
	 * @return r
	 * @throws Exception
	 */
	private int readFromChannel() throws Exception  {

		// clear方法重设缓冲区，使它可以接受读入的数据  
		buffer.clear();  


		System.out.println("读之前这里的file position是"+filePosition);

		/*Reads a sequence of bytes from this channel into the given buffer.*/
		int r = fcin.read(buffer); 



		/*record the next position*/
		filePosition = fcin.position()+1;//debug later 不确定这里的position是否是对的
		System.out.println("读之后这里的file position是"+filePosition);

		/*set the buffer position to zero*/
		buffer.flip();

		return r;

	}



	/**
	 * close the file channel
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {

		try {
			if(fcin != null) {
				fcin.close();
			}

		} catch(IOException e) {
			throw e;
		}

	}

	
	/**
	 * 
	 */
	public void reset() {

	}
	
	public void reset(int index) {

	}

}



