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
 *     Adrián Medraño Calvo <amcalvo@medranocalvo.com>
 */

/*
 * Kawa scheme compile task.
 */
package com.medranocalvo.gradle.kawa

import org.gradle.api.JavaVersion
import org.gradle.api.file.FileCollection
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.compile.AbstractCompile

class KawaCompile extends AbstractCompile {
    private static final Logger LOGGER = Logging.getLogger(KawaAndroidPlugin)

    @Input
    String language = "scheme"

    @Input
    boolean inline = true

    @Input
    Set<String> warnings = [];

    // The default for Kawa 2.0 is Java 7.
    @Input
    String sourceCompatibility = JavaVersion.VERSION_1_7

    @Input
    String targetCompatibility = JavaVersion.VERSION_1_7

    @TaskAction
    protected void compile() {
        /*
         * Clunky anonymous task.  See
         * <http://permalink.gmane.org/gmane.comp.programming.tools.gradle.user/6410>
         */
        def run = project.task(type: JavaExec) { }.configure {
            main = 'kawa.repl'
            classpath = this.classpath
            args = []
            args += ['-d', this.destinationDir]
            if (!this.inline) {
                args += ['--no-inline']
            }
            this.warnings.each {
                args += ["--warn-${it}"];
            }
            args += ["--${this.language}"]
            args += ['-C']
            args += this.source as List
        }
        LOGGER.debug("Classpath is ${classpath.getAsPath()}")
        LOGGER.debug("Sources are ${source}")
        LOGGER.debug("Inline is ${inline}")
        LOGGER.debug("Destination directory is ${destinationDir}")
        LOGGER.debug("Command line is ${run.getCommandLine()}")
        run.exec()
        // Establish that we did something.
        setDidWork(true)
    }

    /**
     * Configure the task as per the extension object.
     */
    void applyExtension(KawaOptions ext) {
        if (ext.language != null) {
            LOGGER.debug "Overriding language with \"${ext.language}\"";
            this.language = ext.language;
        }
        if (ext.inline != null) {
            LOGGER.debug "Overriding inline with \"${ext.inline}\"";
            this.inline = ext.inline;
        }
        if (ext.warn) {
            LOGGER.debug "Extending warnings with \"${ext.warn}\"";
            this.warnings += ext.warn;
        }
    }
}
