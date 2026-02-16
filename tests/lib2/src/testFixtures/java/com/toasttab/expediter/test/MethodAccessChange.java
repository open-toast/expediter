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

package com.toasttab.expediter.test;

public class MethodAccessChange extends MethodAccessChangeParent {
    // changes from static to instance
    public void staticToInstance() { }

    // changes return type from void to String
    public String voidToNonVoidReturnType(String x) {
        return x;
    }

    // changes from public to private
    private void publicToPrivate(int x) { }

    // changes from public to package-private
    void publicToPackagePrivate(long x) { }

    // changes from public to protected
    protected void publicToProtected(float x) { }

    public void usesRemovedParamType(RemovedParamType[][] param) { }
}
