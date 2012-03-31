/*
 * Copyright 2002-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jm.spring.controller.test;

import javax.servlet.http.HttpServletResponse;

import junit.framework.Assert;

import org.codehaus.jackson.map.ObjectMapper;
import org.jm.swagger.SpringMVCAPIReader;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.wordnik.swagger.core.Documentation;


/**
 * 
 * @author Justin Musgrove
 */
public class SpringMVCAPIReaderTest {

	private RequestMappingHandlerMapping mapping;

	private Handler handler;

	private ObjectMapper mapper;
	

	@Before
	public void setUp() throws Exception {
		mapper = new ObjectMapper();
		handler = new Handler();

		StaticApplicationContext context = new StaticApplicationContext();
		context.registerSingleton("handler", handler.getClass());

		mapping = new RequestMappingHandlerMapping();
		mapping.setApplicationContext(context);
	}
	
	@Test
	public void test_get_all_resources () {
		
		SpringMVCAPIReader mvcapiReader = new SpringMVCAPIReader(
				"0.1", 
				"1.1-SHAPSHOT.121026", 
				"localhost:8080", 
				"");
		Documentation doc = mvcapiReader.createResources(mapping);
		
		Assert.assertNotNull(doc);
	}
	
	@Test
	public void test_process_method () {
		
		SpringMVCAPIReader mvcapiReader = new SpringMVCAPIReader(
				"0.1", 
				"1.1-SHAPSHOT.121026", 
				"localhost:8080", 
				"");
		Documentation doc = mvcapiReader.processMethods(mapping, Handler.class.toString());
		
		Assert.assertNotNull(doc);
	}
	
	
	@SuppressWarnings("unused")
	@Controller
	@RequestMapping ("/test")
	private static class Handler {

		@RequestMapping(
				value = {"/{param1}/{param2}/detail"}, 
				method = {RequestMethod.GET, RequestMethod.POST}, 
				produces = {MediaType.APPLICATION_JSON_VALUE}, 
				consumes = {MediaType.APPLICATION_JSON_VALUE},
				headers = {}
			)
		public void multipleValueWithMediaType (
				HttpServletResponse response,
				@PathVariable Integer param1, //@ApiParam(name = "Path Variable 1", value = "ID of pet that needs to be fetched", allowableValues = "range[1,5]")   
				@PathVariable String param2, //@ApiParam(name = "Path Variable 2", value = "ID of pet that needs to be fetched", allowableValues = "range[1,5]")  
				@RequestParam(defaultValue="none", required=true, value="requestParam1") String requestParam1, //@ApiParam(name = "Request Parameter 1", value = "ID of pet that needs to be fetched", allowableValues = "range[1,5]") 
				@RequestParam(value="requestParam2") Integer requestParam2 //@ApiParam(name = "Request Parameter 2", value = "ID of pet that needs to be fetched", allowableValues = "range[1,5]") 
				) { 
		}
	}
}