package com.toasttab.expediter.test.caller;

public class CloneableHierarchy {
    static class CloneableBase implements Cloneable {
        @Override
        public Object clone() throws CloneNotSupportedException {
            return super.clone();
        }
    }

    static class CloneableChild extends CloneableBase {

    }

    static class CloneableGrandchild extends CloneableChild implements MarkerInterface {

    }

    static interface MarkerInterface {

    }
}
