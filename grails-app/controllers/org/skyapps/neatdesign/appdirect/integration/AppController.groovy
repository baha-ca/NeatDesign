package org.skyapps.neatdesign.appdirect.integration

import oauth.signpost.OAuthConsumer
import oauth.signpost.basic.DefaultOAuthConsumer
import oauth.signpost.exception.OAuthException
import grails.converters.*

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.skyapps.neatdesign.customer.Subscription;
import org.skyapps.neatdesign.security.Role
import org.skyapps.neatdesign.security.User
import org.skyapps.neatdesign.security.UserRole

import grails.plugin.springsecurity.annotation.Secured

@Secured(['permitAll'])
class AppController {
	def userCache
	
    def index() { }
	
	def create() { 
		render handleAppEvent( params.url, AppEvent.SUBSCRIPTION_ORDER )
	}
	def change() { 
		render handleAppEvent( params.url, AppEvent.SUBSCRIPTION_CHANGE )
	}
	def cancel() {
		render handleAppEvent( params.url, AppEvent.SUBSCRIPTION_CANCEL )
	}
	def status() {
		render handleAppEvent( params.url, AppEvent.SUBSCRIPTION_NOTICE )
	}
	
	def addUser(){
		render handleAppEvent( params.url, AppEvent.USER_ASSIGNMENT )
	}
	
	def removeUser(){
		render handleAppEvent( params.url, AppEvent.USER_UNASSIGNMENT )
	}
	
	protected Object handleAppEvent(String eventURL, AppEvent eventType){
		HttpURLConnection eventRequest = null
		try{
			eventRequest = signURL(eventURL)
			
			def responseMessage = parseAppResponse(eventRequest, eventType)
			log.info("Message to be sent back: "+responseMessage)
			response.contentType = 'text/xml'
			response.setHeader("Content-Length", responseMessage.length().toString())
			return responseMessage
		} catch (OAuthException oae) {
			response.status = 401
			def msg = "Failed to sign event URL: " + eventURL + ". Exception message: " + oae.message
			log.error(msg)
			return msg
		} catch (IOException ioe) {
			response.status = 405
			def msg ="Communication problem with event URL: " + eventURL + ". Exception message: " + ioe.message
			log.error(msg)
			return msg
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
		if (log.debugEnabled){
			log.debug("Response: " + tokenRequest.responseCode + " "
				+ tokenRequest.responseMessage)
		}
		def rootNode = tokenRequest.inputStream.readLines().toString() // maybe XML.parse(tokenRequest) ??
		rootNode = rootNode.substring(1, rootNode.length()-1) // strip square bracket
		rootNode = XML.parse(rootNode)
		if (log.debugEnabled){
			log.debug("Response XML node: "+rootNode)
			log.debug("Response's event type: " + rootNode.type.text())
			log.debug("   Market place: " + rootNode.marketplace.partner.text())
		}
		// make sure payload contains the right Event type and marketplace
		if (rootNode.type.text() != eventType.toString() || !rootNode.marketplace.partner.text()){
			return AppUtil.createFailureResponse(eventType, ReturnCode.INVALID_RESPONSE)
		}
		
		switch (eventType){
			case AppEvent.SUBSCRIPTION_ORDER:
				return createNewSubscription(rootNode.marketplace, rootNode.creator, rootNode.payload)
			case AppEvent.SUBSCRIPTION_CHANGE:
			case AppEvent.SUBSCRIPTION_CANCEL:
			case AppEvent.USER_ASSIGNMENT:
			case AppEvent.USER_UNASSIGNMENT:
			case AppEvent.SUBSCRIPTION_NOTICE:
				return changeOrCancelSubscription(rootNode.marketplace, rootNode.payload, eventType)
			default :
				return AppUtil.createFailureResponse(eventType, ReturnCode.CONFIGURATION_ERROR)
		}
	}
	
	protected String createNewSubscription(def marketplaceXml, def accountXml, def payloadXml){
		def newUserId = AppUtil.generateNewUsername(accountXml, payloadXml.company)
		if (User.findWhere(username: newUserId)){
			return AppUtil.createFailureResponse(AppEvent.SUBSCRIPTION_ORDER, ReturnCode.USER_ALREADY_EXISTS)
		}
		
		Long accountId = AppUtil.addNewSubscription(marketplaceXml, accountXml, payloadXml, newUserId)
		
		if (accountId)
			return AppUtil.createSuccessResponse(AppEvent.SUBSCRIPTION_ORDER, accountId)
		else
			return AppUtil.createFailureResponse(AppEvent.SUBSCRIPTION_ORDER, ReturnCode.UNKNOWN_ERROR)
	}

	protected String changeOrCancelSubscription(def marketplaceXml, def payloadXml, AppEvent eventType){
		def subscription = Subscription.findWhere(id: Long.parseLong(payloadXml.account.accountIdentifier.text()))
		if (!subscription){
			return AppUtil.createFailureResponse(eventType, ReturnCode.ACCOUNT_NOT_FOUND)
		}
		
		if (subscription.marketplace != marketplaceXml.partner.text()){ // validate the right marketplace
			return AppUtil.createFailureResponse(eventType, ReturnCode.CONFIGURATION_ERROR)
		}
		
		boolean actionCompleted = false
		
		switch (eventType){
			case AppEvent.SUBSCRIPTION_CHANGE:
				actionCompleted = AppUtil.changeSubscription(payloadXml, subscription)
				break;
			case AppEvent.SUBSCRIPTION_CANCEL:
				refreshUserCache(subscription)
				actionCompleted = AppUtil.cancelSubscription(payloadXml, subscription)
				break;
			case AppEvent.USER_ASSIGNMENT:
				def newUserId = AppUtil.generateNewUsername(payloadXml.user, subscription.company)
				User userFound = User.findWhere(username: newUserId)
				// A manager exists as long as the subscription exists, if she was unsubscribed, 
				// the app will disable her instead of deleting her from the system. She will be removed 
				// from the users list of the subscription if she was deleted but will remain the manager
				//  of the subscription. Hence the condition below
				if (userFound && subscription.manager != userFound){
					return AppUtil.createFailureResponse(AppEvent.SUBSCRIPTION_ORDER, ReturnCode.USER_ALREADY_EXISTS)
				}
				actionCompleted = AppUtil.createUser(newUserId, userFound, payloadXml, subscription)
				break;
			case AppEvent.USER_UNASSIGNMENT:
				refreshUserCache(subscription)
				actionCompleted = AppUtil.removeUser(payloadXml, subscription)
				break;
			case AppEvent.SUBSCRIPTION_NOTICE:
				actionCompleted = AppUtil.createSubscriptionNotice(payloadXml, subscription)
		}
		
		if (actionCompleted)
			return AppUtil.createSuccessResponse(eventType)
		else
			return AppUtil.createFailureResponse(eventType, ReturnCode.UNKNOWN_ERROR)
		
	}
	
	protected refreshUserCache(subscription){
		subscription.users.each{
			userCache.removeUserFromCache it.username
		}
	}

}


