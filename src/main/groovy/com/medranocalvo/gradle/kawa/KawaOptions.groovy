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
 * Configure the KawaCompile task.
 */
package com.medranocalvo.gradle.kawa

class KawaOptions {
    private String language = null;
    String getLanguage() {
        this.language
    }
    void setLanguage(String l) {
        this.language = l
    }
    void language(String l) {
        setLanguage l;
    }

    private Boolean inline = null;
    Boolean getInline() {
        this.inline
    }
    void setInline(Boolean i) {
        this.inline = i;
    }
    void inline(Boolean i) {
        setInline i;
    }

    private List<String> warn = [];
    List<String> getWarn() {
        this.warn
    }
    void setWarn(List<String> w) {
        this.warn = w;
    }
    void warn(String... w) {
        this.warn.addAll(w)
    }
}
