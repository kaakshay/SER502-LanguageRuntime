package edu.asu.msse.emerginglanguages.function;

import java.util.ArrayList;

import edu.asu.msse.emerginglanguages.data.DataType;

public class Function {
	public String body;
	public ArrayList<String> arguments;
	public ArrayList<DataType> argumentsDataTypes;
	
	public Function(String funcBody, ArrayList<String> arguments, ArrayList<DataType> argumentsDataTypes){
		this.body = funcBody;
		this.arguments = arguments;
		this.argumentsDataTypes = argumentsDataTypes;
//		if(arguments == null)
//			this.arguments = new HashMap<>();
	}
}
