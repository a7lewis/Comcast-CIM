package hello;

import java.io.Serializable;

public class Ad implements Serializable {
	
	Ad( String id, int duration, String adContent ) {
		this.partner_id = id;
		this.duration = duration;
		this.ad_content = adContent;
	}
	
	Ad() {}
	
	private String partner_id;
	private Integer duration;
	private String ad_content;
	
	public Integer getDuration() { return duration; }	
	public void setDuration( int duration ) { this.duration = duration; }
	
	public String getPartner_id()	{ return partner_id; }
	public void setPartner_id( String id ) { this.partner_id = id; }
	
	public String getAd_content() { return ad_content; }	
	public void setAd_content( String adContent ) { this.ad_content = adContent; }
		
}