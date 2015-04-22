package edu.asu.msse.emerginglanguages.runtime;

import java.util.HashMap;
import java.util.Map;

public enum VMCommand {
    ASSIGN("ASSIGN"),
    DECL("DECL"),
    BEGIN("BEGIN"),
    PRINT("PRINT"),
    END("END"),
    CALL("CALL"),
    STMT("STMT"),
    STMTEND("STMTEND"),
    OPERATION("OPER");
    
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

