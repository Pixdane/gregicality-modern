package com.pixdane.gregicality;

public interface Foo {

    default void bar() {
        throw new AssertionError();
    }
}
