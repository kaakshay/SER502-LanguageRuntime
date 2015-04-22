package edu.asu.msse.emerginglanguages.data;

public class Data {
	public DataType type;
	public String value;
	
	public Data(String value, DataType type) {
        this.value = value;
        this.type = type;
    }

    @Override
    public String toString() {
        return value;
    }
}
