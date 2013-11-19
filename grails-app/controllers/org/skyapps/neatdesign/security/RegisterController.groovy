package org.skyapps.neatdesign.security

import org.springframework.security.access.annotation.Secured;

@Secured(['IS_AUTHENTICATED_ANONYMOUSLY'])
class RegisterController extends grails.plugin.springsecurity.ui.RegisterController {
}
