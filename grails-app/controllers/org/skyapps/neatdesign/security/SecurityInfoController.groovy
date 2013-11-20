package org.skyapps.neatdesign.security

import org.springframework.security.access.annotation.Secured;

@Secured(['ROLE_ADMIN'])
class SecurityInfoController extends grails.plugin.springsecurity.ui.SecurityInfoController {
	
	def indexAdmin() {
		[]
	}
}
