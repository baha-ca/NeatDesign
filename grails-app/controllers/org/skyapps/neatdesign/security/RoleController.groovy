package org.skyapps.neatdesign.security

import org.springframework.security.access.annotation.Secured;

@Secured(['ROLE_ADMIN'])
class RoleController extends grails.plugin.springsecurity.ui.RoleController {
}
