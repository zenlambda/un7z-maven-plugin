Maven Un7z Plugin
=================

This maven plugin extracts archives using the 7z library.

Example Usage
-------------

	<repositories>
	    <repository>
		<id>zenlambda-releases</id>
		<url>http://mvn-repo.zenlambda.com/releases</url>
	    </repository>
	</repositories>


	<build>
		<plugins>
			...

				<plugin>
					<groupId>com.zenlambda.maven</groupId>
					<artifactId>un7z-maven-plugin</artifactId>
					<version>0.0.1</version>
					<inherited>false</inherited>
					<executions>
						<execution>
							<phase>package</phase>
							<goals>
								<goal>unpack</goal>
							</goals>
						</execution>
					</executions>

					<configuration>
						<srcArchive>${project.build.directory}/jogamp-all-platforms.7z</srcArchive>
						<destDir>${project.build.directory}</destDir>
						<failIfNotFound>true</failIfNotFound>
						<overwrite>false</overwrite>
					</configuration>
				</plugin>

			...
		<plugins>
			
	<build>


Known Issues
------------

Extraction is very very slow (about 5 minutes to extract a 17MB 7z archive).

This is because the 'simple' 7z-jbindings interface is used [1].

Build Dependencies
------------------

A version of maven that supports http redirect is required to use the zenlambda-releases 
repository at the time of writing. This is needed to download the sevenzipjbindings 
dependency, unless it is 'built' separately. Maven version 3.0.4 should work fine.

Acknowledgements
----------------

This plugin was developed using code from Ondřej Žižka's maven-unzip-plugin [2].

References
----------

[1] http://sevenzipjbind.sourceforge.net/basic_snippets.html#extraction-single-file-simple-int

[2] http://www.pohlidame.cz/insolvencni-rejstrik/maven-unzip-plugin.html

