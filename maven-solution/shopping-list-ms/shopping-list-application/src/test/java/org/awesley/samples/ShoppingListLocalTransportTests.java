package org.awesley.samples;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.lifecycle.ResourceProvider;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;
import org.apache.cxf.jaxrs.spring.SpringResourceFactory;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.local.LocalConduit;
import org.awesley.shoppinglist.resources.implementation.ShoppingListApiImpl;
import org.awesley.shoppinglist.resources.interfaces.ShoppingListApi;
import org.awesley.shoppinglist.resources.models.ShoppingList;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CxfServiceSpringBootApplication.class) //, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ShoppingListLocalTransportTests {

	private final static String ENDPOINT_ADDRESS = "local://services";
	private static Server server;

	private static List<Object> providers;
	
	@Autowired
	private void setShoppingListApi(ShoppingListApi shoppingListApi){
		ShoppingListLocalTransportTests.shoppingListApi = shoppingListApi;
	}
	
	private static ShoppingListApi shoppingListApi;
	
	@BeforeClass
	public static void initialize() throws Exception {
		initProviders();
	    startServer();
	}
	 
	private static void initProviders() {
		providers = new ArrayList<Object>();
		JacksonJsonProvider jsonProvider = new JacksonJsonProvider();
		jsonProvider.setMapper(new ObjectMapper());
		providers.add(jsonProvider);
	}

	private static void startServer() throws Exception {
	     JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
	     sf.setResourceClasses(ShoppingListApi.class);
	         
	     sf.setProviders(providers);
	         
	     sf.setResourceProvider(ShoppingListApi.class,
	                            new SingletonResourceProvider(null){
	    	@Override
	    	public Object getInstance(Message m) {
	    		return shoppingListApi;
	    	}
	     });
	     
	     sf.setAddress(ENDPOINT_ADDRESS);
	 
	     server = sf.create();
	}
	 
	@AfterClass
	public static void destroy() throws Exception {
	   server.stop();
	   server.destroy();
	   providers = null;
	}

	@Test
	public void canGetShoppingList() {
		ShoppingListApi client = JAXRSClientFactory.create(ENDPOINT_ADDRESS, ShoppingListApi.class, providers);
		WebClient.getConfig(client).getRequestContext().put(LocalConduit.DIRECT_DISPATCH, Boolean.TRUE);
		
		ShoppingList shoppingList = client.getShoppingList("1");
		
		assertNotNull(shoppingList);
		assertEquals("1", shoppingList.getListID());	
	}
}
