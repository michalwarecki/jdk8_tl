/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * This file is available under and governed by the GNU General Public
 * License version 2 only, as published by the Free Software Foundation.
 * However, the following notice accompanied the original version of this
 * file:
 *
 * Copyright (c) 2008-2012, Stephen Colebourne & Michael Nascimento Santos
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  * Neither the name of JSR-310 nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package tck.java.time;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamConstants;
import java.lang.reflect.Field;
import java.time.DateTimeException;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.Queries;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQuery;
import java.time.zone.ZoneRulesException;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test ZoneId.
 */
@Test
public class TCKZoneId extends AbstractTCKTest {

    //-----------------------------------------------------------------------
    @Test
    public void test_serialization() throws Exception {
        assertSerializable(ZoneId.of("Europe/London"));
        assertSerializable(ZoneId.of("America/Chicago"));
    }

    @Test
    public void test_serialization_format() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream dos = new DataOutputStream(baos) ) {
            dos.writeByte(7);
            dos.writeUTF("Europe/London");
        }
        byte[] bytes = baos.toByteArray();
        assertSerializedBySer(ZoneId.of("Europe/London"), bytes);
    }

    @Test
    public void test_deserialization_lenient_characters() throws Exception {
        // an ID can be loaded without validation during deserialization
        String id = "QWERTYUIOPASDFGHJKLZXCVBNM~/._+-";
        ZoneId deser = deserialize(id);
        // getting the ID and string are OK
        assertEquals(deser.getId(), id);
        assertEquals(deser.toString(), id);
        // getting the rules is not
        try {
            deser.getRules();
            fail();
        } catch (ZoneRulesException ex) {
            // expected
        }
    }

    @Test(expectedExceptions=DateTimeException.class)
    public void test_deserialization_lenient_badCharacters() throws Exception {
        // an ID can be loaded without validation during deserialization
        // but there is a check to ensure the ID format is valid
        deserialize("|!?");
    }

    @Test(dataProvider="offsetBasedValid", expectedExceptions=DateTimeException.class)
    public void test_deserialization_lenient_offsetNotAllowed_noPrefix(String input, String resolvedId) throws Exception {
        // an ID can be loaded without validation during deserialization
        // but there is a check to ensure the ID format is valid
        deserialize(input);
    }

    @Test(dataProvider="offsetBasedValid", expectedExceptions=DateTimeException.class)
    public void test_deserialization_lenient_offsetNotAllowed_prefixUTC(String input, String resolvedId) throws Exception {
        // an ID can be loaded without validation during deserialization
        // but there is a check to ensure the ID format is valid
        deserialize("UTC" + input);
    }

    @Test(dataProvider="offsetBasedValid", expectedExceptions=DateTimeException.class)
    public void test_deserialization_lenient_offsetNotAllowed_prefixGMT(String input, String resolvedId) throws Exception {
        // an ID can be loaded without validation during deserialization
        // but there is a check to ensure the ID format is valid
        deserialize("GMT" + input);
    }

    @Test(dataProvider="offsetBasedValid", expectedExceptions=DateTimeException.class)
    public void test_deserialization_lenient_offsetNotAllowed_prefixUT(String input, String resolvedId) throws Exception {
        // an ID can be loaded without validation during deserialization
        // but there is a check to ensure the ID format is valid
        deserialize("UT" + input);
    }

    private ZoneId deserialize(String id) throws Exception {
        String serClass = ZoneId.class.getPackage().getName() + ".Ser";
        Class<?> serCls = Class.forName(serClass);
        Field field = serCls.getDeclaredField("serialVersionUID");
        field.setAccessible(true);
        long serVer = (Long) field.get(null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream dos = new DataOutputStream(baos)) {
            dos.writeShort(ObjectStreamConstants.STREAM_MAGIC);
            dos.writeShort(ObjectStreamConstants.STREAM_VERSION);
            dos.writeByte(ObjectStreamConstants.TC_OBJECT);
            dos.writeByte(ObjectStreamConstants.TC_CLASSDESC);
            dos.writeUTF(serClass);
            dos.writeLong(serVer);
            dos.writeByte(ObjectStreamConstants.SC_EXTERNALIZABLE | ObjectStreamConstants.SC_BLOCK_DATA);
            dos.writeShort(0);  // number of fields
            dos.writeByte(ObjectStreamConstants.TC_ENDBLOCKDATA);  // end of classdesc
            dos.writeByte(ObjectStreamConstants.TC_NULL);  // no superclasses
            dos.writeByte(ObjectStreamConstants.TC_BLOCKDATA);
            dos.writeByte(1 + 2 + id.length());  // length of data (1 byte + 2 bytes UTF length + 32 bytes UTF)
            dos.writeByte(7);  // ZoneId
            dos.writeUTF(id);
            dos.writeByte(ObjectStreamConstants.TC_ENDBLOCKDATA);  // end of blockdata
        }
        ZoneId deser = null;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            deser = (ZoneId) ois.readObject();
        }
        return deser;
    }

    //-----------------------------------------------------------------------
    // OLD_IDS_PRE_2005
    //-----------------------------------------------------------------------
    public void test_constant_OLD_IDS_PRE_2005() {
        Map<String, String> ids = ZoneId.OLD_IDS_PRE_2005;
        assertEquals(ids.get("EST"), "America/New_York");
        assertEquals(ids.get("MST"), "America/Denver");
        assertEquals(ids.get("HST"), "Pacific/Honolulu");
        assertEquals(ids.get("ACT"), "Australia/Darwin");
        assertEquals(ids.get("AET"), "Australia/Sydney");
        assertEquals(ids.get("AGT"), "America/Argentina/Buenos_Aires");
        assertEquals(ids.get("ART"), "Africa/Cairo");
        assertEquals(ids.get("AST"), "America/Anchorage");
        assertEquals(ids.get("BET"), "America/Sao_Paulo");
        assertEquals(ids.get("BST"), "Asia/Dhaka");
        assertEquals(ids.get("CAT"), "Africa/Harare");
        assertEquals(ids.get("CNT"), "America/St_Johns");
        assertEquals(ids.get("CST"), "America/Chicago");
        assertEquals(ids.get("CTT"), "Asia/Shanghai");
        assertEquals(ids.get("EAT"), "Africa/Addis_Ababa");
        assertEquals(ids.get("ECT"), "Europe/Paris");
        assertEquals(ids.get("IET"), "America/Indiana/Indianapolis");
        assertEquals(ids.get("IST"), "Asia/Kolkata");
        assertEquals(ids.get("JST"), "Asia/Tokyo");
        assertEquals(ids.get("MIT"), "Pacific/Apia");
        assertEquals(ids.get("NET"), "Asia/Yerevan");
        assertEquals(ids.get("NST"), "Pacific/Auckland");
        assertEquals(ids.get("PLT"), "Asia/Karachi");
        assertEquals(ids.get("PNT"), "America/Phoenix");
        assertEquals(ids.get("PRT"), "America/Puerto_Rico");
        assertEquals(ids.get("PST"), "America/Los_Angeles");
        assertEquals(ids.get("SST"), "Pacific/Guadalcanal");
        assertEquals(ids.get("VST"), "Asia/Ho_Chi_Minh");
    }

    @Test(expectedExceptions=UnsupportedOperationException.class)
    public void test_constant_OLD_IDS_PRE_2005_immutable() {
        Map<String, String> ids = ZoneId.OLD_IDS_PRE_2005;
        ids.clear();
    }

    //-----------------------------------------------------------------------
    // OLD_IDS_POST_2005
    //-----------------------------------------------------------------------
    public void test_constant_OLD_IDS_POST_2005() {
        Map<String, String> ids = ZoneId.OLD_IDS_POST_2005;
        assertEquals(ids.get("EST"), "-05:00");
        assertEquals(ids.get("MST"), "-07:00");
        assertEquals(ids.get("HST"), "-10:00");
        assertEquals(ids.get("ACT"), "Australia/Darwin");
        assertEquals(ids.get("AET"), "Australia/Sydney");
        assertEquals(ids.get("AGT"), "America/Argentina/Buenos_Aires");
        assertEquals(ids.get("ART"), "Africa/Cairo");
        assertEquals(ids.get("AST"), "America/Anchorage");
        assertEquals(ids.get("BET"), "America/Sao_Paulo");
        assertEquals(ids.get("BST"), "Asia/Dhaka");
        assertEquals(ids.get("CAT"), "Africa/Harare");
        assertEquals(ids.get("CNT"), "America/St_Johns");
        assertEquals(ids.get("CST"), "America/Chicago");
        assertEquals(ids.get("CTT"), "Asia/Shanghai");
        assertEquals(ids.get("EAT"), "Africa/Addis_Ababa");
        assertEquals(ids.get("ECT"), "Europe/Paris");
        assertEquals(ids.get("IET"), "America/Indiana/Indianapolis");
        assertEquals(ids.get("IST"), "Asia/Kolkata");
        assertEquals(ids.get("JST"), "Asia/Tokyo");
        assertEquals(ids.get("MIT"), "Pacific/Apia");
        assertEquals(ids.get("NET"), "Asia/Yerevan");
        assertEquals(ids.get("NST"), "Pacific/Auckland");
        assertEquals(ids.get("PLT"), "Asia/Karachi");
        assertEquals(ids.get("PNT"), "America/Phoenix");
        assertEquals(ids.get("PRT"), "America/Puerto_Rico");
        assertEquals(ids.get("PST"), "America/Los_Angeles");
        assertEquals(ids.get("SST"), "Pacific/Guadalcanal");
        assertEquals(ids.get("VST"), "Asia/Ho_Chi_Minh");
    }

    @Test(expectedExceptions=UnsupportedOperationException.class)
    public void test_constant_OLD_IDS_POST_2005_immutable() {
        Map<String, String> ids = ZoneId.OLD_IDS_POST_2005;
        ids.clear();
    }

    //-----------------------------------------------------------------------
    // mapped factory
    //-----------------------------------------------------------------------
    @Test
    public void test_of_string_Map() {
        Map<String, String> map = new HashMap<>();
        map.put("LONDON", "Europe/London");
        map.put("PARIS", "Europe/Paris");
        ZoneId test = ZoneId.of("LONDON", map);
        assertEquals(test.getId(), "Europe/London");
    }

    @Test
    public void test_of_string_Map_lookThrough() {
        Map<String, String> map = new HashMap<>();
        map.put("LONDON", "Europe/London");
        map.put("PARIS", "Europe/Paris");
        ZoneId test = ZoneId.of("Europe/Madrid", map);
        assertEquals(test.getId(), "Europe/Madrid");
    }

    @Test
    public void test_of_string_Map_emptyMap() {
        Map<String, String> map = new HashMap<>();
        ZoneId test = ZoneId.of("Europe/Madrid", map);
        assertEquals(test.getId(), "Europe/Madrid");
    }

    @Test(expectedExceptions=DateTimeException.class)
    public void test_of_string_Map_badFormat() {
        Map<String, String> map = new HashMap<>();
        ZoneId.of("Not known", map);
    }

    @Test(expectedExceptions=ZoneRulesException.class)
    public void test_of_string_Map_unknown() {
        Map<String, String> map = new HashMap<>();
        ZoneId.of("Unknown", map);
    }

    //-----------------------------------------------------------------------
    // regular factory
    //-----------------------------------------------------------------------
    @DataProvider(name="offsetBasedZero")
    Object[][] data_offsetBasedZero() {
        return new Object[][] {
                {""}, {"0"},
                {"+00"},{"+0000"},{"+00:00"},{"+000000"},{"+00:00:00"},
                {"-00"},{"-0000"},{"-00:00"},{"-000000"},{"-00:00:00"},
        };
    }

    @Test(dataProvider="offsetBasedZero")
    public void factory_of_String_offsetBasedZero_noPrefix(String id) {
        if (id.length() > 0 && id.equals("0") == false) {
            ZoneId test = ZoneId.of(id);
            assertEquals(test, ZoneOffset.UTC);
        }
    }

    @Test(dataProvider="offsetBasedZero")
    public void factory_of_String_offsetBasedZero_prefixUTC(String id) {
        ZoneId test = ZoneId.of("UTC" + id);
        assertEquals(test, ZoneOffset.UTC);
    }

    @Test(dataProvider="offsetBasedZero")
    public void factory_of_String_offsetBasedZero_prefixGMT(String id) {
        ZoneId test = ZoneId.of("GMT" + id);
        assertEquals(test, ZoneOffset.UTC);
    }

    @Test(dataProvider="offsetBasedZero")
    public void factory_of_String_offsetBasedZero_prefixUT(String id) {
        ZoneId test = ZoneId.of("UT" + id);
        assertEquals(test, ZoneOffset.UTC);
    }

    @Test
    public void factory_of_String_offsetBasedZero_z() {
        ZoneId test = ZoneId.of("Z");
        assertEquals(test, ZoneOffset.UTC);
    }

    //-----------------------------------------------------------------------
    @DataProvider(name="offsetBasedValid")
    Object[][] data_offsetBasedValid() {
        return new Object[][] {
                {"+0", "Z"},
                {"+5", "+05:00"},
                {"+01", "+01:00"},
                {"+0100", "+01:00"},{"+01:00", "+01:00"},
                {"+010000", "+01:00"},{"+01:00:00", "+01:00"},
                {"+12", "+12:00"},
                {"+1234", "+12:34"},{"+12:34", "+12:34"},
                {"+123456", "+12:34:56"},{"+12:34:56", "+12:34:56"},
                {"-02", "-02:00"},
                {"-5", "-05:00"},
                {"-0200", "-02:00"},{"-02:00", "-02:00"},
                {"-020000", "-02:00"},{"-02:00:00", "-02:00"},
        };
    }

    @Test(dataProvider="offsetBasedValid")
    public void factory_of_String_offsetBasedValid_noPrefix(String input, String id) {
        ZoneId test = ZoneId.of(input);
        assertEquals(test.getId(), id);
        assertEquals(test, ZoneOffset.of(id));
    }

    @Test(dataProvider="offsetBasedValid")
    public void factory_of_String_offsetBasedValid_prefixUTC(String input, String id) {
        ZoneId test = ZoneId.of("UTC" + input);
        assertEquals(test.getId(), id);
        assertEquals(test, ZoneOffset.of(id));
    }

    @Test(dataProvider="offsetBasedValid")
    public void factory_of_String_offsetBasedValid_prefixGMT(String input, String id) {
        ZoneId test = ZoneId.of("GMT" + input);
        assertEquals(test.getId(), id);
        assertEquals(test, ZoneOffset.of(id));
    }

    @Test(dataProvider="offsetBasedValid")
    public void factory_of_String_offsetBasedValid_prefixUT(String input, String id) {
        ZoneId test = ZoneId.of("UT" + input);
        assertEquals(test.getId(), id);
        assertEquals(test, ZoneOffset.of(id));
    }

    //-----------------------------------------------------------------------
    @DataProvider(name="offsetBasedInvalid")
    Object[][] data_offsetBasedInvalid() {
        return new Object[][] {
                {"A"}, {"B"}, {"C"}, {"D"}, {"E"}, {"F"}, {"G"}, {"H"}, {"I"}, {"J"}, {"K"}, {"L"}, {"M"},
                {"N"}, {"O"}, {"P"}, {"Q"}, {"R"}, {"S"}, {"T"}, {"U"}, {"V"}, {"W"}, {"X"}, {"Y"}, {"Z"},
                {"+0:00"}, {"+00:0"}, {"+0:0"},
                {"+000"}, {"+00000"},
                {"+0:00:00"}, {"+00:0:00"}, {"+00:00:0"}, {"+0:0:0"}, {"+0:0:00"}, {"+00:0:0"}, {"+0:00:0"},
                {"+01_00"}, {"+01;00"}, {"+01@00"}, {"+01:AA"},
                {"+19"}, {"+19:00"}, {"+18:01"}, {"+18:00:01"}, {"+1801"}, {"+180001"},
                {"-0:00"}, {"-00:0"}, {"-0:0"},
                {"-000"}, {"-00000"},
                {"-0:00:00"}, {"-00:0:00"}, {"-00:00:0"}, {"-0:0:0"}, {"-0:0:00"}, {"-00:0:0"}, {"-0:00:0"},
                {"-19"}, {"-19:00"}, {"-18:01"}, {"-18:00:01"}, {"-1801"}, {"-180001"},
                {"-01_00"}, {"-01;00"}, {"-01@00"}, {"-01:AA"},
                {"@01:00"},
        };
    }

    @Test(dataProvider="offsetBasedInvalid", expectedExceptions=DateTimeException.class)
    public void factory_of_String_offsetBasedInvalid_noPrefix(String id) {
        if (id.equals("Z")) {
            throw new DateTimeException("Fake exception: Z alone is valid, not invalid");
        }
        ZoneId.of(id);
    }

    @Test(dataProvider="offsetBasedInvalid", expectedExceptions=DateTimeException.class)
    public void factory_of_String_offsetBasedInvalid_prefixUTC(String id) {
        ZoneId.of("UTC" + id);
    }

    @Test(dataProvider="offsetBasedInvalid", expectedExceptions=DateTimeException.class)
    public void factory_of_String_offsetBasedInvalid_prefixGMT(String id) {
        ZoneId.of("GMT" + id);
    }

    @Test(dataProvider="offsetBasedInvalid", expectedExceptions=DateTimeException.class)
    public void factory_of_String_offsetBasedInvalid_prefixUT(String id) {
        if (id.equals("C")) {
            throw new DateTimeException("Fake exception: UT + C = UTC, thus it is valid, not invalid");
        }
        ZoneId.of("UT" + id);
    }

    //-----------------------------------------------------------------------
    @DataProvider(name="regionBasedInvalid")
    Object[][] data_regionBasedInvalid() {
        // \u00ef is a random unicode character
        return new Object[][] {
                {""}, {":"}, {"#"},
                {"\u00ef"}, {"`"}, {"!"}, {"\""}, {"\u00ef"}, {"$"}, {"^"}, {"&"}, {"*"}, {"("}, {")"}, {"="},
                {"\\"}, {"|"}, {","}, {"<"}, {">"}, {"?"}, {";"}, {"'"}, {"["}, {"]"}, {"{"}, {"}"},
                {"\u00ef:A"}, {"`:A"}, {"!:A"}, {"\":A"}, {"\u00ef:A"}, {"$:A"}, {"^:A"}, {"&:A"}, {"*:A"}, {"(:A"}, {"):A"}, {"=:A"}, {"+:A"},
                {"\\:A"}, {"|:A"}, {",:A"}, {"<:A"}, {">:A"}, {"?:A"}, {";:A"}, {"::A"}, {"':A"}, {"@:A"}, {"~:A"}, {"[:A"}, {"]:A"}, {"{:A"}, {"}:A"},
                {"A:B#\u00ef"}, {"A:B#`"}, {"A:B#!"}, {"A:B#\""}, {"A:B#\u00ef"}, {"A:B#$"}, {"A:B#^"}, {"A:B#&"}, {"A:B#*"},
                {"A:B#("}, {"A:B#)"}, {"A:B#="}, {"A:B#+"},
                {"A:B#\\"}, {"A:B#|"}, {"A:B#,"}, {"A:B#<"}, {"A:B#>"}, {"A:B#?"}, {"A:B#;"}, {"A:B#:"},
                {"A:B#'"}, {"A:B#@"}, {"A:B#~"}, {"A:B#["}, {"A:B#]"}, {"A:B#{"}, {"A:B#}"},
        };
    }

    @Test(dataProvider="regionBasedInvalid", expectedExceptions=DateTimeException.class)
    public void factory_of_String_regionBasedInvalid(String id) {
        ZoneId.of(id);
    }

    //-----------------------------------------------------------------------
    @Test
    public void factory_of_String_region_EuropeLondon() {
        ZoneId test = ZoneId.of("Europe/London");
        assertEquals(test.getId(), "Europe/London");
        assertEquals(test.getRules().isFixedOffset(), false);
    }

    //-----------------------------------------------------------------------
    @Test(expectedExceptions=NullPointerException.class)
    public void factory_of_String_null() {
        ZoneId.of(null);
    }

    @Test(expectedExceptions=DateTimeException.class)
    public void factory_of_String_badFormat() {
        ZoneId.of("Unknown rule");
    }

    @Test(expectedExceptions=ZoneRulesException.class)
    public void factory_of_String_unknown() {
        ZoneId.of("Unknown");
    }

    //-----------------------------------------------------------------------
    // from(TemporalAccessor)
    //-----------------------------------------------------------------------
    @Test
    public void factory_from_TemporalAccessor_zoneId() {
        TemporalAccessor mock = new TemporalAccessor() {
            @Override
            public boolean isSupported(TemporalField field) {
                return false;
            }
            @Override
            public long getLong(TemporalField field) {
                throw new DateTimeException("Mock");
            }
            @SuppressWarnings("unchecked")
            @Override
            public <R> R query(TemporalQuery<R> query) {
                if (query == Queries.zoneId()) {
                    return (R) ZoneId.of("Europe/Paris");
                }
                return TemporalAccessor.super.query(query);
            }
        };
        assertEquals(ZoneId.from(mock),  ZoneId.of("Europe/Paris"));
    }

    @Test
    public void factory_from_TemporalAccessor_offset() {
        ZoneOffset offset = ZoneOffset.ofHours(1);
        assertEquals(ZoneId.from(offset), offset);
    }

    @Test(expectedExceptions=DateTimeException.class)
    public void factory_from_TemporalAccessor_invalid_noDerive() {
        ZoneId.from(LocalTime.of(12, 30));
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void factory_from_TemporalAccessor_null() {
        ZoneId.from(null);
    }

    //-----------------------------------------------------------------------
    // equals() / hashCode()
    //-----------------------------------------------------------------------
    @Test
    public void test_equals() {
        ZoneId test1 = ZoneId.of("Europe/London");
        ZoneId test2 = ZoneId.of("Europe/Paris");
        ZoneId test2b = ZoneId.of("Europe/Paris");
        assertEquals(test1.equals(test2), false);
        assertEquals(test2.equals(test1), false);

        assertEquals(test1.equals(test1), true);
        assertEquals(test2.equals(test2), true);
        assertEquals(test2.equals(test2b), true);

        assertEquals(test1.hashCode() == test1.hashCode(), true);
        assertEquals(test2.hashCode() == test2.hashCode(), true);
        assertEquals(test2.hashCode() == test2b.hashCode(), true);
    }

    @Test
    public void test_equals_null() {
        assertEquals(ZoneId.of("Europe/London").equals(null), false);
    }

    @Test
    public void test_equals_notEqualWrongType() {
        assertEquals(ZoneId.of("Europe/London").equals("Europe/London"), false);
    }

    //-----------------------------------------------------------------------
    // toString()
    //-----------------------------------------------------------------------
    @DataProvider(name="toString")
    Object[][] data_toString() {
        return new Object[][] {
                {"Europe/London", "Europe/London"},
                {"Europe/Paris", "Europe/Paris"},
                {"Europe/Berlin", "Europe/Berlin"},
                {"UTC", "Z"},
                {"UTC+01:00", "+01:00"},
        };
    }

    @Test(dataProvider="toString")
    public void test_toString(String id, String expected) {
        ZoneId test = ZoneId.of(id);
        assertEquals(test.toString(), expected);
    }

}
