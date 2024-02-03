package io.dmcs.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin

class DmcsGradlePlugin implements Plugin<Project> {

	 private List<Project> configuredProjects = []

	 @Override
	void apply(Project project) {
		applyPlugins(project)
	}

	private static void applyPlugins(Project project) {

		project.afterEvaluate {

			// ensure we only do this once for each project
			if (configuredProjects.contains(project))
				return

			configuredProjects.add(project)

			configureJavaProject(project)

			// also apply to sub-projects
			project.subprojects.each { sub ->
					applyPlugins(sub)
			}

		}
	}

	 private static void configureJavaProject(Project project) {

		 boolean isPlatform = project.plugins.findPlugin("java-platform") != null
		 boolean isJava = project.plugins.findPlugin('java') ||
				 project.plugins.findPlugin('java-library') ||
				 project.plugins.findPlugin('groovy')

		 if (!isJava && !isPlatform)
			 return

		 if (isJava) {

			 project.java {
				 withSourcesJar()
			 }
		 }

		 // apply the git properties plugin if we have the java plugin
		 if (isJava && !project.pluginManager.hasPlugin('com.gorylenko.gradle-git-properties')) {
			 project.pluginManager.apply('com.gorylenko.gradle-git-properties')
		 }

		 def publishPlugin = project.plugins.findPlugin(MavenPublishPlugin)
		 if (publishPlugin) {

			 // add repository config
			 def pubExt = project.extensions.findByType(PublishingExtension)
			 pubExt.with {

				 if (!publications.find { it.name == 'maven' }) {
					 if (isJava)
						 publications.create('maven', MavenPublication, { it.from project.components.java })
					 else if (isPlatform)
						 publications.create('maven', MavenPublication, { it.from project.components.javaPlatform })
				 }

				 if (repositories.isEmpty()) {

					 pubExt.repositories {
						 maven {
							 name = 'GitHubPackages'
							 url = 'https://maven.pkg.github.com/dawiemalan/servicebus'
							 credentials {
								 username = System.getenv("GITHUB_ACTOR")
								 password = System.getenv("GITHUB_TOKEN")
							 }
						 }
					 }
				 }
			 }
		 }
	 }
}
