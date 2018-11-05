package operators;

import java.io.File;
import java.util.LinkedList;

import data.DataBase;
import data.Tuple;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.schema.Column;
import util.TupleReader;

public class IndexScanOperator extends Operator{
	private String tableName;
	private String tableAddress;
	private File tableFile;
	//private RandomAccessFile readPointer;
	private String tableAliase;
	private LinkedList<String> attributes;
	private TupleReader tr;
	
	// When visiting the logicalScanVisitor, A new visitor should be used to discriminate 
	// the "S.A" from S.A > 10, such that the index attribute is obvious and the lowerBound and UpperBound are obvious.
	public IndexScanOperator(String tableName, String tableAliase, Expression expression, String sortColumn) {
		this.tableName = tableName;
		this.tableAddress = DataBase.getInstance().getAddresses(tableName);
		this.tableFile = new File(tableAddress);
		StringBuilder sb = new StringBuilder();
		sb.append(tableName);
		sb.append(' ');
		sb.append(tableAliase);
		this.tr = new TupleReader(sb.toString());
		if (tableAliase == null) this.tableAliase = tableName;
		else this.tableAliase = tableAliase;
		this.attributes = DataBase.getInstance().getSchema(tableName);
		setExpression(expression);
		
		//modification
		schema = tr.getSchema();
		StringBuilder sb2 = new StringBuilder();
		sb2.append("idxScan-");
		sb2.append(tableAliase);
		name = sb2.toString();
		
		// ?????

		
	}
	
	

	@Override
	public Tuple getNextTuple() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}

}
