package visitors;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import data.DataBase;
import data.IndexNote;
import logicalOperators.LogicalScanOperator;
import net.sf.jsqlparser.expression.AllComparisonExpression;
import net.sf.jsqlparser.expression.AnyComparisonExpression;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.InverseExpression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.expression.WhenClause;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseOr;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseXor;
import net.sf.jsqlparser.expression.operators.arithmetic.Concat;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.Matches;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SubSelect;

public class IndexExpressionVisitor implements ExpressionVisitor {
	private IndexNote indexColumn;
	private String tableName;
	private String tableAliase;
	private Expression targetExpression;
	private List<Expression> indexedCondition = new LinkedList<Expression>();
	private List<Expression> unindexedCondition = new LinkedList<Expression>();
	public IndexExpressionVisitor(LogicalScanOperator logicalScan) {
		tableName = logicalScan.getTableName();
		tableAliase = logicalScan.getTableAliase();
		targetExpression = logicalScan.getCondition();
		Map<String, IndexNote> indexInfoRoster = DataBase.getInstance().getIndexInfos();
		indexColumn = indexInfoRoster.get(tableName);
		
	}
	
	
	public IndexNote getIndexColumn() {
		return indexColumn;
	}


	public void setIndexColumn(IndexNote indexColumn) {
		this.indexColumn = indexColumn;
	}


	public String getTableName() {
		return tableName;
	}


	public void setTableName(String tableName) {
		this.tableName = tableName;
	}


	public String getTableAliase() {
		return tableAliase;
	}


	public void setTableAliase(String tableAliase) {
		this.tableAliase = tableAliase;
	}


	public List<Expression> getIndexedCondition() {
		return indexedCondition;
	}


	public void setIndexedCondition(List<Expression> indexedCondition) {
		this.indexedCondition = indexedCondition;
	}


	public List<Expression> getUnIndexedCondition() {
		return unindexedCondition;
	}


	public void setUnIndexedCondition(List<Expression> unIndexedCondition) {
		this.unindexedCondition = unIndexedCondition;
	}
	
	public void Classify() {
		if(targetExpression != null) {
			targetExpression.accept(this);
		}else {
			unindexedCondition.add(targetExpression);
		}
		while(unindexedCondition.size()>1) {
			Expression ex1 = unindexedCondition.get(0);
			Expression ex2 = unindexedCondition.get(1);
			unindexedCondition.remove(0);
			unindexedCondition.remove(1);
			unindexedCondition.add(0, new AndExpression(ex1, ex2));
		}
	}


	@Override
	public void visit(NullValue arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Function arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(InverseExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(JdbcParameter arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(DoubleValue arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(LongValue arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(DateValue arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(TimeValue arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(TimestampValue arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Parenthesis arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(StringValue arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Addition arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Division arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Multiplication arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Subtraction arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AndExpression and) {
		// TODO Auto-generated method stub
		and.getLeftExpression().accept(this);
		and.getRightExpression().accept(this);
		
	}

	@Override
	public void visit(OrExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Between arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(EqualsTo arg0) {
		Expression left = arg0.getLeftExpression();
		Expression right = arg0.getRightExpression();
		if (left instanceof Column && right instanceof Column) {
			unindexedCondition.add(arg0);
		}else if (left instanceof Column) {
			Column column = (Column)left;
			String[] tableNameIndices = column.getWholeColumnName().split("\\.");
			if (tableNameIndices[1].equals(indexColumn.getColumn())) {
				indexedCondition.add(arg0);
			}else {
				unindexedCondition.add(arg0);
			}
		}else if (right instanceof Column) {
			Column column = (Column)right;
			String[] tableNameIndices = column.getWholeColumnName().split("\\.");
			if (tableNameIndices[1].equals(indexColumn.getColumn())) {
				indexedCondition.add(arg0);
			}else {
				unindexedCondition.add(arg0);
			}
		}else {
			unindexedCondition.add(arg0);
		}
	}

	@Override
	public void visit(GreaterThan arg0) {
		Expression left = arg0.getLeftExpression();
		Expression right = arg0.getRightExpression();
		if (left instanceof Column && right instanceof Column) {
			unindexedCondition.add(arg0);
		}else if (left instanceof Column) {
			Column column = (Column)left;
			String[] tableNameIndices = column.getWholeColumnName().split("\\.");
			if (tableNameIndices[1].equals(indexColumn.getColumn())) {
				indexedCondition.add(arg0);
			}else {
				unindexedCondition.add(arg0);
			}
		}else if (right instanceof Column) {
			Column column = (Column)right;
			String[] tableNameIndices = column.getWholeColumnName().split("\\.");
			if (tableNameIndices[1].equals(indexColumn.getColumn())) {
				indexedCondition.add(arg0);
			}else {
				unindexedCondition.add(arg0);
			}
		}else {
			unindexedCondition.add(arg0);
		}
		
	}

	@Override
	public void visit(GreaterThanEquals arg0) {
		Expression left = arg0.getLeftExpression();
		Expression right = arg0.getRightExpression();
		if (left instanceof Column && right instanceof Column) {
			unindexedCondition.add(arg0);
		}else if (left instanceof Column) {
			Column column = (Column)left;
			String[] tableNameIndices = column.getWholeColumnName().split("\\.");
			if (tableNameIndices[1].equals(indexColumn.getColumn())) {
				indexedCondition.add(arg0);
			}else {
				unindexedCondition.add(arg0);
			}
		}else if (right instanceof Column) {
			Column column = (Column)right;
			String[] tableNameIndices = column.getWholeColumnName().split("\\.");
			if (tableNameIndices[1].equals(indexColumn.getColumn())) {
				indexedCondition.add(arg0);
			}else {
				unindexedCondition.add(arg0);
			}
		}else {
			unindexedCondition.add(arg0);
		}
		
	}

	@Override
	public void visit(InExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(IsNullExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(LikeExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(MinorThan arg0) {
		Expression left = arg0.getLeftExpression();
		Expression right = arg0.getRightExpression();
		if (left instanceof Column && right instanceof Column) {
			unindexedCondition.add(arg0);
		}else if (left instanceof Column) {
			Column column = (Column)left;
			String[] tableNameIndices = column.getWholeColumnName().split("\\.");
			if (tableNameIndices[1].equals(indexColumn.getColumn())) {
				indexedCondition.add(arg0);
			}else {
				unindexedCondition.add(arg0);
			}
		}else if (right instanceof Column) {
			Column column = (Column)right;
			String[] tableNameIndices = column.getWholeColumnName().split("\\.");
			if (tableNameIndices[1].equals(indexColumn.getColumn())) {
				indexedCondition.add(arg0);
			}else {
				unindexedCondition.add(arg0);
			}
		}else {
			unindexedCondition.add(arg0);
		}
		
	}

	@Override
	public void visit(MinorThanEquals arg0) {
		Expression left = arg0.getLeftExpression();
		Expression right = arg0.getRightExpression();
		if (left instanceof Column && right instanceof Column) {
			unindexedCondition.add(arg0);
		}else if (left instanceof Column) {
			Column column = (Column)left;
			String[] tableNameIndices = column.getWholeColumnName().split("\\.");
			if (tableNameIndices[1].equals(indexColumn.getColumn())) {
				indexedCondition.add(arg0);
			}else {
				unindexedCondition.add(arg0);
			}
		}else if (right instanceof Column) {
			Column column = (Column)right;
			String[] tableNameIndices = column.getWholeColumnName().split("\\.");
			if (tableNameIndices[1].equals(indexColumn.getColumn())) {
				indexedCondition.add(arg0);
			}else {
				unindexedCondition.add(arg0);
			}
		}else {
			unindexedCondition.add(arg0);
		}
		
	}

	@Override
	public void visit(NotEqualsTo arg0) {
		unindexedCondition.add(arg0)
		
	}

	@Override
	public void visit(Column arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(SubSelect arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(CaseExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(WhenClause arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(ExistsExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AllComparisonExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(AnyComparisonExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Concat arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Matches arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(BitwiseAnd arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(BitwiseOr arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(BitwiseXor arg0) {
		// TODO Auto-generated method stub
		
	}

}
