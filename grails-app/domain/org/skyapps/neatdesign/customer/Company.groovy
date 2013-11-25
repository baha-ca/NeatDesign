package org.skyapps.neatdesign.customer

class Company {
	String id
    String email
    String name
    String phoneNumber
    String website
	
	static mapping = {
		id generator: 'uuid'
	}
	
    static constraints = {
    }
}
