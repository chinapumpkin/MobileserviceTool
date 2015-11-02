/*
 * Author: Jari Tervonen <jjtervonen@gmail.com> 2013-
 *
 */
package uni.oulu.firstprotocol;

import java.io.IOException;
import java.io.Serializable;

public class DirectionPattern implements Serializable{

	private static final long serialVersionUID = 2L;
	
	private int on_delay=0;
	private int off_delay=0;
	private long [] data_pattern;
	private int repeat_count;
	
	public DirectionPattern(long [] data_pat, int on, int off, int repeat) {
		on_delay = on;
		off_delay = off;
		data_pattern = data_pat;
		//data_count = data_c;
		repeat_count = repeat;
	}
	
	public int getOnDelay() {
		return on_delay;
	}
	
	public int getOffDelay() {
		return off_delay;
	}
	
	public int getRepeats() {
		return repeat_count;
	}
	
	public long getData(int i) {
		if (data_pattern.length <= i)
			return 0x0;
		
		return data_pattern[i];
	}
	
	public int getDataCount() {
		return data_pattern.length;
	}
	
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		for (int i=0; i<data_pattern.length;i++)
			b.append(data_pattern[i]);
			b.append(',');
		b.append(':');
		b.append(on_delay);
		b.append(':');
		b.append(off_delay);
		b.append(':');
		b.append(repeat_count);
		
		return b.toString();
	}
	
	private void writeObject(java.io.ObjectOutputStream out)
			throws IOException {
		// write 'this' to 'out'...
		out.writeChars(toString());   
	}
	
	private void readObject(java.io.ObjectInputStream in)
			throws IOException, ClassNotFoundException {
		// populate the fields of 'this' from the data in 'in'...
		String s = (String)in.readObject();
		DirectionPattern d = compilePattern(s);
		this.data_pattern = d.data_pattern;
		this.on_delay = d.getOnDelay();
		this.off_delay = d.getOffDelay();
		this.repeat_count = d.getRepeats();
	}
	
	public static DirectionPattern compilePattern(String t) {
		String [] c = t.split(":");
		int size = c.length;
		
		if (size < 4)
			return null;
		
		DirectionPattern ret = null; // = new TrackCoord[size];
	
		
		String [] sv = c[0].split(",");
		long [] v = new long[sv.length];
		for(int j=0;j<v.length;j++) {
			v[j] = Long.valueOf(sv[0]);
		}
		ret= new DirectionPattern(v, Integer.valueOf(c[1]), Integer.valueOf(c[2]), Integer.valueOf(c[3]));
		return ret;
	}
}
