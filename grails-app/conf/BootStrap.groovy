import org.skyapps.neatdesign.security.Role
import org.skyapps.neatdesign.security.User
import org.skyapps.neatdesign.security.UserRole

class BootStrap {

    def init = { servletContext ->
		def roleAdmin = new Role(authority: 'ROLE_ADMIN').save()
		def roleUser = new Role(authority: 'ROLE_USER').save()
		
		def user = new User(username: 'user', password: 'password', enabled: true).save() 
		def admin = new User(username: 'admin', password: 'password', enabled: true).save()
		
		UserRole.create user, roleUser 
		UserRole.create admin, roleUser 
		UserRole.create admin, roleAdmin, true 
		
    }
	
    def destroy = {
    }
}

