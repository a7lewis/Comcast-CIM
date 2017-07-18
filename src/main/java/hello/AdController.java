package hello;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import java.util.HashMap;
import java.lang.System;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;


import hello.Ad;

@RestController
public class AdController {

	// first draft - store the ads in memory of spring boot webserver process
	// advantage - ease of development and will be fast since it's in memory store
	// disadvantage - won't allow horizontal scaling - each webserver running this app would have its own store
	// and there would be no consistency - so a POST on server A would not later be properly GETable from server B
	// this fix for this to allow horizontal webserver scaling is a centralized data store such as a database
	private HashMap<String, Ad> adStore = new HashMap<String, Ad>(); 
	private HashMap<String, Long> timeStore = new HashMap<String, Long>();
    
    @RequestMapping(method=RequestMethod.POST, value = "/adcampaign" )
    public ResponseEntity<String> postAdCampaign(@RequestBody Ad incomingAd) {
       
    	System.out.println("POST /adcampaign");
    	
    	// if ad is active, refuse to add another. 
    	// if expired, silently allow it to replace the expired ad
    	long now = System.currentTimeMillis();
    	Long then = timeStore.get( incomingAd.getPartner_id() );  	
    	Ad existingAd = adStore.get( incomingAd.getPartner_id() );
    	if( existingAd != null && then != null ) {		
    		if( (existingAd.getDuration() * 1000 + then.longValue()) > now ) {
    			return new ResponseEntity("cannot replace an active ad campaign", HttpStatus.BAD_REQUEST );
    		}
    	}
              
    	adStore.put(incomingAd.getPartner_id(), incomingAd );
    	timeStore.put(incomingAd.getPartner_id(), now );
    	
        return new ResponseEntity(incomingAd.getAd_content(), HttpStatus.OK);
    }
    
    @RequestMapping(method=RequestMethod.GET, value = "/adcampaign/{partnerId}")
    @ResponseBody
    public ResponseEntity<?> getAdCampaign(@PathVariable String partnerId) {
		
		System.out.println("GET /adcampaign/partnerId");
		
		// check time store - if ad is expired, return error
		long now = System.currentTimeMillis();
		Ad existingAd = adStore.get( partnerId );
		Long then = timeStore.get( partnerId );
		if( existingAd != null && then != null ) {
			if( (existingAd.getDuration() * 1000 + then.longValue()) < now ) {
    			return new ResponseEntity("ad campaign has expired", HttpStatus.BAD_REQUEST );
    		}
			
			return new ResponseEntity<Ad>(existingAd, HttpStatus.OK);
		}
		else {	
			return new ResponseEntity("ad does not exist",HttpStatus.NOT_FOUND);
		}
		   	
    }
    
    @RequestMapping(method=RequestMethod.GET, value = "/allCampaigns")
    @ResponseBody
    public ResponseEntity<?> getAllCampaigns() {
    	// declare json envelope list
    	// populate envelope list from the internal hashmap of Ads and return
    	System.out.println("GET /allCampaigns");
    	
    	ArrayList<Ad> list = new ArrayList<Ad>();  	
    	Iterator it = adStore.entrySet().iterator();
    	while( it.hasNext() ) {
    		Map.Entry pair = (Map.Entry)it.next();
    		Ad iterAd = (Ad) pair.getValue();
    		list.add(iterAd);
    	}
    	AdList adlist = new AdList();
    	adlist.setList(list);    	
    	
    	return new ResponseEntity<AdList>(adlist, HttpStatus.OK);
    }
    
    // one could further extend this app to enable multiple ads per partner
    // by creating a 'campaign_id' and mandating that as part of the Ad json object
    // the store would change - the outer key would still be the partner id,
    // the value would change from a single Ad to an AdList - where each Ad inside the list
    // would be accessed by campaign_id
    
    // deployment and use
    // deployment: 'java -jar build/libs/gs-spring-boot-0.1.0.jar'
    // testing: 'curl localhost:8080/adcampaign/<partner_id>'
    // testing: 'curl localhost:8080/adcampaign -H "Content-Type: application/json" -X POST -d '{"partner_id":"fredo","duration":3,"ad_content":"eat more beets!"}'
    

}
