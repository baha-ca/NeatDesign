package org.skyapps.neatdesign.appdirect.integration

import oauth.signpost.OAuthConsumer
import oauth.signpost.basic.DefaultOAuthConsumer
import oauth.signpost.exception.OAuthException
import grails.converters.*

import org.skyapps.neatdesign.security.Role
import org.skyapps.neatdesign.security.User
import org.skyapps.neatdesign.security.UserRole
import grails.plugin.springsecurity.annotation.Secured

@Secured(['permitAll'])
class AppController {

    def index() { }
	
	def create() { 
		render handleAppEvent( params.url, AppEvent.SUBSCRIBE )
	}
	def change() { 
		handleAppEvent( params.url, AppEvent.CHANGE )
	}
	def cancel() {
		handleAppEvent( params.url, AppEvent.UNSUBSCRIBE )
	}
	def status() {
		handleAppEvent( params.url, AppEvent.NOTICE )
	}
	
	protected Object handleAppEvent(String eventURL, AppEvent eventType){
		HttpURLConnection eventRequest = null
		try{
			eventRequest = signURL(eventURL)
			
			def responseMessage = parseAppResponse(eventRequest, eventType)
			
			response.contentType = 'application/xml'
			response.setHeader("Content-Length", responseMessage.length().toString())
			return responseMessage
		} catch (OAuthException oae) {
			response.status = 401
			return "Failed to sign event URL: " + eventURL + ". Exception message: " + oae.getMessage()
		} catch (IOException ioe) {
			response.status = 405
			return "Communication problem with event URL: " + eventURL + ". Exception message: " + ioe.getMessage()
		} finally {
			if (eventRequest && eventRequest.connected){
				eventRequest.disconnect();
			}
		}
	}
	
	protected HttpURLConnection signURL(String eventURL) throws OAuthException {
		OAuthConsumer consumer = new DefaultOAuthConsumer(grailsApplication.config.appdirect.consumerkey, grailsApplication.config.appdirect.secret)
		URL url = new URL(eventURL)
		HttpURLConnection tokenRequest = (HttpURLConnection) url.openConnection()
		consumer.sign(tokenRequest)
		return tokenRequest
	}
	
	protected String parseAppResponse(HttpURLConnection tokenRequest, AppEvent eventType){
		log.info("Response: " + tokenRequest.getResponseCode() + " "
			+ tokenRequest.getResponseMessage());

		def rootNode = tokenRequest.inputStream.readLines().toString()
		rootNode = rootNode.substring(1, rootNode.length()-1)
		rootNode = XML.parse(rootNode)
		log.debug(rootNode)
		switch (eventType){
			case AppEvent.SUBSCRIBE:
				def uniqueId = createNewAccount(rootNode.creator, rootNode.payload.company)
				return createSuccessResponse(uniqueId, eventType)
			case AppEvent.CHANGE:
			case AppEvent.UNSUBSCRIBE:
			case AppEvent.NOTICE:
			default :
				return "Error event type unknown"
		}
	}
	
	protected String createNewAccount(def accountXml, def companyXml){
		def firstName = accountXml.firstName.text()
		String uniqueAccountId = accountXml.lastName.text().charAt(0).toString() + firstName.replaceAll(' ', '').capitalize() + companyXml.name.text().replaceAll(' ', '').capitalize()
		boolean created = User.withTransaction { status ->
			
			def password = "secret" //encodePassword("secret") // for now, default password is secret used for new user creation for non OpenID authentication only
			def user = User.newInstance(username: uniqueAccountId,
										password: password,
										enabled: true)

			user.addToOpenIds(url: accountXml.openId.text())

			if (!user.save()) {
				return false
			}

			
			UserRole.create user, Role.findWhere("authority": "ROLE_OPENID")
			return true
		}
		if (created)
			return uniqueAccountId
			
		return null
		
	}
	
	protected Object createSuccessResponse(String uniqueId, AppEvent eventType){
		def xmlScript = '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<result>
    <success>true</success>
    <message>Account creation successful</message>
    <accountIdentifier>new-account-identifier</accountIdentifier>
</result>'''
		xmlScript = xmlScript.replace("<accountIdentifier>new-account-identifier</accountIdentifier>", "<accountIdentifier>" + uniqueId + "</accountIdentifier>")
		
		return xmlScript
	}

}

enum AppEvent {
	SUBSCRIBE, CHANGE, UNSUBSCRIBE, NOTICE
}


