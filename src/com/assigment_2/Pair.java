package com.assigment_2;
/*
    * Copyright (c) 2010, 2013, Oracle and/or its affiliates. All rights reserved.
    * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
    *
    * This code is free software; you can redistribute it and/or modify it
    * under the terms of the GNU General Public License version 2 only, as
    * published by the Free Software Foundation.  Oracle designates this
    * particular file as subject to the "Classpath" exception as provided
    * by Oracle in the LICENSE file that accompanied this code.
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

  import java.io.Serializable;

  /**
   * &lt;p&gt;A convenience class to represent name-value pairs.&lt;/p&gt;
   * @since JavaFX 2.0
   */
 public class Pair<K, V> implements Serializable{

     /**
      * Key of this &lt;code&gt;Pair&lt;/code&gt;.
      */
    private K key;

    /**
     * Gets the key for this pair.
     * @return key for this pair
     */
    public K getKey() { return key; }

    /**
     * Value of this this &lt;code&gt;Pair&lt;/code&gt;.
     */
    private V value;

    /**
     * Gets the value for this pair.
     * @return value for this pair
     */
    public V getValue() { return value; }

    /**
     * Creates a new pair
     * @param key The key for this pair
     * @param value The value to use for this pair
     */
    public Pair( K key, V value) {
        this.key = key;
        this.value = value;
    }

    /**
     * &lt;p&gt;&lt;code&gt;String&lt;/code&gt; representation of this
     * &lt;code&gt;Pair&lt;/code&gt;.&lt;/p&gt;
     *
     * &lt;p&gt;The default name/value delimiter '=' is always used.&lt;/p&gt;
     *
     *  @return &lt;code&gt;String&lt;/code&gt; representation of this &lt;code&gt;Pair&lt;/code&gt;
     */
    @Override
    public String toString() {
        return key + "=" + value;
     }

     /**
      * &lt;p&gt;Generate a hash code for this &lt;code&gt;Pair&lt;/code&gt;.&lt;/p&gt;
      *
      * &lt;p&gt;The hash code is calculated using both the name and
      * the value of the &lt;code&gt;Pair&lt;/code&gt;.&lt;/p&gt;
      *
      * @return hash code for this &lt;code&gt;Pair&lt;/code&gt;
      */
     @Override
     public int hashCode() {
         int hash = 7;
         hash = 31 * hash + (key != null ? key.hashCode() : 0);
         hash = 31 * hash + (value != null ? value.hashCode() : 0);
         return hash;
     }

      /**
       * &lt;p&gt;Test this &lt;code&gt;Pair&lt;/code&gt; for equality with another
       * &lt;code&gt;Object&lt;/code&gt;.&lt;/p&gt;
       *
       * &lt;p&gt;If the &lt;code&gt;Object&lt;/code&gt; to be tested is not a
       * &lt;code&gt;Pair&lt;/code&gt; or is &lt;code&gt;null&lt;/code&gt;, then this method
       * returns &lt;code&gt;false&lt;/code&gt;.&lt;/p&gt;
       *
       * &lt;p&gt;Two &lt;code&gt;Pair&lt;/code&gt;s are considered equal if and only if
       * both the names and values are equal.&lt;/p&gt;
       *
       * @param o the &lt;code&gt;Object&lt;/code&gt; to test for
       * equality with this &lt;code&gt;Pair&lt;/code&gt;
       * @return &lt;code&gt;true&lt;/code&gt; if the given &lt;code&gt;Object&lt;/code&gt; is
       * equal to this &lt;code&gt;Pair&lt;/code&gt; else &lt;code&gt;false&lt;/code&gt;
       */
      @Override
      public boolean equals(Object o) {
          if (this == o) return true;
          if (o instanceof Pair) {
              Pair pair = (Pair) o;
              if (key != null ? !key.equals(pair.key) : pair.key != null) return false;
              if (value != null ? !value.equals(pair.value) : pair.value != null) return false;
              return true;
          }
          return false;
      }
  }
