package edu.asu.msse.emerginglanguages.runtime;

import java.util.HashMap;
import java.util.Stack;

import edu.asu.msse.emerginglanguages.data.Data;
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

public class Scope {
	public HashMap<String, Data> variables;
	public HashMap<String, Function> functions = null;
	public Stack<Data> stack = null;
	public Scope parentScope;
	public boolean isWhileLoop = false;
	public boolean breakWhileLoop = false;
	public Scope(Scope parentScope){
		this.parentScope = parentScope;
		variables = new HashMap<String, Data>();
		if(parentScope == null)
			functions = new HashMap<String, Function>();
	}
}
