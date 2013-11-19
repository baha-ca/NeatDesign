class UrlMappings {

	static mappings = {
		/*"/login/auth" {
			controller = 'openId'
			action = 'auth'
		 }
		
		"/login/openIdCreateAccount" {
			controller = 'openId'
			action = 'createAccount'
		}*/
		
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}

		
		"/admin"(view:"/indexAdmin")
		"/"(view:"/index")
		"500"(view:'/error')
	}
}
