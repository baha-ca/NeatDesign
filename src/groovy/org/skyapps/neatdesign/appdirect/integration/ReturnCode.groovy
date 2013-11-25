package org.skyapps.neatdesign.appdirect.integration

enum ReturnCode{
	SUCCESS,
	USER_ALREADY_EXISTS,
	USER_NOT_FOUND,
	ACCOUNT_NOT_FOUND,
	MAX_USERS_REACHED,
	UNAUTHORIZED,
	OPERATION_CANCELED,
	CONFIGURATION_ERROR,
	INVALID_RESPONSE,
	UNKNOWN_ERROR
	
	def isSuccess(){
		return this.name == SUCCESS.name
	}
	
}
