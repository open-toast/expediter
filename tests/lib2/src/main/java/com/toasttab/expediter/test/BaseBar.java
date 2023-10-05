package com.toasttab.expediter.test;

class BaseBar {
    // public field accessed via public subclass (even though this class is package-private), this is fine
    public int i;

    // changes from public to package-private
    int j;
}
