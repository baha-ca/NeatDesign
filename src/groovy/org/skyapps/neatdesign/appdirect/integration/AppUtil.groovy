package org.skyapps.neatdesign.appdirect.integration

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.logging.Log
import org.apache.log4j.Logger;
import org.skyapps.neatdesign.customer.Company;
import org.skyapps.neatdesign.customer.Item
import org.skyapps.neatdesign.customer.Order
import org.skyapps.neatdesign.customer.Subscription
import org.skyapps.neatdesign.security.Role;
import org.skyapps.neatdesign.security.User;
import org.skyapps.neatdesign.security.UserRole;

class AppUtil {
	
	Log log = Logger.getLogger(AppUtil.class)
	
	static public String generateNewUsername(def accountXml, def companyXml) {
		String uniqueAccountId = generateUsernamePrefix(accountXml)
		uniqueAccountId += companyXml.name.text().replaceAll(' ', '').capitalize()
		return uniqueAccountId
	}
	
	static public String generateNewUsername(def accountXml, Company company) {
		String uniqueAccountId = generateUsernamePrefix(accountXml)
		uniqueAccountId += company.name.toString().capitalize()
		return uniqueAccountId
	}
	
	static private String generateUsernamePrefix(accountXml){
		def firstName = accountXml.firstName.text()
		String uniqueAccountId = accountXml.lastName.text().charAt(0).toString()
		uniqueAccountId += firstName.replaceAll(' ', '').capitalize()
		return uniqueAccountId
	}
	
	static public String createSuccessResponse(AppEvent eventType, Long subscriptionId = null){
		def xmlScript = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><result><success>true</success>'
		
		switch(eventType){
			case AppEvent.SUBSCRIPTION_ORDER:
				xmlScript += '<message>Account creation successful</message><accountIdentifier>' + subscriptionId + '</accountIdentifier>'
				break
			case AppEvent.SUBSCRIPTION_CHANGE:
				xmlScript += '<message>Account change successful</message>'
				break
			case AppEvent.SUBSCRIPTION_CANCEL:
				xmlScript += '<message>Account cancel successful</message>'
				break
			case AppEvent.SUBSCRIPTION_NOTICE:
				break
		}
		
		return xmlScript + "</result>"
	}
	
	static public String createFailureResponse(AppEvent eventType, ReturnCode returnCode){
		def failureXmlScript = '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<result>
    <success>false</success>
    <errorCode>USER_ALREADY_EXISTS</errorCode>
    <message>Account creation failure</message>
</result>'''
		
		def xmlScript = failureXmlScript.replace("<errorCode>USER_ALREADY_EXISTS</errorCode>", "<errorCode>" + returnCode + "</errorCode>")
		
		switch(eventType){
			case AppEvent.SUBSCRIPTION_CHANGE:
				xmlScript = xmlScript.replace("<message>Account creation failure</message>", "<message>Account change failure</message>")
				break
			case AppEvent.SUBSCRIPTION_CANCEL:
				xmlScript = xmlScript.replace("<message>Account creation failure</message>", "<message>Account cancel failure</message>")
				break
			case AppEvent.SUBSCRIPTION_NOTICE:
				xmlScript = xmlScript.replace("<message>Account creation failure</message>", "<message>Subscription notice failure</message>")
				break
		}
		
		return xmlScript
	}
	
	static public Long addNewSubscription(def marketplaceXml, def accountXml, def payloadXml, def newUsername){
		Subscription subscription = null
		boolean created = User.withTransaction { status ->
			
			def password = generateRandomPassword(newUsername)
			
			def user = User.newInstance(username: newUsername,
										password: password,
										enabled: true)

			user.addToOpenIds(url: accountXml.openId.text())

			if (!user.save()) {
				return false
			}

			// create role security
			UserRole.create user, Role.findWhere("authority": "ROLE_OPENID")
			UserRole.create user, Role.findWhere("authority": "ROLE_USER")
			UserRole.create user, Role.findWhere("authority": "ROLE_MANAGER") // subscription creator is the manager
			
			//create company
			Company company = Company.get(payloadXml.company.uuid.text())
			if (!company){// create company if it doesn't exist
				company = Company.newInstance(
					id: payloadXml.company.uuid.text(),
					name: payloadXml.company.name.text(),
					email: payloadXml.company.email.text(),
					phoneNumber: payloadXml.company.phoneNumber.text(),
					website: payloadXml.company.website.text()
					)
				if (!company.save()){
					return false
				}
			}
			
			// create Order
			Order order = Order.newInstance(edition:payloadXml.order.editionCode.text())

			addItemsToOrder(order, payloadXml.order.item)
			
			//finally create the subscription (Account)
			subscription = Subscription.newInstance(
				marketplace: marketplaceXml.partner.text(),
				marketplaceUrl: marketplaceXml.baseUrl.text(),
				company: company, 
				manager:user, 
				order:order)
			subscription.addToUsers(user)
			if(!subscription.save()){
				return false
			}

			return true
		}
		
		if (created && subscription){
			return subscription.id
		}else{
			return null
		}
	}
	
	static private String generateRandomPassword(username){
		return "secretAbc^"+username+RandomStringUtils.random(6, true, true) // for now, default password is secretAbc^+newAccountId+some random string used for new user creation for non OpenID authentication only
	}
	
	static public Boolean changeSubscription(def payloadXml, Subscription subscription){
		if (!payloadXml.order.item){
			return false
		}
		
		return subscription.order.withTransaction {
			subscription.order.items*.delete() // TODO maybe keep old items for reference and auditing -> new status field needed
			addItemsToOrder(subscription.order, payloadXml.order.item)
			if (!subscription.order.save()){
				return false
			}
			
			return true
		}
	}
	
	static public Boolean cancelSubscription(def payloadXml, Subscription subscription){
		return subscription.withTransaction {
			subscription.manager = null
			subscription.users.clear()
			if (!deleteAllUsers(subscription)){
				return false
			}
			
			Company companyObj = subscription.company
			subscription.delete flush:true
			
			companyObj.delete flush:true
			
			return true
		}
	}
	
	static public Boolean createUser(String newUsername, User existingUser=null, def payloadXml, Subscription subscription){
		return subscription.withTransaction {
			User newUser = existingUser
			if (newUser){// manager is not deleted, but disabled
				newUser.enabled = true
				
				if (!newUser.save()) {
					return false
				}
					
			} else {
				newUser = User.newInstance(
					username: newUsername,
					password: generateRandomPassword(newUsername),
					enabled: true
					)
				
				newUser.addToOpenIds(url: payloadXml.user.openId.text())
				
				if (!newUser.save()) {
					return false
				}
				
				UserRole.create newUser, Role.findWhere("authority": "ROLE_OPENID")
				UserRole.create newUser, Role.findWhere("authority": "ROLE_USER")
			}
			
		
			
			subscription.addToUsers(newUser)
			if (!subscription.save()){
				return false
			}
			
			return true
		}
	}
	
	static public Boolean removeUser(def payloadXml, Subscription subscription){
		// since this app does not save user emails, the user name must be recreated from the payload xml elements
		String username = generateNewUsername(payloadXml.user, subscription.company)
		User userToDelete = User.findWhere("username":username)
		return subscription.withTransaction {
			subscription.users.remove(userToDelete)
			
			if (userToDelete == subscription.manager){ // if user to delete is the manager, then disable instead of delete, TODO allow setting a different manager for the subscription
				userToDelete.enabled = false
				if (!userToDelete.save()){
					return false
				}
				
				return true
			}
			
			UserRole.removeAll(userToDelete)
			userToDelete.delete flush:true
			
			if (!subscription.save()){
				return false
			}
			
			return true
		}
	}
	
	static private Boolean deleteAllUsers(subscription){
		return subscription.withTransaction {
			subscription.users.each {
				UserRole.removeAll(it)
				it.delete flush: true
			}
			
			return true
		}
	}
	
	static public Boolean createSubscriptionNotice(def payloadXml, Subscription subscription){
		return true; // for now
	}
	
	static private void addItemsToOrder(Order order, def itemsXml){
		itemsXml.each(){// create items
			Item itm = Item.newInstance()
			it.children().each{field->
				itm[field.name()] = field.text() // Item domain object has the same field names as the XML element names
			}

			order.addToItems(itm)
		}
	}
	
}
