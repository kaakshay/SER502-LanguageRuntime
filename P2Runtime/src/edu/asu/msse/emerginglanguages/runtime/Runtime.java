package edu.asu.msse.emerginglanguages.runtime;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.asu.msse.emerginglanguages.data.Data;
import edu.asu.msse.emerginglanguages.data.DataType;
import edu.asu.msse.emerginglanguages.function.Function;

/**
 * Copyright 2015 Akshay Ashwathanarayana,
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p/>
 *
 * @author Akshay Ashwathanarayana mailto:Akshay.Ashwathanarayana@asu.edu
 * @version April 22, 2015
 */

public class Runtime {
	//	private static Scope mainScope;
	private final static Logger LOG = Logger.getLogger(Runtime.class.getName()); 
	
	private static void execute(String ir, Scope scope){
		LineNumberReader br = new LineNumberReader(new StringReader(ir));
		execute(br, scope);
	}

	private static void execute(File file, Scope scope){
		LineNumberReader br;
		try {
			br = new LineNumberReader(new FileReader(file));
			execute(br, scope);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	//	private static void execute(File file, Scope scope){
	//		LineNumberReader br;
	//		try {
	//			br = new LineNumberReader(new FileReader(file));
	//			execute(br, scope);
	//		} catch (FileNotFoundException e) {
	//			// TODO Auto-generated catch block
	//			e.printStackTrace();
	//		}

	private static void execute(LineNumberReader reader, Scope scope){
		String nextLine=null;
		try {
			nextLine = reader.readLine();

			boolean ifTrue = false;
			boolean functionReturned = false;
			while(nextLine != null){
				if(functionReturned)
					break;
				StringTokenizer tokenizer = new StringTokenizer(nextLine, ":");
				String token = tokenizer.nextToken();
				switch(VMCommand.get(token)){
				case ENDIF:
					LOG.log(Level.INFO, "in ENDIF");
				case ENDWHILE:
					LOG.log(Level.INFO, "in ENDWHILE");
					break;
				case VARDECL:{
					LOG.log(Level.INFO, "in VARDECL");
					String name = tokenizer.nextToken();
					DataType type = DataType.get(tokenizer.nextToken());
					String value = tokenizer.nextToken();
					if(value.equals("POP"))
						value = scope.stack.pop().value;

					Data variable = new Data(value, type);
					scope.variables.put(name, variable);
					break;
				}
				case ASSIGN:{
					LOG.log(Level.INFO, "in ASSIGN");	
					String name = tokenizer.nextToken();
					String value = tokenizer.nextToken();
					if(value.equals("POP"))
						value = scope.stack.pop().value;

					Data data = getVariable(name, scope);
					if(data != null)
						data.value = value;
					break;
				}
				case DECL:{
					LOG.log(Level.INFO, "in DECL");
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

					Function function = new Function(funcBody.toString(), arguments, argumentsDataTypes);
					scope.functions.put(funcName, function);
					break;
				}
				case CALL:{
					LOG.log(Level.INFO, "in CALL");
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
							executeFunction(func, argumentList, scope, false);
					}else{
						LOG.log(Level.SEVERE, "FUNCTION NOT FOUND");
						StringBuilder sb = new StringBuilder();
						sb.append("Error at line number "+ reader.getLineNumber());
						sb.append("Function "+funcCallName +" not found");
						throw new RuntimeException(sb.toString());
					}
					break;
				}	
				case PRINT:{
					LOG.log(Level.INFO, "in PRINT");
					String nextToken = tokenizer.nextToken();
					Data data = getData(nextToken, scope);
					if(data != null)
						System.out.println(data.value);
					break;
				}
				case STMT:{
					LOG.log(Level.INFO, "in STMT");
					scope.stack = new Stack<>();
					break;
				}
				case STMTEND:{
					LOG.log(Level.INFO, "in STMTEND");
					scope.stack = null;
					break;
				}
				case OPERATION:{
					LOG.log(Level.INFO, "in OPERATION");
					String opToken = tokenizer.nextToken();
					dataforOper(opToken, scope);
					opToken = tokenizer.nextToken();
					dataforOper(opToken, scope);
					opToken = tokenizer.nextToken();
					Operator op = Operator.get(opToken);
					switch(op){
					case ADD:{
						LOG.log(Level.INFO, "in OPER +");
						int b = Integer.parseInt(scope.stack.pop().value);
						int a = Integer.parseInt(scope.stack.pop().value);
						scope.stack.push(new Data((a+b)+"", DataType.NUMBER));
						break;
					}
					case SUBTRACT:{
						LOG.log(Level.INFO, "in OPER -");
						int b = Integer.parseInt(scope.stack.pop().value);
						int a = Integer.parseInt(scope.stack.pop().value);
						scope.stack.push(new Data((a-b)+"", DataType.NUMBER));
						break;
					}
					case MULTIPLY:{
						LOG.log(Level.INFO, "in OPER *");
						int b = Integer.parseInt(scope.stack.pop().value);
						int a = Integer.parseInt(scope.stack.pop().value);
						scope.stack.push(new Data((a*b)+"", DataType.NUMBER));
						break;
					}
					case DIVIDE:{
						LOG.log(Level.INFO, "in OPER /");
						int b = Integer.parseInt(scope.stack.pop().value);
						int a = Integer.parseInt(scope.stack.pop().value);
						scope.stack.push(new Data((a/b)+"", DataType.NUMBER));
						break;
					}
					case MOD:{
						LOG.log(Level.INFO, "in OPER %");
						int b = Integer.parseInt(scope.stack.pop().value);
						int a = Integer.parseInt(scope.stack.pop().value);
						scope.stack.push(new Data((a%b)+"", DataType.NUMBER));
						break;
					}
					case GT:{
						LOG.log(Level.INFO, "in OPER >");
						int b = Integer.parseInt(scope.stack.pop().value);
						int a = Integer.parseInt(scope.stack.pop().value);
						scope.stack.push(new Data((a>b)+"", DataType.BOOLEAN));
						break;
					}
					case GTE:{
						LOG.log(Level.INFO, "in OPER >=");
						int b = Integer.parseInt(scope.stack.pop().value);
						int a = Integer.parseInt(scope.stack.pop().value);
						scope.stack.push(new Data((a>=b)+"", DataType.BOOLEAN));
						break;
					}
					case EQUALS:{
						LOG.log(Level.INFO, "in OPER ==");
						int b = Integer.parseInt(scope.stack.pop().value);
						int a = Integer.parseInt(scope.stack.pop().value);
						scope.stack.push(new Data((a==b)+"", DataType.BOOLEAN));
						break;
					}
					case NOTEQUALS:{
						LOG.log(Level.INFO, "in OPER !=");
						int b = Integer.parseInt(scope.stack.pop().value);
						int a = Integer.parseInt(scope.stack.pop().value);
						scope.stack.push(new Data((a==b)+"", DataType.BOOLEAN));
						break;
					}
					case LT:{
						LOG.log(Level.INFO, "in OPER <");
						int b = Integer.parseInt(scope.stack.pop().value);
						int a = Integer.parseInt(scope.stack.pop().value);
						scope.stack.push(new Data((a<b)+"", DataType.BOOLEAN));
						break;
					}
					case LTE:{
						LOG.log(Level.INFO, "in OPER <=");
						int b = Integer.parseInt(scope.stack.pop().value);
						int a = Integer.parseInt(scope.stack.pop().value);
						scope.stack.push(new Data((a<=b)+"", DataType.BOOLEAN));
						break;
					}
					case AND:{
						LOG.log(Level.INFO, "in OPER AND");
						boolean b = Boolean.parseBoolean(scope.stack.pop().value);
						boolean a = Boolean.parseBoolean(scope.stack.pop().value);
						scope.stack.push(new Data((a&&b)+"", DataType.BOOLEAN));
						break;
					}
					case OR:{
						LOG.log(Level.INFO, "in OPER OR");
						boolean b = Boolean.parseBoolean(scope.stack.pop().value);
						boolean a = Boolean.parseBoolean(scope.stack.pop().value);
						scope.stack.push(new Data((a&&b)+"", DataType.BOOLEAN));
						break;
					}
					}

					break;
				}
				case IF:{
					String ifToken = tokenizer.nextToken();
					dataforOper(ifToken, scope);
					Data data = scope.stack.pop();
					if(data.type == DataType.BOOLEAN){
						if(!Boolean.parseBoolean(data.value)){
							String ln = reader.readLine();
							while(ln != null && !(ln.equals("ELSE") ||ln.equals("ENDIF")))
								ln = reader.readLine();
							if(scope.isWhileLoop)
								scope.parentScope.breakWhileLoop = true;
						}else
							ifTrue = true;
					}else{
						LOG.log(Level.SEVERE, "TYPE NOT BOOLEAN");
						StringBuilder sb = new StringBuilder();
						sb.append("Error at line number "+ reader.getLineNumber());
						sb.append(" Statement does not evaluate to a boolean");
						throw new RuntimeException(sb.toString());
					}
					break;
				}
				case ELSE:{
					if(ifTrue){
						String ln = reader.readLine();
						while(ln != null && !ln.equals("ENDIF"))
							ln = reader.readLine();
					}
					break;
				}
				case RETURN:{
					String retToken = tokenizer.nextToken();
					Data data = getData(retToken, scope);
					if(data != null && scope.parentScope != null)
						scope.parentScope.stack.push(data);
					functionReturned = true;
					break;
				}
				case WHILE:{
					System.out.println("in WHILE");
					StringBuilder whileBody = new StringBuilder();
					String funcLine=null;
					funcLine = reader.readLine();
					while(funcLine!= null && !funcLine.equals("ENDWHILE")){
						whileBody.append(funcLine);
						whileBody.append("\n");
						funcLine = reader.readLine();
					}

					Function function = new Function(whileBody.toString(), new ArrayList<String>(), new ArrayList<DataType>());
					while(true){
						if(scope.breakWhileLoop)
							break;
						else
							executeFunction(function, new ArrayList<String>(), scope, true);
					}
					break;
				}
				default:
					break;
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
					LOG.log(Level.SEVERE, "Variable not found");
					StringBuilder sb = new StringBuilder();
					sb.append("Variable "+parts[1] +" not found");
					sb.append("Make sure you have declared the variable");
					throw new RuntimeException(sb.toString());
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
			data = currentScope.variables.get(variableName);
			if(data != null)
				break;
			else
				currentScope = currentScope.parentScope;
		}
		return data;
	}

	public static void executeFunction(Function function,ArrayList<String> arguments, Scope parentScope, boolean isWhileLoop){
		LOG.log(Level.INFO, "in executeFunction");
		if(parentScope.stack == null)
			parentScope.stack = new Stack<>();
		Scope scope = new Scope(parentScope);
		if(isWhileLoop)
			scope.isWhileLoop = true;
		if(arguments.size() == function.arguments.size()){
			for(int i=0 ; i<arguments.size(); i++){
				Data data = new Data(arguments.get(i), function.argumentsDataTypes.get(i));
				scope.variables.put(function.arguments.get(i), data);
			}
		}
		execute(function.body, scope);
	}
	public static void main(String[] args) {
		if(args.length != 0){
			Scope mainScope = new Scope(null);
			File file = new File(args[0]);
			execute(file, mainScope);
		}else{
			runTests();
		}
	}

	public static void runTests(){
		System.out.println("Running all tests");
		// ((2+3)-3*1)/2
		Scope mainScope = new Scope(null);
		String a= "VARDECL:a:NUMBER:1\n"+
				"DECL:foo:a~NUMBER\n"+
				"PRINT:VAR~a\n"+
				"END\n"+
				"CALL:foo:VAR~a\n"+
				"STMT\n"+
				"OPER:NUMBER~5:NUMBER~10:+\n"+
				"OPER:NUMBER~3:NUMBER~2:*\n"+
				"OPER:POP:POP:-\n"+
				"OPER:POP:NUMBER~3:/\n"+
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

		String fact = "DECL:fact:a~NUMBER\n"+
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
				"VARDECL:a:NUMBER:5\n"+
				"CALL:fact:VAR~a\n"+
				"PRINT:POP";

		String boolStmt = "VARDECL:a:BOOL:true\n"+
				"STMT\n"+
				"IF:VAR~a\n"+
				"STMTEND\n"+
				"PRINT:VAR~a\n"+
				"ENDIF";

		String ifElse = "VARDECL:a:BOOL:true\n"+
				"STMT\n"+
				"IF:VAR~a\n"+
				"STMTEND\n"+
				"PRINT:VAR~a\n"+
				"ELSE\n"+
				"PRINT:NUMBER~10\n"+
				"ENDIF";

		String loop = "VARDECL:a:NUMBER:10\n"+
				"WHILE:\n"+
				"STMT\n"+
				"OPER:VAR~a:NUMBER~0:>\n"+
				"IF:POP\n"+
				"STMTEND\n"+
				"PRINT:VAR~a\n"+
				"STMT\n"+
				"OPER:VAR~a:NUMBER~1:-\n"+
				"ASSIGN:a:POP\n"+
				"STMTEND\n"+
				"ENDIF\n"+
				"ENDWHILE";


		String returnTest = "DECL:fact\n"+
				"STMT\n"+
				"RETURN:NUMBER~5\n"+
				"END\n"+
				"VARDECL:a:NUMBER:5\n"+
				"CALL:fact\n"+
				"PRINT:POP";


		System.out.println("Executing ---------- > a");
		mainScope = new Scope(null);
		execute(a, mainScope);
		System.out.println("Executing ---------- > fact");
		mainScope = new Scope(null);
		execute(fact, mainScope);
		System.out.println("Executing ---------- > return test");
		mainScope = new Scope(null);
		execute(returnTest, mainScope);
		System.out.println("Executing ---------- > loop");
		mainScope = new Scope(null);
		execute(loop, mainScope);
		System.out.println("Executing ---------- > bool stmt");
		mainScope = new Scope(null);
		execute(boolStmt, mainScope);
		System.out.println("Executing ---------- > ifElse");
		mainScope = new Scope(null);
		execute(ifElse, mainScope);

	}

}
