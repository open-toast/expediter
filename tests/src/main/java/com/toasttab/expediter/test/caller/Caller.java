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

import com.toasttab.expediter.test.Bar;
import com.toasttab.expediter.test.BaseFoo;
import com.toasttab.expediter.test.Baz;
import com.toasttab.expediter.test.Foo;

public class Caller {
    Foo foo;
    BaseFoo baseFoo;
    Bar bar;

    void missingMethod() {
        bar.bar("");
    }

    void missingStatic() {
        bar.bar();
    }

    void missingSuper() {
        foo.base();
    }

    void missingType() {
        baseFoo.base();
    }

    void privateMethod() {
        bar.bar(1);
    }

    void packagePrivateMethod() {
        bar.bar(1L);
    }

    void missingField() {
        new Baz().a = "";
    }

    void staticField() {
        Baz.x = 1;
    }

    void instanceField() {
        new Baz().y = 1;
    }

    void privateField() {
        new Baz().z = 1;
    }

    void packagePrivateField() {
        bar.j = 1;
    }

    void fieldMovedFromSuper() {
        new Baz().i = 1;
    }

    void fieldAccessedViaPublicSubclass() { bar.i = 1; }
}
