Maven Un7z Plugin
=================

This maven plugin extracts archives using the 7z library.

Example Usage
-------------

	<build>
		<plugins>
			...

				<plugin>
					<groupId>com.zenlambda.maven</groupId>
					<artifactId>un7z-maven-plugin</artifactId>
					<version>0.0.1-SNAPSHOT</version>
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

References
----------

[1] http://sevenzipjbind.sourceforge.net/basic_snippets.html#extraction-single-file-simple-int
