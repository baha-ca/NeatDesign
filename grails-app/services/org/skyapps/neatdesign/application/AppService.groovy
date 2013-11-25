package org.skyapps.neatdesign.application

import org.skyapps.neatdesign.customer.Subscription

class AppService {

    def retrieveSubscriptionfromOpenId(def openId) {
		def query = Subscription.where { users { opendIds { url == openId } } }
		Subscription subscription = query.find() // TODO handle user registered with multiple apps
		if (subscription){
			return subscription.marketplaceUrl
		}
		return ""
    }
}
