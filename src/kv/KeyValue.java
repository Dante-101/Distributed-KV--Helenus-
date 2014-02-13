package kv;

public class KeyValue {
	
	String key;
	Value<String> value;
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public Value<String> getValue() {
		return value;
	}
	public void setValue(Value<String> v) {
		this.value = v;
	}
	
	public KeyValue(String key, Value<String> v) {
		super();
		this.key = key;
		this.value = v;
	}
	
	public KeyValue(String key) {
		super();
		this.key = key;
		this.value = new Value<String>("");
	}
	
	public KeyValue() {
		super();
		this.key = "";
		this.value = new Value<String>("");
	}
}
