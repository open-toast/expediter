package com.toasttab.expediter.test.caller;

public class Clones {
    static class A implements Cloneable {
        @Override
        public Object clone() throws CloneNotSupportedException {
            return super.clone();
        }
    }

    static class B extends A {

    }

    static class C extends B implements D {

    }

    static interface D {

    }
}
