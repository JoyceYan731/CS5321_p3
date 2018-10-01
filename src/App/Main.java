package App;

import java.io.FileReader;
import java.util.List;

import data.Dynamic_properties;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import operators.JoinOperator;
import operators.ProjectOperator;
import operators.ScanOperator;
import operators.SelectOperator;

public class Main {

	public static void main(String[] args) {
		String queriesFile = Dynamic_properties.TEST_QUERY_PATH;
		try {
			CCJSqlParser parser = new CCJSqlParser(new FileReader(queriesFile));
			Statement statement = parser.Statement();
			Select select = (Select)statement;
			PlainSelect ps = (PlainSelect)select.getSelectBody();
			String table_info = ps.getFromItem().toString();
			List table_info1 = ps.getJoins();
			ScanOperator scanOp = new ScanOperator(table_info);
			ScanOperator scanOp1 = new ScanOperator(table_info1.get(0).toString());
			JoinOperator joinOp = new JoinOperator(scanOp, scanOp1);
			//SelectOperator selectOp = new SelectOperator(ps,scanOp);
			ProjectOperator projectOp = new ProjectOperator(ps, joinOp);
			projectOp.dump();
		} catch (Exception e) {
			System.err.println("Exception occurred during parsing");
			e.printStackTrace();
		}

	}

}
