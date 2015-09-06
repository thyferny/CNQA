package in.thyferny.a;
import java.util.List;

public class TranslateMode {
	String from,to;
	List<Data> trans_result;
	public class Data{
		String dst,src;
 
		public String getDst() {
			return dst;
		}
 
		public void setDst(String dst) {
			this.dst = dst;
		}
 
		public String getSrc() {
			return src;
		}
 
		public void setSrc(String src) {
			this.src = src;
		}
		
	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	public List<Data> getTrans_result() {
		return trans_result;
	}
	public void setTrans_result(List<Data> trans_result) {
		this.trans_result = trans_result;
	}
	
}