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
package org.jm.spring.controller;

import javax.servlet.http.HttpServletRequest;

import org.jm.swagger.SpringMVCAPIReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.UrlPathHelper;

import com.wordnik.swagger.core.Documentation;

/**
 * 
 * @author Justin Musgrove
 *
 */
@Controller
@RequestMapping("/")
public class EndpointDocumentationController {

	private final RequestMappingHandlerMapping handlerMapping;
	
	@Autowired
	public EndpointDocumentationController(RequestMappingHandlerMapping handlerMapping) {
		this.handlerMapping = handlerMapping;
		
	}
	
	/**
	 * Construct base path
	 * 
	 * @param httpServletRequest
	 * @return
	 */
	private String getBasePath (HttpServletRequest httpServletRequest) {
		String scheme = httpServletRequest.getScheme();             // http
	    String serverName = httpServletRequest.getServerName();     // hostname.com
	    int serverPort = httpServletRequest.getServerPort();        // 80
	    
		StringBuffer basePath =  new StringBuffer();
		basePath.append(scheme).append("://").append(serverName);
	    if (serverPort != 80) {
	    	basePath.append(":").append(serverPort);
	    }
	    return basePath.toString();
	}
	
	
	/**
	 * Should return a listing of available operations like 
	 * http://petstore.swagger.wordnik.com/api/resources.json
	 * 
	 * @param httpServletRequest
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/resources.json", method=RequestMethod.GET, produces={MediaType.APPLICATION_JSON_VALUE})
	public @ResponseBody Documentation showAvailableResources(
			HttpServletRequest httpServletRequest) throws Exception {
		
		UrlPathHelper urlPathHelper = new UrlPathHelper();
		
		String basePath = getBasePath (httpServletRequest);
		
		SpringMVCAPIReader springMVCAPIReader = new SpringMVCAPIReader(
				"0.1", 
				"1.1-SHAPSHOT.121026", 
				basePath, 
				urlPathHelper.getContextPath(httpServletRequest));
		
		Documentation document =  springMVCAPIReader.createResources(handlerMapping);

		return document;
	}
	
	@RequestMapping(value="/{classRequestMapping}", method=RequestMethod.GET, produces={MediaType.APPLICATION_JSON_VALUE})
	public @ResponseBody  Documentation showAvailableResource(
			HttpServletRequest httpServletRequest, 
			@PathVariable String classRequestMapping) throws ClassNotFoundException {
		
		Documentation document = null;
		UrlPathHelper urlPathHelper = new UrlPathHelper();
		String basePath = getBasePath (httpServletRequest);
		
		SpringMVCAPIReader springMVCAPIReader = new SpringMVCAPIReader(
				"0.1", 
				"1.1-SHAPSHOT.121026", 
				basePath, 
				urlPathHelper.getContextPath(httpServletRequest));
		
		document =  springMVCAPIReader.processMethods(handlerMapping, classRequestMapping);
		
		return document;
	}
	
}
