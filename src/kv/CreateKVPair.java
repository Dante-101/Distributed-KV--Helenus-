package kv;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class CreateKVPair {
	
	public static void main(String[] args) throws IOException{
		
		int num = 1000;
		String file = "KVPair.txt";
		
		int j = num;
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		for(int i=1; i<=num; i++){
			String line = i + ":" + j + "\n";
			bw.write(line);
			
			--j;
		}
		bw.close();
	}

}
