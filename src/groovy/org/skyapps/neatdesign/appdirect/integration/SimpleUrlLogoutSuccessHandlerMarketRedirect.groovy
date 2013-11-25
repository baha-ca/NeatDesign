package org.skyapps.neatdesign.appdirect.integration

import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.apache.ivy.core.search.OrganisationEntry;
import org.skyapps.neatdesign.customer.Subscription;
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler

class SimpleUrlLogoutSuccessHandlerMarketRedirect extends SimpleUrlLogoutSuccessHandler {

			private static final ThreadLocal<Authentication> AUTH_HOLDER = new ThreadLocal<Authentication>()
			def appService
			
			void onLogoutSuccess(HttpServletRequest req, HttpServletResponse resp, Authentication authe) throws IOException ,ServletException {
				AUTH_HOLDER.set authe
				try{
					super.handle(req, resp, authe)
				}finally{
					AUTH_HOLDER.remove()
				}
			}
			
			@Override
			protected String determineTargetUrl(HttpServletRequest req, HttpServletResponse resp) {
				Authentication authe = AUTH_HOLDER.get()
				String defaultUrl = super.determineTargetUrl(req, resp)
				String marketplaceUrl

				if (authe) {
					marketplaceUrl = appService.retrieveSubscriptionfromOpenId(authe.principal.username)
				}
				
				if (marketplaceUrl){
					return marketplaceUrl
				}else{
					return defaultUrl
				}
			}
}
