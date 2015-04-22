package edu.asu.msse.emerginglanguages.runtime;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import edu.asu.msse.emerginglanguages.data.Data;
import edu.asu.msse.emerginglanguages.data.DataType;
import edu.asu.msse.emerginglanguages.function.Function;

public class Runtime {
	private static Scope mainScope;
	
	private static void execute(String ir, Scope scope){
		LineNumberReader br = new LineNumberReader(new StringReader(ir));
		execute(br, scope);
	}
	
	private static void execute(LineNumberReader reader, Scope scope){
		String nextLine=null;
		try {
			nextLine = reader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		while(nextLine != null){
			StringTokenizer tokenizer = new StringTokenizer(nextLine, ":");
			String token = tokenizer.nextToken();
			switch(VMCommand.get(token)){
			case ASSIGN:
				System.out.println("in ASSIGN");
				String name = tokenizer.nextToken();
				DataType type = DataType.get(tokenizer.nextToken());
				String value = tokenizer.nextToken();
				Data variable = new Data(value, type);
				scope.variables.put(name, variable);
				break;
			case DECL:
				System.out.println("in DECL");
				String funcName = tokenizer.nextToken();
				
				ArrayList<String> arguments = new ArrayList<>();
				ArrayList<DataType> argumentsDataTypes = new ArrayList<>();
				while(tokenizer.hasMoreTokens()){
					String funcArgExpr = tokenizer.nextToken();
					String[] a = funcArgExpr.split("~");
					arguments.add(a[0]);
					argumentsDataTypes.add(DataType.get(a[1]));
				}
				StringBuilder funcBody = new StringBuilder();
				String funcLine=null;
				try {
					funcLine = reader.readLine();
					while(funcLine!= null && !funcLine.equals("END")){
						funcBody.append(funcLine);
						funcLine = reader.readLine();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
//				System.out.println(funcBody.toString());
				Function function = new Function(funcBody.toString(), arguments, argumentsDataTypes);
				scope.functions.put(funcName, function);
				break;
			case CALL:
				System.out.println("in CALL");
				String funcCallName = tokenizer.nextToken();
				
				ArrayList<String> argumentList = new ArrayList<>();
				while(tokenizer.hasMoreTokens()){
					String arg = tokenizer.nextToken();
					argumentList.add(scope.variables.get(arg).value);
				}
				executeFunction(scope.functions.get(funcCallName), argumentList, scope);
				break;
				
			case PRINT:
				System.out.println("in PRINT");
				System.out.println((scope.variables.get(tokenizer.nextToken())).value);
			}
			
			try {
				nextLine = reader.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void executeFunction(Function function,ArrayList<String> arguments, Scope parentScope){
		System.out.println("in executeFunction");
		Scope scope = new Scope(parentScope);
		if(arguments.size() == function.arguments.size()){
			for(int i=0 ; i<arguments.size(); i++){
				Data data = new Data(arguments.get(i), function.argumentsDataTypes.get(i));
				scope.variables.put(function.arguments.get(i), data);
			}
		}
		execute(function.body, scope);
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		mainScope = new Scope(null);
		String a= "ASSIGN:a:INT:1\n"+
		"DECL:foo:a~INT\n"+
		"PRINT:a\n"+
		"END\n"+
		"CALL:foo:a";
		execute(a, mainScope);
	}

}
