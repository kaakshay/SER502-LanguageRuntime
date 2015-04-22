package edu.asu.msse.emerginglanguages.data;

import java.util.HashMap;
import java.util.Map;

import edu.asu.msse.emerginglanguages.runtime.VMCommand;

public enum DataType {
	INT("INT"), BOOLEAN("BOOL");
    private static final Map<String, DataType> lookup = new HashMap<String, DataType>();
    static {
        for (DataType d : DataType.values())
            lookup.put(d.getType(), d);
    }
	private String type;
	DataType(String type) {
        this.type=type;
    }

    public String getType() {
        return type;
    }
    public static DataType get(String type) {
        return lookup.get(type);
    }
}
