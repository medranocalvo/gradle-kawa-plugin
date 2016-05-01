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
 * Kawa source set integration.
 */
package com.medranocalvo.gradle.kawa

import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.internal.file.DefaultSourceDirectorySet
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.util.ConfigureUtil

class KawaSourceSet {
    private final static Logger logger = Logging.getLogger(KawaSourceSet)
    public static final String LANG = 'kawa'
    public static final String FILE_EXTENSION = "scm"

    private final SourceDirectorySet kawa

    KawaSourceSet(String displayName, KawaSourceDirectorySetFactory sourceDirectorySetFactory) {
        this.kawa =
            sourceDirectorySetFactory.create(LANG, "${displayName} ${LANG.capitalize()} source")
        this.kawa.filter.include("**/*.${FILE_EXTENSION}")
        this.logger.info("including **/*.${FILE_EXTENSION}")
        this.logger.info("Files are ${this.kawa.collect { "$it" }}");
    }

    SourceDirectorySet getKawa() {
        this.kawa
    }

    KawaSourceSet kawa(Closure configureClosure) {
        ConfigureUtil.configure(configureClosure, getKawa())
        return this
    }
}

/*
 * Create a SourceDirectorySet independent of Gradle version.
 * The issue is that prior to Gradle 0.12 DefaultSourceDirectorySet (internal
 * API) was used, but then its constructor got more arguments.  We'd use
 * SourceDirectorySetFactory, but it only exists on Gradle >= 0.12.
 *
 * See https://discuss.gradle.org/t/defaultsourcedirectoryset-alternative/15193
 */
class KawaSourceDirectorySetFactory {
    private final FileResolver fileResolver
    private final ProjectInternal project
    private final Class sourceDirectorySetFactoryClass
    private final static Logger logger = Logging.getLogger(KawaSourceDirectorySetFactory)
    KawaSourceDirectorySetFactory(ProjectInternal project, FileResolver fileResolver) {
        this.fileResolver = fileResolver
        this.project = project
        try {
            sourceDirectorySetFactoryClass =
                "org.gradle.api.internal.file.SourceDirectorySetFactory" as Class
        } catch (Exception e) {
            this.logger.debug("Falling back to DefaultSourceDirectorySet: ${e.toString()}")
        }
    }
    DefaultSourceDirectorySet create(String name) {
        if (sourceDirectorySetFactoryClass) {
            project.getServices().get(sourceDirectorySetFactoryClass).create(name);
        } else {
            return new DefaultSourceDirectorySet(name, this.fileResolver);
        }
    }
    DefaultSourceDirectorySet create(String name, String displayName) {
        if (sourceDirectorySetFactoryClass) {
            project.getServices().get(sourceDirectorySetFactoryClass).create(name, displayName);
        } else {
            return new DefaultSourceDirectorySet(name, displayName, this.fileResolver);
        }
    }
}
