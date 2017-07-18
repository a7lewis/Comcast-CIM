package hello;

import java.io.Serializable;
import java.util.List;
import hello.Ad;

public class AdList implements Serializable {
	
	List<Ad> list;
	
	public List<Ad> getList() {
		return list;
	}
	
	public void setList(List<Ad> list) {
		this.list = list;
	}
	
}