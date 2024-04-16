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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SortedSet;

public class CallerNegative extends LinkedHashMap {
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

    String toStringOnInterfaceIsOk(List<String> list) {
        return list.toString();
    }

    int inheritedInterfaceMethodIsOk(SortedSet<String> set) {
        return set.size();
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

    private enum NestedEnum {
        DOUBLE_NESTED {
            @Override
            void foo() {}
        };

        void foo() {}
    }

    void methodHandle() throws Throwable {
        MethodHandle h1 = MethodHandles.publicLookup().findVirtual(String.class, "substring", MethodType.methodType(String.class, int.class, int.class));

        h1.invokeExact("xxx", 1, 2);

        MethodHandle h2 = MethodHandles.publicLookup().findStatic(String.class, "valueOf", MethodType.methodType(String.class, long.class));

        h2.invokeExact(1L);
    }

    void varHandle() throws Throwable {
        VarHandle vh = MethodHandles.publicLookup().findVarHandle(int[].class, "length", int.class);

        vh.get(new int[0]);
    }

    void superSuperCallOk() {
        // `invokespecial LinkedHashMap.remove(Object)` should be ok even though `remove` is declared on `HashMap`
        super.remove(new Object());
    }
}
