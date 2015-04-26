package edu.asu.msse.emerginglanguages.runtime;

import java.util.HashMap;
import java.util.Map;

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
 * @version April 26, 2015
 */

public enum Operator {
	LT("<"), LTE("<="),
	GT(">"), GTE(">="),
	EQUALS("=="), NOTEQUALS("!="),
	ADD("+"), SUBTRACT("-"), MULTIPLY("*"), DIVIDE("/"), MOD("%"),
	AND("AND"), OR("OR");
	
	private static final Map<String, Operator> lookup = new HashMap<String, Operator>();
    static {
        for (Operator d : Operator.values())
            lookup.put(d.getOperator(), d);
    }
	private String operator;
	
	Operator(String operator) {
        this.operator=operator;
    }

    public String getOperator() {
        return operator;
    }
    public static Operator get(String oper) {
        return lookup.get(oper);
    }
}
