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
import com.toasttab.expediter.test.Base;
import com.toasttab.expediter.test.Baz;
import com.toasttab.expediter.test.Ex;
import com.toasttab.expediter.test.Foo;
import com.toasttab.expediter.test.Lambda;
import com.toasttab.expediter.test.ParamParam;
import com.toasttab.expediter.test.Var;
import com.toasttab.expediter.test.WasClass;
import com.toasttab.expediter.test.WasInterface;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;

public final class Caller extends Base {
    Foo foo;
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

    void protectedField() {
        new Baz().bar(1f);
    }

    void fieldMovedFromSuper() {
        new Baz().i = 1;
    }

    void fieldAccessedViaPublicSubclass() { bar.i = 1; }

    void accessProtectedField() { w = 0; }

    void missingTypeLocalVar() {
        Var v = new Var();
    }

    void missingIndyLambda() {
        Lambda l = () -> { };
    }

    void missingTypeException() {
        try {
            new Object();
        } catch (Ex e) {

        }
    }

    void missingTypeMethodArg() {
        bar.arg(null);
    }

    boolean missingTypeInstanceof(Object o) {
        return o instanceof ParamParam[];
    }

    void superMethodMoved() {
        super.supersuper();
    }

    void interfaceToClass(WasInterface o) {
        o.foo();
    }

    void virtualToInterface(WasClass o) {
        o.foo();
    }
}
