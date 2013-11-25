package org.skyapps.neatdesign.customer

class Order {
	String edition
	
	static belongsTo = [subscription: Subscription]
	
	static hasMany = [items: Item]
	
    static constraints = {
		edition blank:false
		items empty: false
    }
	
	static mapping = {
		table "`Order`" // the word order is reserved as a table name
		//edition column: "marketEdition"
	}
}