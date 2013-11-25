import org.skyapps.neatdesign.security.Role
import org.skyapps.neatdesign.security.User
import org.skyapps.neatdesign.security.UserRole

class BootStrap {

    def init = { servletContext ->
		def roleAdmin = new Role(authority: 'ROLE_ADMIN').save()
		def roleUser = new Role(authority: 'ROLE_USER').save()
		def roleManager = new Role(authority: 'ROLE_MANAGER').save()
		def roleOpenId = new Role(authority: 'ROLE_OPENID').save()
		
		def user = new User(username: 'user', password: 'userPass', enabled: true).save() 
		def admin = new User(username: 'admin', password: 'adminPass', enabled: true).save()
		def manager = new User(username: 'mgr', password: 'mgrPass', enabled: true).save()
		
		UserRole.create user, roleUser 
		UserRole.create admin, roleUser 
		UserRole.create admin, roleAdmin, true 
		
		UserRole.create manager, roleUser
		UserRole.create manager, roleManager
		
    }
	
    def destroy = {
    }
}

