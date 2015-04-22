package edu.asu.msse.emerginglanguages.runtime;

import java.util.HashMap;

import edu.asu.msse.emerginglanguages.data.Data;
import edu.asu.msse.emerginglanguages.function.Function;

public class Scope {
	public HashMap<String, Data> variables;
	public HashMap<String, Function> functions = null;
	public Scope parentScope;
	
	public Scope(Scope parentScope){
		this.parentScope = parentScope;
		variables = new HashMap<String, Data>();
		if(parentScope == null)
			functions = new HashMap<String, Function>();
	}
}
