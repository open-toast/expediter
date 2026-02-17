package com.toasttab.expediter.test;

class MethodAccessChangeParent {
    // public field accessed via public subclass (even though this class is package-private), this is fine
    public int accessibleViaSubclass;

    // changes from public to package-private
    int publicToPackagePrivateField;
}
