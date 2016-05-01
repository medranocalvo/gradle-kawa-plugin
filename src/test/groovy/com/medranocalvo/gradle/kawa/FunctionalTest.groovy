/*
 * Copyright (C) 2016 Adrián Medraño Calvo
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * Written by:
 *     Adrián Medraño Calvo <adrian@medranocalvo.com>
 */
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Unroll

import groovy.json.JsonSlurper

import java.io.File

import static org.junit.Assert.assertEquals

import static org.gradle.testkit.runner.TaskOutcome.*

@Unroll
public class FunctionalTest extends Specification {
    private static final String TEST_PARAMETERS_PROPERTY_PREFIX = 'com.medranocalvo.gradle.kawa.test.'
    private File testProjectDir
    private static Map<String,?> parameters = [:];
    @Rule private TemporaryFolder testBuildDir = new TemporaryFolder()
    @Rule private TemporaryFolder testCacheDir = new TemporaryFolder()

    def ':assemble [Project=#projectDir, Gradle=#gradleVersion, AndroidPluginForGradle=#androidPluginVersion]'() {
        given:
        // Set up the Gradle build:
        // - projectDir
        // - cacheDir
        // - buildDir
        // The last one needs the example projects to use the nebula
        // gradle-override-plugin, in order to be able to set project properties
        // via command-line arguments.
        File buildDir = testBuildDir.getRoot()
        File cacheDir = testCacheDir.getRoot()
        Map<String, String> overrideMap = [:]
        overrideMap['org.gradle.project.buildDir'] = buildDir
        overrideMap['org.gradle.project.androidPluginVersion'] = androidPluginVersion

        List<String> extraArgs = []
        extraArgs.add "--project-cache-dir=${cacheDir}"
        extraArgs.addAll overrideMap.collect { k, v -> "-D${k}=${v}" }

        // Print them out for debugging.
        System.err.println "test.projectDir=${projectDir}"
        System.err.println "test.buildDir=${buildDir}"
        System.err.println "test.extraArgs=${extraArgs}"
        System.err.println "test.gradleVersion=${gradleVersion}"

        when:
        BuildResult result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withGradleVersion(gradleVersion)
            .withArguments(*extraArgs, ":assembleDebug")
            .withPluginClasspath()
            .build()

        then:
        // Easily look at the output in the test report.
        System.out.println result.getOutput()
        assertEquals(SUCCESS, result.task(":assembleDebug").getOutcome())

        where:
        [projectDir, gradleVersion, androidPluginVersion] << getCombinations(['simple'])
    }

    /*
     * Get project/version combinations to test.
     * Note that Android plugin 2.0.0 needs Gradle greater than 2.9.
     */
    def getCombinations(projectNames) {
        List<File> projectDirs = projectNames.collect { projectName ->
            new File(examplesDir, projectName)
        }
        List<List<String>> versions = versionCombinations.collect {
            [it.gradleVersion, it.androidPluginVersion]
        }
        combine projectDirs, versions;
    }

    /*
     * Add a new variant to a list of combinations.
     * @p combinations is a list generated by the combinations method.
     */
    List<List<Object>> combine(List<Object> items, List<List<Object>> combs) {
        items.collect { elem ->
            combs.collect {
                [elem, *it]
            }
        }.collectMany { it };  // flatten one level.
    }

    String getExamplesDir() {
        getTestParameter('examplesDir')
    }

    def getVersionCombinations() {
        getTestParameter "versionCombinations"
    }

    /*
     * This roundabout is due to the the primitive system for passing data
     * from the Gradle to the test JVM: system properties.  They are
     * basicall Map<String, String>; in order to pass structured data we use
     * JSON.
     *
     * Each versionCombination is a map specifying a compatible Gradle and
     * Android plugin versions. versionCombinations is a list of those.
     */
    def getTestParameter(String parameterName) {
        if (!parameters.containsKey(parameterName)) {
            def slurper = new JsonSlurper()
            parameters[parameterName] = slurper.parseText System.properties[TEST_PARAMETERS_PROPERTY_PREFIX + parameterName]
        }
        parameters[parameterName]
    }
}
