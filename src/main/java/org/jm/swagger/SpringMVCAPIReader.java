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
package org.jm.swagger;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jm.spring.controller.EndpointDocumentationController;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.wordnik.swagger.core.ApiParam;
import com.wordnik.swagger.core.Documentation;
import com.wordnik.swagger.core.DocumentationEndPoint;
import com.wordnik.swagger.core.DocumentationOperation;
import com.wordnik.swagger.core.DocumentationParameter;

/**
 * 
 * @author Justin Musgrove
 *
 */
public class SpringMVCAPIReader {

	private String apiVersion;
	private String swaggerVersion;
	private String basePath;
	private String resourcePath;
	
	@SuppressWarnings({ "rawtypes", "serial" })
	private Set<Class> excludeControllers = new HashSet<Class>(){{
			add(EndpointDocumentationController.class);
	}};
	
	/**
	 * @param apiVersion
	 * @param swaggerVersion
	 * @param basePath
	 * @param resourcePath
	 * @param mapping
	 */
	public SpringMVCAPIReader(String apiVersion, String swaggerVersion, String basePath, String resourcePath) {
		this.apiVersion = apiVersion;
		this.swaggerVersion = swaggerVersion;
		this.basePath = basePath;
		this.resourcePath = resourcePath;
	}

	/**
	 * This method will create the resources from controllers that have
	 * request mappings and pull the base path for {@link DocumentationEndPoint}   
	 * from the classess {@link RequestMapping} if it is not contained in the excludeControllers
	 * list.
	 * 
	 * @param mapping
	 * @return 
	 */
	public Documentation createResources (RequestMappingHandlerMapping mapping) {

		Documentation documentation = new Documentation(this.apiVersion, this.swaggerVersion, this.basePath, this.resourcePath);
		
		Map<String, Object> beansWithAnnotation = mapping.getApplicationContext().getBeansWithAnnotation(Controller.class);
		for (Entry<String, Object> entry : beansWithAnnotation.entrySet()) {
			
			if (!excludeControllers.contains(entry.getValue().getClass())) {
				RequestMapping classRequestMapping = AnnotationUtils.findAnnotation(entry.getValue().getClass(), RequestMapping.class);
				if (classRequestMapping != null && classRequestMapping.value().length > 0) {
					String path = classRequestMapping.value()[0];
					DocumentationEndPoint documentationEndPoint = new DocumentationEndPoint(
							path,  
							entry.getValue().getClass().getSimpleName());

					documentation.addApi(documentationEndPoint);
				}
			}
		}
		return documentation;
	}
	
	/**
	 * This method should create a {@link Documentation} object that should represent a similiar object to 
	 * http://petstore.swagger.wordnik.com/api/pet.json
	 * 
	 * 
	 * @param mapping
	 * @param classRequestMapping
	 * @return
	 */
	public Documentation processMethods (RequestMappingHandlerMapping mapping, String classRequestMapping) {
		
		Documentation documentation = new Documentation(this.apiVersion, this.swaggerVersion, this.basePath, this.resourcePath);
		
		for (Entry<RequestMappingInfo, HandlerMethod> entry : mapping.getHandlerMethods().entrySet()) {
			
			RequestMappingInfo requestMappingInfo = entry.getKey();
			HandlerMethod handlerMethod = entry.getValue();

			// find bean that has a matching classRequestMapping value
			RequestMapping requestMapping = handlerMethod.getBeanType().getAnnotation(RequestMapping.class);
			if (requestMapping.value()[0].contains(classRequestMapping)) {

				String path = null;
				for (String pt : requestMappingInfo.getPatternsCondition().getPatterns()) {
					path = pt;
				}

				//TODO - how to get description for the method
				DocumentationEndPoint documentationEndPoint = new DocumentationEndPoint(path + "{format}", "description");

				DocumentationOperation documentationOperation = processRequestMapping (requestMappingInfo, handlerMethod.getMethod().getName());
				List<DocumentationParameter> documentationParamaters = convertHandlerMethod(handlerMethod);
				documentationOperation.setParameters(documentationParamaters);
				
				documentationEndPoint.addOperation(documentationOperation);
				documentation.addApi(documentationEndPoint);
			}
		}
		
		return documentation;
	}

	/**
	 * NickName right now defaults to the method name
	 * 
	 * @param requestMappingInfo
	 * @return
	 */
	private DocumentationOperation processRequestMapping (RequestMappingInfo requestMappingInfo, String nickName) {
		DocumentationOperation documentationOperation = new DocumentationOperation();
		
		//GET/POST
		Set<RequestMethod> requestMethods = requestMappingInfo.getMethodsCondition().getMethods();
		String value = Joiner.on(",").join(requestMethods); 
		documentationOperation.setHttpMethod(value);
		
		documentationOperation.setNickname(nickName);
		documentationOperation.setSummary("summary");
		
	    return documentationOperation;
	}
	
	/**
	 * Loop through each method parameter and convert to DocumentationParameter
	 * 
	 * @param handlerMethod
	 * @return
	 */
	private List<DocumentationParameter> convertHandlerMethod(HandlerMethod handlerMethod) {
		List<DocumentationParameter> documentationParameters = Lists.newArrayList();
		MethodParameter[] methParameters = handlerMethod.getMethodParameters();
	    for (MethodParameter methodParamater : methParameters) {
	    	if (methodParamater.hasParameterAnnotations()) {
		    	documentationParameters.add(convertMethodParameter (methodParamater));
	    	}
	    }
	    return documentationParameters;
	}

	/**
	 * Create {@link DocumentationParameter} from {@link MethodParameter}
	 * 
	 * @param methodParamater
	 * @return
	 */
	private DocumentationParameter convertMethodParameter (MethodParameter methodParamater) {
		
		DocumentationParameter documentationParameter = new DocumentationParameter();
		documentationParameter.setDataType(methodParamater.getParameterType().getSimpleName()); 
		
		methodParamater.initParameterNameDiscovery(new LocalVariableTableParameterNameDiscoverer());
		documentationParameter.setName(methodParamater.getParameterName());
		
		documentationParameter.setDescription("description");
		documentationParameter.setNotes("notes");
		documentationParameter.setParamType("path");
		documentationParameter.setDefaultValue("default value");
		documentationParameter.setAllowMultiple(false);
		
		PathVariable pathVariable = methodParamater.getParameterAnnotation(PathVariable.class);
    	
    	if (pathVariable != null) {
    		documentationParameter.setRequired(true);
    	}

    	RequestParam requestParam = methodParamater.getParameterAnnotation(RequestParam.class);
    	if (requestParam != null) {
    		documentationParameter.setRequired(requestParam.required());
    		documentationParameter.setDefaultValue(requestParam.defaultValue());
    	}
    	
    	ApiParam apiParam = methodParamater.getParameterAnnotation(ApiParam.class);
    	if (apiParam != null) {
    		documentationParameter.setName(apiParam.name());
    		documentationParameter.setDescription(apiParam.value());
//    		documentationParameter.setAllowableValues(apiParam.allowableValues());
    	}
    	
		return documentationParameter;
	}

	/**
	 * @param excludeControllers the excludeControllers to set
	 */
	@SuppressWarnings("rawtypes")
	public void setExcludeControllers( Set<Class> excludeControllers) {
		if (this.excludeControllers != null) {
			this.excludeControllers.addAll(excludeControllers);
		}
	}
}
