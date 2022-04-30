package com.james.libraryb;

import com.james.librarya.LibraryAClass;

/**
 * @author james
 * @date :2021/7/18 22:18
 */
public class LibraryB {

    /**
     * libraryB testHello
     */
    public static void testHello() {
        LibraryAClass.testHello();
        System.out.println("im libraryB");
    }
}
