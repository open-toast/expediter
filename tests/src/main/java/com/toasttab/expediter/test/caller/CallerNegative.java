/*
 * Copyright (c) 2023 Toast Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.toasttab.expediter.test.caller;

public class CallerNegative {
    private int x;

    void arrayCloneIsOk() {
        Object[] array = new Object[0];
        array.clone();
    }

    void subclassCloneIsOk() throws Exception {
        Clones.C xx = new Clones.C();

        xx.clone();
    }

    void arrayLengthIsOk() {
        int[] array = new int[0];
        int i = array.length;
    }

    void privateAccessToNestedIsOk() {
        new Nested().f = 1;
    }

    private class Nested {
        private int f;

        void privateAccessFromNestedIsOk() {
            x = 1;
        }
    }
}
