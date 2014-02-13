package kv;

import common.Parameters;

public class Value<V> {
	
	private V v;
	
	public Value(){
		this.v = null;
	}
	
	public Value(V t){
		this.v = t;
	}
	
	public void set(V t) {
		this.v = t;
	}
	
	public V get() {
		return v;
	}
	
	public static Long getTimeStamp(Value<String> value){
		String[] v = value.get().split(Parameters.valueDelimiterRegex, 2);
		return Long.parseLong(v[0]);
	}
	
	public static String getData(Value<String> value){
		String[] v = value.get().split(Parameters.valueDelimiterRegex, 2);
		return v[1];
	}
	
	public static Value<String> setTimeStamp(Value<String> value){
		String str = System.currentTimeMillis() + Parameters.valueDelimiter + value.get();
		value.set(str);
		return value;
	}
	
	
}

