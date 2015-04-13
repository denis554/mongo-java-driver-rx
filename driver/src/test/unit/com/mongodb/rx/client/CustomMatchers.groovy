/*
 * Copyright 2015 MongoDB, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mongodb.rx.client

import org.hamcrest.BaseMatcher
import org.hamcrest.Description

@SuppressWarnings('NoDef')
class CustomMatchers {

    static isTheSameAs(final Object e) {
        [
                matches         : { a -> compare(e, a) },
                describeTo      : { Description description -> description.appendText("Operation has the same attributes ${e.class.name}")
                },
                describeMismatch: { a, description -> describer(e, a, description) }
        ] as BaseMatcher
    }

    static compare(expected, actual) {
        if (expected == actual) {
            return true
        }
        if (expected == null || actual == null) {
            return false
        }
        if (actual.class.name != expected.class.name) {
            return false
        }
        getFieldNames(actual.class).collect { it ->
            if (nominallyTheSame(it)) {
                return actual."$it".class == expected."$it".class
            } else if (actual."$it" != expected."$it") {
                def (a1, e1) = [actual."$it", expected."$it"]
                if (List.isCase(a1) && List.isCase(e1) && (a1.size() == e1.size())) {
                    def i = -1
                    return a1.collect { a -> i++; compare(a, e1[i]) }.every { it }
                } else if (a1 != null && a1.class.name.startsWith('com.mongodb') && a1.class == e1.class) {
                    return compare(a1, e1)
                }
                return false
            }
            true
        }.every { it }
    }

    static describer(expected, actual, description) {
        if (expected == actual) {
            return true
        }
        if (expected == null || actual == null) {
            description.appendText("different values: $expected != $actual, ")
            return false
        }
        if (actual.class.name != expected.class.name) {
            description.appendText("different classes: ${expected.class.name} != ${actual.class.name}, ")
            return false
        }

        getFieldNames(actual.class).collect { it ->
            if (nominallyTheSame(it)) {
                if (actual."$it".class != expected."$it".class) {
                    description.appendText("different classes $it :" +
                            " ${expected."$it".class.name} != ${actual."$it".class.name}, ")
                    return false
                }
            } else if (actual."$it" != expected."$it") {
                def (a1, e1) = [actual."$it", expected."$it"]
                if (List.isCase(a1) && List.isCase(e1) && (a1.size() == e1.size())) {
                    def i = -1
                    a1.each { a ->
                        i++; if (!compare(a, e1[i])) {
                            describer(a, e1[i], description)
                        }
                    }.every { it }
                } else if (a1 != null && a1.class.name.startsWith('com.mongodb') && a1.class == e1.class) {
                    return compare(a1, e1, description)
                }
                description.appendText("different values in $it : $e1 != $a1\n")
                return false
            }
            true
        }
    }

    static getFieldNames(Class curClass) {
        getFieldNames(curClass, [])
    }

    static getFieldNames(Class curClass, names) {
        if (curClass != Object) {
            getFieldNames(curClass.getSuperclass(), names += curClass.declaredFields.findAll { !it.synthetic }*.name)
        }
        names
    }

    static nominallyTheSame(String className ) {
        className in ['decoder', 'executor']
    }

}
