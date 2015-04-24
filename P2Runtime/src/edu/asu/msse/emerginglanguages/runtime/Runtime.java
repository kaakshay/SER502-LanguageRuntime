package edu.asu.msse.emerginglanguages.runtime;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
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


			boolean functionReturned = false;
			while(nextLine != null){
				if(functionReturned)
					break;
				StringTokenizer tokenizer = new StringTokenizer(nextLine, ":");
				String token = tokenizer.nextToken();
				switch(VMCommand.get(token)){
				case ENDIF:
					break;
				case ASSIGN:{
					System.out.println("in ASSIGN");
					String name = tokenizer.nextToken();
					DataType type = DataType.get(tokenizer.nextToken());
					String value = tokenizer.nextToken();
					if(value.equals("POP"))
						value = scope.stack.pop().value;

					Data variable = new Data(value, type);
					scope.variables.put(name, variable);
					break;
				}
				case DECL:{
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

					funcLine = reader.readLine();
					while(funcLine!= null && !funcLine.equals("END")){
						funcBody.append(funcLine);
						funcBody.append("\n");
						funcLine = reader.readLine();
					}

					//				System.out.println(funcBody.toString());
					Function function = new Function(funcBody.toString(), arguments, argumentsDataTypes);
					scope.functions.put(funcName, function);
					break;
				}
				case CALL:{
					System.out.println("in CALL");
					String funcCallName = tokenizer.nextToken();

					ArrayList<String> argumentList = new ArrayList<>();
					while(tokenizer.hasMoreTokens()){
						String arg = tokenizer.nextToken();
						argumentList.add(getData(arg, scope).value);
					}
					Function func = null;
					Scope currentScope =  scope;
					HashMap<String, Function> funcMap = null;
					while(currentScope != null){
						funcMap = currentScope.functions;
						if(funcMap != null)
							break;
						currentScope = currentScope.parentScope;
					}
					if(funcMap != null){
						func = funcMap.get(funcCallName);
						if(func != null)
							executeFunction(func, argumentList, scope);
					}else{
						//TODO throw exception
					}
					break;
				}	
				case PRINT:{
					System.out.println("in PRINT");
					String nextToken = tokenizer.nextToken();
					Data data = getData(nextToken, scope);
					if(data != null)
						System.out.println(data.value);
					break;
				}
				case STMT:{
					scope.stack = new Stack<>();
					break;
				}
				case STMTEND:{
					scope.stack = null;
					break;
				}
				case OPERATION:{
					String opToken = tokenizer.nextToken();
					dataforOper(opToken, scope);
					opToken = tokenizer.nextToken();
					dataforOper(opToken, scope);
					opToken = tokenizer.nextToken();
					switch(opToken){
					case "+":{
						int b = Integer.parseInt(scope.stack.pop().value);
						int a = Integer.parseInt(scope.stack.pop().value);
						scope.stack.push(new Data((a+b)+"", DataType.NUMBER));
						break;
					}
					case "-":{
						int b = Integer.parseInt(scope.stack.pop().value);
						int a = Integer.parseInt(scope.stack.pop().value);
						scope.stack.push(new Data((a-b)+"", DataType.NUMBER));
						break;
					}
					case "*":{
						int b = Integer.parseInt(scope.stack.pop().value);
						int a = Integer.parseInt(scope.stack.pop().value);
						scope.stack.push(new Data((a*b)+"", DataType.NUMBER));
						break;
					}
					case "/":{
						int b = Integer.parseInt(scope.stack.pop().value);
						int a = Integer.parseInt(scope.stack.pop().value);
						scope.stack.push(new Data((a/b)+"", DataType.NUMBER));
						break;
					}
					case "%":{
						int b = Integer.parseInt(scope.stack.pop().value);
						int a = Integer.parseInt(scope.stack.pop().value);
						scope.stack.push(new Data((a%b)+"", DataType.NUMBER));
						break;
					}
					case ">":{
						int b = Integer.parseInt(scope.stack.pop().value);
						int a = Integer.parseInt(scope.stack.pop().value);
						scope.stack.push(new Data((a>b)+"", DataType.BOOLEAN));
						break;
					}
					case "==":{
						int b = Integer.parseInt(scope.stack.pop().value);
						int a = Integer.parseInt(scope.stack.pop().value);
						scope.stack.push(new Data((a==b)+"", DataType.BOOLEAN));
						break;
					}
					}

					break;
				}
				case IF:{
					String ifToken = tokenizer.nextToken();
					//						scope.stack = new Stack<>();
					dataforOper(ifToken, scope);
					Data data = scope.stack.pop();
					if(data.type == DataType.BOOLEAN){
						if(!Boolean.parseBoolean(data.value)){
							String ln = reader.readLine();
							while(ln != null && !ln.equals("ENDIF"))
								ln = reader.readLine();
						}
					}else{
						//TODO throw exception not boolean type
					}
					break;
				}
				case RETURN:{
					String retToken = tokenizer.nextToken();
					Data data = getData(retToken, scope);
					if(data != null && scope.parentScope != null)
						scope.parentScope.stack.push(data);
					functionReturned = true;
				}

				}

				nextLine = reader.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void dataforOper(String token, Scope scope){
		Data data = getData(token, scope);
		if(data != null)
			scope.stack.push(data);

	}
	
	public static Data getData(String token, Scope scope){
		String[] parts = token.split("~");
		if(!parts[0].equals("POP")){
			if(!parts[0].equals("VAR")){
				String value = parts[1];
				DataType type = DataType.get(parts[0]);
				return(new Data(value, type));
			}else{
				Data data = getVariable(parts[1], scope);
				if(data != null){
					return(data);
				}else{
					//TODO Throw exception variable not found
				}
			}
		}else{
			if(scope.stack != null)
				return scope.stack.pop();
		}
		return null;
	}

	public static Data getVariable(String variableName, Scope scope){
		Data data = null;
		Scope currentScope =  scope;
		while(currentScope != null){
			data = scope.variables.get(variableName);
			if(data != null)
				break;
			else
				currentScope = currentScope.parentScope;
		}
		return data;
	}

	public static void executeFunction(Function function,ArrayList<String> arguments, Scope parentScope){
		System.out.println("in executeFunction");
		if(parentScope.stack == null)
			parentScope.stack = new Stack<>();
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
		// ((2+3)-3*1)/2
		mainScope = new Scope(null);
		String a= "ASSIGN:a:NUMBER:1\n"+
				"DECL:foo:a~NUMBER\n"+
				"PRINT:VAR~a\n"+
				"END\n"+
				"CALL:foo:a\n"+
				"STMT\n"+
				"OPER:NUMBER~2:NUMBER~3:+\n"+
				"OPER:NUMBER~3:NUMBER~1:*\n"+
				"OPER:POP:POP:-\n"+
				"OPER:POP:NUMBER~2:/\n"+
				"PRINT:POP\n"+
				"STMTEND";

		/*int fact(int n)
	    {
	        int result;
	       if(n==0 || n==1)
	         return 1;

	       result = fact(n-1) * n;
	       return result;
	    }*/

		String actualFact = "DECL:fact:a~NUMBER\n"+
				"STMT\n"+
				"OPER:VAR~a:NUMBER~1:==\n"+
				"IF:POP\n"+
				"STMTEND\n"+
				"RETURN:NUMBER~1\n"+
				"ENDIF\n"+
				"STMT\n"+
				"OPER:VAR~a:NUMBER~1:-\n"+
				"CALL:fact:POP\n"+
				"OPER:POP:VAR~a:*\n"+
				"RETURN:POP\n"+
				"END\n"+
				"ASSIGN:a:NUMBER:5\n"+
				"CALL:fact:VAR~a\n"+
				"PRINT:POP";
				
		String fact = "DECL:fact:a~NUMBER\n"+
				"STMT\n"+
				"OPER:VAR~a:NUMBER~0:>\n"+
				"IF:POP\n"+
				"STMTEND\n"+
				"STMT\n"+
				"OPER:VAR~a:NUMBER~1:-\n"+
				"ASSIGN:a:NUMBER:POP\n"+
				"STMTEND\n"+
				"PRINT:VAR~a\n"+
				"CALL:fact:VAR~a\n"+
				"ENDIF\n"+
				"END\n"+
				"ASSIGN:a:NUMBER:1\n"+
				"CALL:fact:VAR~a";

		String boolStmt = "ASSIGN:a:BOOL:true\n"+
				"STMT\n"+
				"IF:VAR~a\n"+
				"STMTEND\n"+
				"PRINT:VAR~a\n"+
				"ENDIF";
		
		String ifElse = "ASSIGN:a:BOOL:true\n"+
				"STMT\n"+
				"IF:VAR~a\n"+
				"STMTEND\n"+
				"PRINT:VAR~a\n"+
				"ENDIF";
		//		execute(a, mainScope);
		execute(fact, mainScope);
//		execute(actualFact, mainScope);
		//		execute(boolStmt, mainScope);
	}

}
