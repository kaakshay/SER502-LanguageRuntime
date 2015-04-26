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
 * @version April 22, 2015
 */

public enum VMCommand {
    ASSIGN("ASSIGN"),
    DECL("DECL"),
    BEGIN("BEGIN"),
    PRINT("PRINT"),
    END("END"),
    CALL("CALL"),
    STMT("STMT"),
    STMTEND("STMTEND"),
    OPERATION("OPER"),
    IF("IF"), ELSE("ELSE"), ENDIF("ENDIF"),
    RETURN("RETURN"),
    WHILE("WHILE"), ENDWHILE("ENDWHILE"),
    VARDECL("VARDECL");
    private static final Map<String, VMCommand> lookup = new HashMap<String, VMCommand>();
    static {
        for (VMCommand d : VMCommand.values())
            lookup.put(d.getCommand(), d);
    }
	private String command;
	VMCommand(String command) {
        this.command=command;
    }

    public String getCommand() {
        return command;
    }
    public static VMCommand get(String command) {
        return lookup.get(command);
    }
}

