package App;

import java.beans.Statement;
import java.io.FileReader;
import data.Dynamic_properties;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.statement.select.Select;
import operators.Operator;
import visitors.BasicVisitor;
import net.sf.jsqlparser.statement.select.PlainSelect;


public class SQLInterpreter {

	public static void init (String[] args) {
		Dynamic_properties.setPath(args[0], args[1]);
	}

	public static void BuildQueryPlan () {
		
		String queriesFile = Dynamic_properties.queryPath;
		try {
			CCJSqlParser parser = new CCJSqlParser(new FileReader(queriesFile));
			Statement statement;
			int index = 1;
			while ((statement = (Statement) parser.Statement()) != null) {
				System.out.println("Read statement: " + statement);
				Select select = (Select) statement;
			}
				
//
//		String queriesFile = Dynamic_properties.queryPath;
//		try {
//			Operator root = null;
//			BasicVisitor visitor = new BasicVisitor();
//			
//		
//			
//			
//			
//			
//			
//			CCJSqlParser parser = new CCJSqlParser(new FileReader(queriesFile));
//			Statement statement = (Statement) parser.Statement();
//			int index = 1;
//
//			while (statement != null) {
//				Select select = (Select)statement;
//				root = visitor.getQueryPlan(select);
//				writeToFile (index, root);
//				index++;
//			}


		} catch (Exception e){
			
		}

	}
	
	public static void writeToFile (int index, Operator root) {
		
		root.dump(index);
		
	}
}
