import grails.plugin.springsecurity.SpringSecurityUtils
import org.skyapps.neatdesign.appdirect.integration.SimpleUrlLogoutSuccessHandlerMarketRedirect

beans = {
	def conf = SpringSecurityUtils.securityConfig
	
	logoutSuccessHandler(SimpleUrlLogoutSuccessHandlerMarketRedirect) {
		redirectStrategy = ref('redirectStrategy')
		appService = ref('appService')
		defaultTargetUrl = conf.logout.afterLogoutUrl
	}
}
