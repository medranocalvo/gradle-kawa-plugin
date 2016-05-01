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

/*
 * Kawa plugin -- integrated with android plugin.
 */
package com.medranocalvo.gradle.kawa

import org.gradle.api.GradleException
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.ProjectConfigurationException
import org.gradle.api.Task
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.internal.file.FileResolver

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.BasePlugin
import com.android.build.gradle.api.BaseVariant

import javax.inject.Inject

class KawaAndroidPlugin implements Plugin<Project> {
    private static final String KAWA_ANDROID_PLUGIN_NAME = "com.medranocalvo.gradle.gradle-android-kawa-plugin"
    private static final String KAWA_ANDROID_SOURCESET_NAME = "kawa"
    private static final String KAWA_ANDROID_EXTENSION_NAME = "kawa"
    private static final Logger LOGGER = Logging.getLogger(KawaAndroidPlugin)
    /* Depended upon plugins. */
    private static final List<String> KAWA_ANDROID_ANDROID_PLUGIN_IDS = [
        'com.android.application',
        'android',
        'com.android.library',
        'android-library'
    ]

    /* Whether the plugin has actually been applied prior to project evaluation. */
    private boolean isApplied = false;

    private final FileResolver fileResolver

    @Inject
    public KawaAndroidPlugin(FileResolver fileResolver) {
        this.fileResolver = fileResolver
    }

    /**
     * Check that some Android plugin has been aplied.
     */
    @Override
    void apply(Project project) {
        // Apply the base kawa plugin directly, in order to have the top-level
        // kawa extension object.
        project.plugins.apply(KawaBasePlugin)
        KAWA_ANDROID_ANDROID_PLUGIN_IDS.each { androidPluginId ->
            project.plugins.withId androidPluginId, { androidPlugin ->
                if (!isApplied) {
                    apply(project, androidPlugin)
                } else {
                    LOGGER.debug "Found plugin ${androidPlugin}, but KawaAndroidPlugin has been already applied";
                }
            }
        }
    }

    void apply(Project project, BasePlugin androidPlugin) {
        isApplied = true;
        LOGGER.debug('Applying KawaAndroidPlugin plugin')
        def androidExtension = getAndroidExtension(project)

        // Add Kawa extension:
        // - Project (done by KawaBasePlugin)
        // - Android block
        // - Each buildType
        // - Each buildFlavor
        addKawaExtension androidExtension
        androidExtension.buildTypes.all { addKawaExtension it }
        androidExtension.productFlavors.all { addKawaExtension it }

        // Extend the source sets to include Kawa sources.
        extendSourceSets(project)
        androidExtension.buildTypes.whenObjectAdded { extendSourceSets(project) }
        androidExtension.productFlavors.whenObjectAdded { extendSourceSets(project) }

        // Make sure the Android plugin is done.
        project.afterEvaluate {
            this.getAndroidVariants(project).all { variant ->
                LOGGER.debug("Configuring variant $variant.name for Kawa")
                // Tasks
                Task javaCompile = variant.javaCompile
                LOGGER.debug("Found javaCompile task for variant: ${javaCompile.name}")

                String variantName = variant.name.capitalize();
                Task preDex = ["preDex${variantName}", "transformClassesWithDexFor${variantName}"].findResult {
                    project.tasks.findByName(it)
                }
                LOGGER.debug("Found preDex task for variant: ${preDex.name}")

                def sourceSets = variant.variantData.variantConfiguration.sortedSourceProviders
                def sourceFiles = sourceSets.inject([]) { acc, sourceSet ->
                    acc + sourceSet."$KAWA_ANDROID_SOURCESET_NAME".files
                }
                def runtimeJars = this.getRuntimeJars(project, androidPlugin)
                def kawaClasspath = javaCompile.classpath +
                                    project.files(runtimeJars) +
                                    // TODO: is this all right?  When compiling
                                    //       just .scm files Kawa already knows
                                    //       about them, but when mixing
                                    //       pre-compiled classes with .scm
                                    //       (e.g. R.java), we need to tell Kawa
                                    //       about where to find the
                                    //       pre-compiled classes.
                                    project.files(javaCompile.destinationDir)
                def kawaCompileName = javaCompile.name.replace("Java", "Kawa")
                LOGGER.debug("Configuring Kawa compile task [$kawaCompileName]")
                def kawaCompile = project.task(kawaCompileName, type: KawaCompile)
                kawaCompile.source = sourceFiles;
                kawaCompile.destinationDir = javaCompile.destinationDir;
                // Set up classpath, including Android JARs.
                kawaCompile.classpath = kawaClasspath;
                // Apply configuration from each found extension, outer to
                // inner.
                def kawaExtensions = getKawaExtensions project,
                                                       androidExtension,
                                                       variant.buildType,
                                                       variant.mergedFlavor
                kawaExtensions.each { kawaCompile.applyExtension it; }
                // Run after Java.
                kawaCompile.dependsOn javaCompile;
                // Run before Dex.
                preDex.dependsOn kawaCompile;

            }
        }
        LOGGER.debug("Detected Android plugin version ${getAndroidPluginVersion(project)}")
    }

    /**
     * Retrieve the android plugin.
     */
    private BasePlugin getAndroidPlugin(project) {
        return KAWA_ANDROID_ANDROID_PLUGIN_IDS.collect {
            project.plugins.findPlugin(it)
        } .find {
            it != null
        }
    }

    // Get Android Gradle plugin version.
    private String getAndroidPluginVersion(Project project) {
        def dependency = [project, project.rootProject].collect {
            it.buildscript.configurations.classpath.resolvedConfiguration.firstLevelModuleDependencies.find {
                it.moduleGroup == 'com.android.tools.build' && it.moduleName == 'gradle'
            }
        }.find()
        if (!dependency) {
            throw new ProjectConfigurationException(
                "Cannot determine Android build tools version", null)
        }
        dependency.moduleVersion
    }

    /*
     * Retrieve the android extension.
     */
    private BaseExtension getAndroidExtension(Project project) {
        project.extensions.getByName 'android'
    }

    /*
     * Retrieve a kawa extensions found in the argumenst.
     */
    private List<KawaOptions> getKawaExtensions(Object... objs) {
        def kawaExtensions = objs.toList().findResults {
            if (it.hasProperty("extensions")) {
                it.extensions.findByType KawaOptions;
            }
        }
        LOGGER.debug("Found extensions: ${kawaExtensions}")
        kawaExtensions
    }

    /*
     * Adds KawaOptions to the object's extensions.
     */
    private void addKawaExtension(ExtensionAware obj) {
        LOGGER.debug "Adding Kawa extension to $obj"
        obj.extensions.create(KAWA_ANDROID_EXTENSION_NAME, KawaOptions)
    }

    /*
     * Get Android runtime JARs.
     */
    private NamedDomainObjectContainer<?> getAndroidVariants(Project project) {
        def androidPlugin = getAndroidPlugin project
        def androidExtension = getAndroidExtension project
        def isLibraryPlugin = androidPlugin.class.name.endsWith '.LibraryPlugin'
        def container = project.container BaseVariant
        container.addAll androidExtension.testVariants
        container.addAll (isLibraryPlugin
                          ? androidExtension.libraryVariants
                          : androidExtension.applicationVariants)
        container
    }

    /*
     * Get Android Gradle runtime JARs.
     */
    def getRuntimeJars(Project project, BasePlugin plugin) {
        def result
        switch (getAndroidPluginVersion(project)) {
            case ~/0\.9\..*/:
                result = plugin.runtimeJars
                break
            case ~/0\.10\..*/:
            case ~/0\.11\..*/:
            case ~/0\.12\..*/:
            case ~/0\.13\..*/:
            case ~/0\.14\..*/:
            case ~/1\.0\..*/:
                result = plugin.bootClasspath
                break
            case ~/1\.1\..*/:
                result = plugin.androidBuilder.bootClasspath
                break
            case ~/1\.5\..*/:
            default:
                result = plugin.androidBuilder.getBootClasspath(false)
                break;
        }
        LOGGER.debug("Retrieved runtime JARs: ${result}")
        result
    }

    private void extendSourceSets(Project project) {
        def androidExtension = getAndroidExtension(project)
        KawaSourceDirectorySetFactory sourceDirectorySetFactory =
                new KawaSourceDirectorySetFactory(project, this.fileResolver)
        androidExtension.sourceSets.each { sourceSet ->
            if (sourceSet.extensions.findByName(KAWA_ANDROID_SOURCESET_NAME) == null) {
                LOGGER.debug("Adding Kawa sourceSet to ${sourceSet.name}")
                def kawaSourceSet = new KawaSourceSet(sourceSet.name, sourceDirectorySetFactory)
                def kawaSrcDir = ["src", sourceSet.name, KawaSourceSet.FILE_EXTENSION].join(File.separator)
                kawaSourceSet.kawa.srcDir(kawaSrcDir)
                sourceSet.extensions.add(KAWA_ANDROID_SOURCESET_NAME, kawaSourceSet.getKawa())
            }
        }
    }
}
