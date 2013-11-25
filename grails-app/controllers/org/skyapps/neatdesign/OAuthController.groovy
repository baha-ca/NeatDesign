package org.skyapps.neatdesign

import org.springframework.security.access.annotation.Secured;

import oauth.signpost.OAuthConsumer
import oauth.signpost.basic.DefaultOAuthConsumer
import oauth.signpost.signature.QueryStringSigningStrategy

@Secured(['ROLE_ADMIN'])
class OAuthController {

	def signRequest() {
		OAuthConsumer consumer = new DefaultOAuthConsumer("Dummy", "secret");
		URL url = new URL("https://www.appdirect.com/AppDirect/rest/api/events/dummyChange")
		HttpURLConnection request = (HttpURLConnection) url.openConnection();
		consumer.sign(request);
		request.connect();
		render "request signed: " + request
	}
	
	def signURL() {
		OAuthConsumer consumer = new DefaultOAuthConsumer("Dummy", "secret");
		consumer.setSigningStrategy( new QueryStringSigningStrategy());
		String url = "https://www.appdirect.com/AppDirect/finishorder?success=true&accountIdentifer=Alice";
		String signedUrl = consumer.sign(url);
		render "SignedUrl answer: " + signedUrl
	}
	
	def signURLFromParam() {
		OAuthConsumer consumer = new DefaultOAuthConsumer(grailsApplication.config.appdirect.consumerkey, grailsApplication.config.appdirect.secret)
		consumer.setSigningStrategy( new QueryStringSigningStrategy());
		String url = params.urlToSign
		String signedUrl = consumer.sign(url);
		[signedUrl:signedUrl]
	}
	
	def signRequestFromParam(){
		OAuthConsumer consumer = new DefaultOAuthConsumer(grailsApplication.config.appdirect.consumerkey, grailsApplication.config.appdirect.secret);
		URL url = new URL(params.requestUrlToSign)
		HttpURLConnection request = (HttpURLConnection) url.openConnection();
		consumer.sign(request);
		request.connect();
		render view: 'signURLFromParam', model: [signedUrl: request, fromRequest:true]
	}
}
