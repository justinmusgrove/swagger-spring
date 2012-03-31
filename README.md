TODOs:
DocumentationParameter (which could use APIParameter annotation)
	* Description
	* Notes
	* paramType?
	* Default Value
	* allowMultiple

Setup:
* Add component scan for 'package'
* In root directory /resources.json -> swagger-ui
 
Assumptions:
* The EndpointDocumentationController will look @ the root directory since there swagger-ui looks includes it in the path.  Meaning, if were to change EndpointDocumentationController to /doc you would get /doc appended to each request
* It only looks for @Controllers that are annotated with @RequestMapping
* There is an excludesControllers = which defaults to the EndpointController, If there are others you don't want documented add them.
 
Spring:
It would be nice to have a @RequestMapping, @PathVariable description for documentation purposes
 
Wordnik (swagger-ui):
Since all services aren't as fast as yours, it would be nice to have some indicator to show when requests are occurring

