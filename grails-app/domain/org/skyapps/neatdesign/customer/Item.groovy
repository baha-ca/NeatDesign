package org.skyapps.neatdesign.customer

class Item {

	String quantity
	String unit
	
	static belongsTo = [order:Order]
	
    static constraints = {
    }
}