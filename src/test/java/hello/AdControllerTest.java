package hello;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class AdControllerTest {

    @Autowired
    private MockMvc mvc;

	// post valid content, should succeed
    @Test
    public void postAd() throws Exception {
    
    	String json = "{\"partner_id\":\"Blue\",\"duration\":10,\"ad_content\":\"buy more widgets!\"}";
    
        mvc.perform(MockMvcRequestBuilders.post("/adcampaign")
        .content(json)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().string(equalTo("buy more widgets!")))
        ;
    }
    
    //  post, then get before ad expires, should succeed
    @Test
    public void postThenGetOK() throws Exception {
    	String json = "{\"partner_id\":\"Red\",\"duration\":10,\"ad_content\":\"buy more widgets!\"}";
    	
    
        mvc.perform(MockMvcRequestBuilders.post("/adcampaign")
        .content(json)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().string(equalTo("buy more widgets!")))
        ;
        
        mvc.perform(MockMvcRequestBuilders.get("/adcampaign/Red")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().string(equalTo(json)));
    }
    
    // test - get without ever posting an add, should get NOT FOUND
    @Test
    public void getNonexistentAd() throws Exception {
    	
    	mvc.perform(MockMvcRequestBuilders.get("/adcampaign/Green")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(content().string(equalTo("ad does not exist")));
    }
    
    // post, then quickly post again - second post should fail since while first still active
    @Test
    public void postTwice() throws Exception {
    	String json = "{\"partner_id\":\"Orange\",\"duration\":10,\"ad_content\":\"buy more widgets!\"}";
    	
    
        mvc.perform(MockMvcRequestBuilders.post("/adcampaign")
        .content(json)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().string(equalTo("buy more widgets!")))
        ;
        
        mvc.perform(MockMvcRequestBuilders.post("/adcampaign")
        .content(json)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(equalTo("cannot replace an active ad campaign")))
        ;
    }
    
    // post, wait until ad expires, get - should get appropriate error
    @Test
    public void postExpireGet() throws Exception {
    	String json = "{\"partner_id\":\"Green\",\"duration\":1,\"ad_content\":\"buy more widgets!\"}";
    	
    
        mvc.perform(MockMvcRequestBuilders.post("/adcampaign")
        .content(json)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().string(equalTo("buy more widgets!")))
        ;
        
        Thread.sleep(2000);
        
        mvc.perform(MockMvcRequestBuilders.get("/adcampaign/Green")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(equalTo("ad campaign has expired")));
    } 
}
