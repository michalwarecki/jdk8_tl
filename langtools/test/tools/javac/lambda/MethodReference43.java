/*
 * Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * @test
 * @bug 8003280
 * @summary Add lambda tests
 *  check that diamond inference is applied when using raw constructor reference qualifier
 * @run main MethodReference43
 */
public class MethodReference43 {

    static int assertionCount = 0;

    static void assertTrue(boolean cond) {
        assertionCount++;
        if (!cond)
            throw new AssertionError();
    }

    interface SAM1 {
       Foo<?> m(String s);
    }

    interface SAM2 {
       Foo<?> m(Integer s);
    }

    interface SAM3 {
       Foo<?> m(Object o);
    }

    interface SAM4 {
       Foo<Number> m(Integer o);
    }

    static class Foo<X extends Number> {
        Foo(X x) { }
    }


    static void m(SAM1 s) { assertTrue(false); }
    static void m(SAM2 s) { assertTrue(false); }
    static void m(SAM3 s) { assertTrue(false); }
    static void m(SAM4 s) { assertTrue(true); }

    public static void main(String[] args) {
        m(Foo::new);
        assertTrue(assertionCount == 1);
    }
}
