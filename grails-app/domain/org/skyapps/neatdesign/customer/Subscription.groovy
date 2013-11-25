package org.skyapps.neatdesign.customer

import org.skyapps.neatdesign.security.Role;
import org.skyapps.neatdesign.security.User;
import org.skyapps.neatdesign.security.UserRole;

class Subscription {

	String marketplace
	String marketplaceUrl
	Company company
	User manager
	Order order
	
	static hasMany = [users: User]
	
    static constraints = {
		marketplace blank: false
		marketplaceUrl blank: false, url: true
		company blank: false
		/*manager blank:false, validator: {val, obj -> 
			val in obj.users; 
			UserRole.get(val.id, Role.findByAuthority("ROLE_MANAGER").id) != null
		}*/
    }
}
