<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>Welcome to NeatDesign</title>
		<style type="text/css" media="screen">
			#status {
				background-color: #eee;
				border: .2em solid #fff;
				margin: 2em 2em 1em;
				padding: 1em;
				width: 12em;
				float: left;
				-moz-box-shadow: 0px 0px 1.25em #ccc;
				-webkit-box-shadow: 0px 0px 1.25em #ccc;
				box-shadow: 0px 0px 1.25em #ccc;
				-moz-border-radius: 0.6em;
				-webkit-border-radius: 0.6em;
				border-radius: 0.6em;
			}

			.ie6 #status {
				display: inline; /* float double margin fix http://www.positioniseverything.net/explorer/doubled-margin.html */
			}

			#status ul {
				font-size: 0.9em;
				list-style-type: none;
				margin-bottom: 0.6em;
				padding: 0;
			}

			#status li {
				line-height: 1.3;
			}

			#status h1 {
				text-transform: uppercase;
				font-size: 1.1em;
				margin: 0 0 0.3em;
			}

			#page-body {
				margin: 2em 1em 1.25em 18em;
			}

			h2 {
				margin-top: 1em;
				margin-bottom: 0.3em;
				font-size: 1em;
			}

			p {
				line-height: 1.5;
				margin: 0.25em 0;
			}

			#controller-list ul {
				list-style-position: inside;
			}

			#controller-list li {
				line-height: 1.3;
				list-style-position: inside;
				margin: 0.25em 0;
			}

			@media screen and (max-width: 480px) {
				#status {
					display: none;
				}

				#page-body {
					margin: 0 1em 1em;
				}

				#page-body h1 {
					margin-top: 0;
				}
			}
		</style>
	</head>
	<body>
		<a href="#page-body" class="skip"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div id="status" role="complementary">
			<h1>Application Status</h1>
			<ul>
				<sec:ifLoggedIn>
				<li>App version: <g:meta name="app.version"/></li>
				<li>Basic Security</li>
				</sec:ifLoggedIn>
				<li>Found on DirectApp Marketplaces</li>
				<li></li>
				<li></li>
				<li></li>
				<sec:access expression="hasRole('ROLE_ADMIN')">
				<li><g:link controller="securityInfo" action="indexAdmin">Admin Access</g:link></li>
				</sec:access>
			</ul>
			
		</div>
		<div id="page-body" role="main">
			<h1>Welcome to SkyApps NeatDesign</h1>
			<p>Congratulations, you have successfully landed on SkyApps first application! At the moment
			   this is the default page, more pages will be added soon. The app helps designers show-case their neatest products.</p>

			<sec:ifAllGranted roles="ROLE_MANAGER">
			<h2>You are the manager of this subscription</h2>
			</sec:ifAllGranted>
			<%--div id="controller-list" role="navigation">
				<h2>Available Controllers:</h2>
				<ul>
					<g:each var="c" in="${grailsApplication.controllerClasses.sort { it.fullName } }">
						<li class="controller"><g:link controller="${c.logicalPropertyName}">${c.fullName}</g:link></li>
					</g:each>
				</ul>
			</div>
			<p><g:link controller="OAuth" action="signRequest">Sign Outgoing Request: https://www.appdirect.com/AppDirect/rest/api/events/dummyChange</g:link></p>
			<p><g:link controller="OAuth" action="signURL">Sign URL: https://www.appdirect.com/AppDirect/finishorder?success=true&accountIdentifer=Alice</g:link></p--%>
			
		</div>
		
	</body>
</html>
