/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
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

/*
 * This file is available under and governed by the GNU General Public
 * License version 2 only, as published by the Free Software Foundation.
 * However, the following notice accompanied the original version of this
 * file:
 *
 * Copyright (c) 2012, Stephen Colebourne & Michael Nascimento Santos
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
package java.time.chrono;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.chrono.HijrahChronology;
import java.time.chrono.JapaneseChronology;
import java.time.chrono.MinguoChronology;
import java.time.chrono.ThaiBuddhistChronology;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.Queries;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQuery;
import java.time.temporal.ValueRange;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A calendar system, used to organize and identify dates.
 * <p>
 * The main date and time API is built on the ISO calendar system.
 * This class operates behind the scenes to represent the general concept of a calendar system.
 * For example, the Japanese, Minguo, Thai Buddhist and others.
 * <p>
 * Most other calendar systems also operate on the shared concepts of year, month and day,
 * linked to the cycles of the Earth around the Sun, and the Moon around the Earth.
 * These shared concepts are defined by {@link ChronoField} and are available
 * for use by any {@code Chronology} implementation:
 * <pre>
 *   LocalDate isoDate = ...
 *   ChronoLocalDate&lt;ThaiBuddhistChronology&gt; thaiDate = ...
 *   int isoYear = isoDate.get(ChronoField.YEAR);
 *   int thaiYear = thaiDate.get(ChronoField.YEAR);
 * </pre>
 * As shown, although the date objects are in different calendar systems, represented by different
 * {@code Chronology} instances, both can be queried using the same constant on {@code ChronoField}.
 * For a full discussion of the implications of this, see {@link ChronoLocalDate}.
 * In general, the advice is to use the known ISO-based {@code LocalDate}, rather than
 * {@code ChronoLocalDate}.
 * <p>
 * While a {@code Chronology} object typically uses {@code ChronoField} and is based on
 * an era, year-of-era, month-of-year, day-of-month model of a date, this is not required.
 * A {@code Chronology} instance may represent a totally different kind of calendar system,
 * such as the Mayan.
 * <p>
 * In practical terms, the {@code Chronology} instance also acts as a factory.
 * The {@link #of(String)} method allows an instance to be looked up by identifier,
 * while the {@link #ofLocale(Locale)} method allows lookup by locale.
 * <p>
 * The {@code Chronology} instance provides a set of methods to create {@code ChronoLocalDate} instances.
 * The date classes are used to manipulate specific dates.
 * <p><ul>
 * <li> {@link #dateNow() dateNow()}
 * <li> {@link #dateNow(Clock) dateNow(clock)}
 * <li> {@link #dateNow(ZoneId) dateNow(zone)}
 * <li> {@link #date(int, int, int) date(yearProleptic, month, day)}
 * <li> {@link #date(Era, int, int, int) date(era, yearOfEra, month, day)}
 * <li> {@link #dateYearDay(int, int) dateYearDay(yearProleptic, dayOfYear)}
 * <li> {@link #dateYearDay(Era, int, int) dateYearDay(era, yearOfEra, dayOfYear)}
 * <li> {@link #date(TemporalAccessor) date(TemporalAccessor)}
 * </ul><p>
 *
 * <h3 id="addcalendars">Adding New Calendars</h3>
 * The set of available chronologies can be extended by applications.
 * Adding a new calendar system requires the writing of an implementation of
 * {@code Chronology}, {@code ChronoLocalDate} and {@code Era}.
 * The majority of the logic specific to the calendar system will be in
 * {@code ChronoLocalDate}. The {@code Chronology} subclass acts as a factory.
 * <p>
 * To permit the discovery of additional chronologies, the {@link java.util.ServiceLoader ServiceLoader}
 * is used. A file must be added to the {@code META-INF/services} directory with the
 * name 'java.time.chrono.Chronology' listing the implementation classes.
 * See the ServiceLoader for more details on service loading.
 * For lookup by id or calendarType, the system provided calendars are found
 * first followed by application provided calendars.
 * <p>
 * Each chronology must define a chronology ID that is unique within the system.
 * If the chronology represents a calendar system defined by the
 * <em>Unicode Locale Data Markup Language (LDML)</em> specification then that
 * calendar type should also be specified.
 *
 * <h3>Specification for implementors</h3>
 * This class must be implemented with care to ensure other classes operate correctly.
 * All implementations that can be instantiated must be final, immutable and thread-safe.
 * Subclasses should be Serializable wherever possible.
 *
 * @since 1.8
 */
public abstract class Chronology implements Comparable<Chronology> {

    /**
     * Map of available calendars by ID.
     */
    private static final ConcurrentHashMap<String, Chronology> CHRONOS_BY_ID = new ConcurrentHashMap<>();
    /**
     * Map of available calendars by calendar type.
     */
    private static final ConcurrentHashMap<String, Chronology> CHRONOS_BY_TYPE = new ConcurrentHashMap<>();

    /**
     * Register a Chronology by ID and type for lookup by {@link #of(java.lang.String)}.
     * Chronos must not be registered until they are completely constructed.
     * Specifically, not in the constructor of Chronology.
     * @param chrono the chronology to register; not null
     */
    private static void registerChrono(Chronology chrono) {
        Chronology prev = CHRONOS_BY_ID.putIfAbsent(chrono.getId(), chrono);
        if (prev == null) {
            String type = chrono.getCalendarType();
            if (type != null) {
                CHRONOS_BY_TYPE.putIfAbsent(type, chrono);
            }
        }
    }

    /**
     * Initialization of the maps from id and type to Chronology.
     * The ServiceLoader is used to find and register any implementations
     * of {@link java.time.chrono.Chronology} found in the bootclass loader.
     * The built-in chronologies are registered explicitly.
     * Calendars configured via the Thread's context classloader are local
     * to that thread and are ignored.
     * <p>
     * The initialization is done only once using the registration
     * of the IsoChronology as the test and the final step.
     * Multiple threads may perform the initialization concurrently.
     * Only the first registration of each Chronology is retained by the
     * ConcurrentHashMap.
     * @return true if the cache was initialized
     */
    private static boolean initCache() {
        if (CHRONOS_BY_ID.get("ISO") == null) {
            // Initialization is incomplete
            @SuppressWarnings("rawtypes")
            ServiceLoader<Chronology> loader =  ServiceLoader.load(Chronology.class, null);
            for (Chronology chrono : loader) {
                registerChrono(chrono);
            }

            // Register these calendars; the ServiceLoader configuration is not used
            registerChrono(HijrahChronology.INSTANCE);
            registerChrono(JapaneseChronology.INSTANCE);
            registerChrono(MinguoChronology.INSTANCE);
            registerChrono(ThaiBuddhistChronology.INSTANCE);

            // finally, register IsoChronology to mark initialization is complete
            registerChrono(IsoChronology.INSTANCE);
            return true;
        }
        return false;
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of {@code Chronology} from a temporal object.
     * <p>
     * This obtains a chronology based on the specified temporal.
     * A {@code TemporalAccessor} represents an arbitrary set of date and time information,
     * which this factory converts to an instance of {@code Chronology}.
     * <p>
     * The conversion will obtain the chronology using {@link Queries#chronology()}.
     * If the specified temporal object does not have a chronology, {@link IsoChronology} is returned.
     * <p>
     * This method matches the signature of the functional interface {@link TemporalQuery}
     * allowing it to be used in queries via method reference, {@code Chronology::from}.
     *
     * @param temporal  the temporal to convert, not null
     * @return the chronology, not null
     * @throws DateTimeException if unable to convert to an {@code Chronology}
     */
    public static Chronology from(TemporalAccessor temporal) {
        Objects.requireNonNull(temporal, "temporal");
        Chronology obj = temporal.query(Queries.chronology());
        return (obj != null ? obj : IsoChronology.INSTANCE);
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of {@code Chronology} from a locale.
     * <p>
     * This returns a {@code Chronology} based on the specified locale,
     * typically returning {@code IsoChronology}. Other calendar systems
     * are only returned if they are explicitly selected within the locale.
     * <p>
     * The {@link Locale} class provide access to a range of information useful
     * for localizing an application. This includes the language and region,
     * such as "en-GB" for English as used in Great Britain.
     * <p>
     * The {@code Locale} class also supports an extension mechanism that
     * can be used to identify a calendar system. The mechanism is a form
     * of key-value pairs, where the calendar system has the key "ca".
     * For example, the locale "en-JP-u-ca-japanese" represents the English
     * language as used in Japan with the Japanese calendar system.
     * <p>
     * This method finds the desired calendar system by in a manner equivalent
     * to passing "ca" to {@link Locale#getUnicodeLocaleType(String)}.
     * If the "ca" key is not present, then {@code IsoChronology} is returned.
     * <p>
     * Note that the behavior of this method differs from the older
     * {@link java.util.Calendar#getInstance(Locale)} method.
     * If that method receives a locale of "th_TH" it will return {@code BuddhistCalendar}.
     * By contrast, this method will return {@code IsoChronology}.
     * Passing the locale "th-TH-u-ca-buddhist" into either method will
     * result in the Thai Buddhist calendar system and is therefore the
     * recommended approach going forward for Thai calendar system localization.
     * <p>
     * A similar, but simpler, situation occurs for the Japanese calendar system.
     * The locale "jp_JP_JP" has previously been used to access the calendar.
     * However, unlike the Thai locale, "ja_JP_JP" is automatically converted by
     * {@code Locale} to the modern and recommended form of "ja-JP-u-ca-japanese".
     * Thus, there is no difference in behavior between this method and
     * {@code Calendar#getInstance(Locale)}.
     *
     * @param locale  the locale to use to obtain the calendar system, not null
     * @return the calendar system associated with the locale, not null
     * @throws DateTimeException if the locale-specified calendar cannot be found
     */
    public static Chronology ofLocale(Locale locale) {
        Objects.requireNonNull(locale, "locale");
        String type = locale.getUnicodeLocaleType("ca");
        if (type == null || "iso".equals(type) || "iso8601".equals(type)) {
            return IsoChronology.INSTANCE;
        }
        // Not pre-defined; lookup by the type
        do {
            Chronology chrono = CHRONOS_BY_TYPE.get(type);
            if (chrono != null) {
                return chrono;
            }
            // If not found, do the initialization (once) and repeat the lookup
        } while (initCache());
        throw new DateTimeException("Unknown calendar system: " + type);
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of {@code Chronology} from a chronology ID or
     * calendar system type.
     * <p>
     * This returns a chronology based on either the ID or the type.
     * The {@link #getId() chronology ID} uniquely identifies the chronology.
     * The {@link #getCalendarType() calendar system type} is defined by the LDML specification.
     * <p>
     * The chronology may be a system chronology or a chronology
     * provided by the application via ServiceLoader configuration.
     * <p>
     * Since some calendars can be customized, the ID or type typically refers
     * to the default customization. For example, the Gregorian calendar can have multiple
     * cutover dates from the Julian, but the lookup only provides the default cutover date.
     *
     * @param id  the chronology ID or calendar system type, not null
     * @return the chronology with the identifier requested, not null
     * @throws DateTimeException if the chronology cannot be found
     */
    public static Chronology of(String id) {
        Objects.requireNonNull(id, "id");
        do {
            Chronology chrono = of0(id);
            if (chrono != null) {
                return chrono;
            }
            // If not found, do the initialization (once) and repeat the lookup
        } while (initCache());

        // Look for a Chronology using ServiceLoader of the Thread's ContextClassLoader
        // Application provided Chronologies must not be cached
        @SuppressWarnings("rawtypes")
        ServiceLoader<Chronology> loader = ServiceLoader.load(Chronology.class);
        for (Chronology chrono : loader) {
            if (id.equals(chrono.getId()) || id.equals(chrono.getCalendarType())) {
                return chrono;
            }
        }
        throw new DateTimeException("Unknown chronology: " + id);
    }

    /**
     * Obtains an instance of {@code Chronology} from a chronology ID or
     * calendar system type.
     *
     * @param id  the chronology ID or calendar system type, not null
     * @return the chronology with the identifier requested, or {@code null} if not found
     */
    private static Chronology of0(String id) {
        Chronology chrono = CHRONOS_BY_ID.get(id);
        if (chrono == null) {
            chrono = CHRONOS_BY_TYPE.get(id);
        }
        return chrono;
    }

    /**
     * Returns the available chronologies.
     * <p>
     * Each returned {@code Chronology} is available for use in the system.
     * The set of chronologies includes the system chronologies and
     * any chronologies provided by the application via ServiceLoader
     * configuration.
     *
     * @return the independent, modifiable set of the available chronology IDs, not null
     */
    public static Set<Chronology> getAvailableChronologies() {
        initCache();       // force initialization
        HashSet<Chronology> chronos = new HashSet(CHRONOS_BY_ID.values());

        /// Add in Chronologies from the ServiceLoader configuration
        @SuppressWarnings("rawtypes")
        ServiceLoader<Chronology> loader = ServiceLoader.load(Chronology.class);
        for (Chronology chrono : loader) {
            chronos.add(chrono);
        }
        return chronos;
    }

    //-----------------------------------------------------------------------
    /**
     * Creates an instance.
     */
    protected Chronology() {
    }

    //-----------------------------------------------------------------------
    /**
     * Casts the {@code Temporal} to {@code ChronoLocalDate} with the same chronology.
     *
     * @param temporal  a date-time to cast, not null
     * @return the date-time checked and cast to {@code ChronoLocalDate}, not null
     * @throws ClassCastException if the date-time cannot be cast to ChronoLocalDate
     *  or the chronology is not equal this Chronology
     */
    ChronoLocalDate ensureChronoLocalDate(Temporal temporal) {
        @SuppressWarnings("unchecked")
        ChronoLocalDate other = (ChronoLocalDate) temporal;
        if (this.equals(other.getChronology()) == false) {
            throw new ClassCastException("Chronology mismatch, expected: " + getId() + ", actual: " + other.getChronology().getId());
        }
        return other;
    }

    /**
     * Casts the {@code Temporal} to {@code ChronoLocalDateTime} with the same chronology.
     *
     * @param temporal   a date-time to cast, not null
     * @return the date-time checked and cast to {@code ChronoLocalDateTime}, not null
     * @throws ClassCastException if the date-time cannot be cast to ChronoLocalDateTimeImpl
     *  or the chronology is not equal this Chronology
     */
    ChronoLocalDateTimeImpl<?> ensureChronoLocalDateTime(Temporal temporal) {
        @SuppressWarnings("unchecked")
        ChronoLocalDateTimeImpl<?> other = (ChronoLocalDateTimeImpl<?>) temporal;
        if (this.equals(other.toLocalDate().getChronology()) == false) {
            throw new ClassCastException("Chronology mismatch, required: " + getId()
                    + ", supplied: " + other.toLocalDate().getChronology().getId());
        }
        return other;
    }

    /**
     * Casts the {@code Temporal} to {@code ChronoZonedDateTimeImpl} with the same chronology.
     *
     * @param temporal  a date-time to cast, not null
     * @return the date-time checked and cast to {@code ChronoZonedDateTimeImpl}, not null
     * @throws ClassCastException if the date-time cannot be cast to ChronoZonedDateTimeImpl
     *  or the chronology is not equal this Chronology
     */
    ChronoZonedDateTimeImpl<?> ensureChronoZonedDateTime(Temporal temporal) {
        @SuppressWarnings("unchecked")
        ChronoZonedDateTimeImpl<?> other = (ChronoZonedDateTimeImpl<?>) temporal;
        if (this.equals(other.toLocalDate().getChronology()) == false) {
            throw new ClassCastException("Chronology mismatch, required: " + getId()
                    + ", supplied: " + other.toLocalDate().getChronology().getId());
        }
        return other;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the ID of the chronology.
     * <p>
     * The ID uniquely identifies the {@code Chronology}.
     * It can be used to lookup the {@code Chronology} using {@link #of(String)}.
     *
     * @return the chronology ID, not null
     * @see #getCalendarType()
     */
    public abstract String getId();

    /**
     * Gets the calendar type of the underlying calendar system.
     * <p>
     * The calendar type is an identifier defined by the
     * <em>Unicode Locale Data Markup Language (LDML)</em> specification.
     * It can be used to lookup the {@code Chronology} using {@link #of(String)}.
     * It can also be used as part of a locale, accessible via
     * {@link Locale#getUnicodeLocaleType(String)} with the key 'ca'.
     *
     * @return the calendar system type, null if the calendar is not defined by LDML
     * @see #getId()
     */
    public abstract String getCalendarType();

    //-----------------------------------------------------------------------
    /**
     * Obtains a local date in this chronology from the era, year-of-era,
     * month-of-year and day-of-month fields.
     *
     * @param era  the era of the correct type for the chronology, not null
     * @param yearOfEra  the chronology year-of-era
     * @param month  the chronology month-of-year
     * @param dayOfMonth  the chronology day-of-month
     * @return the local date in this chronology, not null
     * @throws DateTimeException if unable to create the date
     */
    public ChronoLocalDate date(Era era, int yearOfEra, int month, int dayOfMonth) {
        return date(prolepticYear(era, yearOfEra), month, dayOfMonth);
    }

    /**
     * Obtains a local date in this chronology from the proleptic-year,
     * month-of-year and day-of-month fields.
     *
     * @param prolepticYear  the chronology proleptic-year
     * @param month  the chronology month-of-year
     * @param dayOfMonth  the chronology day-of-month
     * @return the local date in this chronology, not null
     * @throws DateTimeException if unable to create the date
     */
    public abstract ChronoLocalDate date(int prolepticYear, int month, int dayOfMonth);

    /**
     * Obtains a local date in this chronology from the era, year-of-era and
     * day-of-year fields.
     *
     * @param era  the era of the correct type for the chronology, not null
     * @param yearOfEra  the chronology year-of-era
     * @param dayOfYear  the chronology day-of-year
     * @return the local date in this chronology, not null
     * @throws DateTimeException if unable to create the date
     */
    public ChronoLocalDate dateYearDay(Era era, int yearOfEra, int dayOfYear) {
        return dateYearDay(prolepticYear(era, yearOfEra), dayOfYear);
    }

    /**
     * Obtains a local date in this chronology from the proleptic-year and
     * day-of-year fields.
     *
     * @param prolepticYear  the chronology proleptic-year
     * @param dayOfYear  the chronology day-of-year
     * @return the local date in this chronology, not null
     * @throws DateTimeException if unable to create the date
     */
    public abstract ChronoLocalDate dateYearDay(int prolepticYear, int dayOfYear);

    //-----------------------------------------------------------------------
    /**
     * Obtains the current local date in this chronology from the system clock in the default time-zone.
     * <p>
     * This will query the {@link Clock#systemDefaultZone() system clock} in the default
     * time-zone to obtain the current date.
     * <p>
     * Using this method will prevent the ability to use an alternate clock for testing
     * because the clock is hard-coded.
     * <p>
     * This implementation uses {@link #dateNow(Clock)}.
     *
     * @return the current local date using the system clock and default time-zone, not null
     * @throws DateTimeException if unable to create the date
     */
    public ChronoLocalDate dateNow() {
        return dateNow(Clock.systemDefaultZone());
    }

    /**
     * Obtains the current local date in this chronology from the system clock in the specified time-zone.
     * <p>
     * This will query the {@link Clock#system(ZoneId) system clock} to obtain the current date.
     * Specifying the time-zone avoids dependence on the default time-zone.
     * <p>
     * Using this method will prevent the ability to use an alternate clock for testing
     * because the clock is hard-coded.
     *
     * @param zone  the zone ID to use, not null
     * @return the current local date using the system clock, not null
     * @throws DateTimeException if unable to create the date
     */
    public ChronoLocalDate dateNow(ZoneId zone) {
        return dateNow(Clock.system(zone));
    }

    /**
     * Obtains the current local date in this chronology from the specified clock.
     * <p>
     * This will query the specified clock to obtain the current date - today.
     * Using this method allows the use of an alternate clock for testing.
     * The alternate clock may be introduced using {@link Clock dependency injection}.
     *
     * @param clock  the clock to use, not null
     * @return the current local date, not null
     * @throws DateTimeException if unable to create the date
     */
    public ChronoLocalDate dateNow(Clock clock) {
        Objects.requireNonNull(clock, "clock");
        return date(LocalDate.now(clock));
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains a local date in this chronology from another temporal object.
     * <p>
     * This creates a date in this chronology based on the specified temporal.
     * A {@code TemporalAccessor} represents an arbitrary set of date and time information,
     * which this factory converts to an instance of {@code ChronoLocalDate}.
     * <p>
     * The conversion typically uses the {@link ChronoField#EPOCH_DAY EPOCH_DAY}
     * field, which is standardized across calendar systems.
     * <p>
     * This method matches the signature of the functional interface {@link TemporalQuery}
     * allowing it to be used as a query via method reference, {@code aChronology::date}.
     *
     * @param temporal  the temporal object to convert, not null
     * @return the local date in this chronology, not null
     * @throws DateTimeException if unable to create the date
     */
    public abstract ChronoLocalDate date(TemporalAccessor temporal);

    /**
     * Obtains a local date-time in this chronology from another temporal object.
     * <p>
     * This creates a date-time in this chronology based on the specified temporal.
     * A {@code TemporalAccessor} represents an arbitrary set of date and time information,
     * which this factory converts to an instance of {@code ChronoLocalDateTime}.
     * <p>
     * The conversion extracts and combines the {@code ChronoLocalDate} and the
     * {@code LocalTime} from the temporal object.
     * Implementations are permitted to perform optimizations such as accessing
     * those fields that are equivalent to the relevant objects.
     * The result uses this chronology.
     * <p>
     * This method matches the signature of the functional interface {@link TemporalQuery}
     * allowing it to be used as a query via method reference, {@code aChronology::localDateTime}.
     *
     * @param temporal  the temporal object to convert, not null
     * @return the local date-time in this chronology, not null
     * @throws DateTimeException if unable to create the date-time
     */
    public ChronoLocalDateTime<?> localDateTime(TemporalAccessor temporal) {
        try {
            return date(temporal).atTime(LocalTime.from(temporal));
        } catch (DateTimeException ex) {
            throw new DateTimeException("Unable to obtain ChronoLocalDateTime from TemporalAccessor: " + temporal.getClass(), ex);
        }
    }

    /**
     * Obtains a {@code ChronoZonedDateTime} in this chronology from another temporal object.
     * <p>
     * This creates a zoned date-time in this chronology based on the specified temporal.
     * A {@code TemporalAccessor} represents an arbitrary set of date and time information,
     * which this factory converts to an instance of {@code ChronoZonedDateTime}.
     * <p>
     * The conversion will first obtain a {@code ZoneId} from the temporal object,
     * falling back to a {@code ZoneOffset} if necessary. It will then try to obtain
     * an {@code Instant}, falling back to a {@code ChronoLocalDateTime} if necessary.
     * The result will be either the combination of {@code ZoneId} or {@code ZoneOffset}
     * with {@code Instant} or {@code ChronoLocalDateTime}.
     * Implementations are permitted to perform optimizations such as accessing
     * those fields that are equivalent to the relevant objects.
     * The result uses this chronology.
     * <p>
     * This method matches the signature of the functional interface {@link TemporalQuery}
     * allowing it to be used as a query via method reference, {@code aChronology::zonedDateTime}.
     *
     * @param temporal  the temporal object to convert, not null
     * @return the zoned date-time in this chronology, not null
     * @throws DateTimeException if unable to create the date-time
     */
    public ChronoZonedDateTime<?> zonedDateTime(TemporalAccessor temporal) {
        try {
            ZoneId zone = ZoneId.from(temporal);
            try {
                Instant instant = Instant.from(temporal);
                return zonedDateTime(instant, zone);

            } catch (DateTimeException ex1) {
                ChronoLocalDateTimeImpl cldt = ensureChronoLocalDateTime(localDateTime(temporal));
                return ChronoZonedDateTimeImpl.ofBest(cldt, zone, null);
            }
        } catch (DateTimeException ex) {
            throw new DateTimeException("Unable to obtain ChronoZonedDateTime from TemporalAccessor: " + temporal.getClass(), ex);
        }
    }

    /**
     * Obtains a {@code ChronoZonedDateTime} in this chronology from an {@code Instant}.
     * <p>
     * This creates a zoned date-time with the same instant as that specified.
     *
     * @param instant  the instant to create the date-time from, not null
     * @param zone  the time-zone, not null
     * @return the zoned date-time, not null
     * @throws DateTimeException if the result exceeds the supported range
     */
    public ChronoZonedDateTime<?> zonedDateTime(Instant instant, ZoneId zone) {
        return ChronoZonedDateTimeImpl.ofInstant(this, instant, zone);
    }

    //-----------------------------------------------------------------------
    /**
     * Checks if the specified year is a leap year.
     * <p>
     * A leap-year is a year of a longer length than normal.
     * The exact meaning is determined by the chronology according to the following constraints.
     * <p><ul>
     * <li>a leap-year must imply a year-length longer than a non leap-year.
     * <li>a chronology that does not support the concept of a year must return false.
     * </ul><p>
     *
     * @param prolepticYear  the proleptic-year to check, not validated for range
     * @return true if the year is a leap year
     */
    public abstract boolean isLeapYear(long prolepticYear);

    /**
     * Calculates the proleptic-year given the era and year-of-era.
     * <p>
     * This combines the era and year-of-era into the single proleptic-year field.
     *
     * @param era  the era of the correct type for the chronology, not null
     * @param yearOfEra  the chronology year-of-era
     * @return the proleptic-year
     * @throws DateTimeException if unable to convert
     */
    public abstract int prolepticYear(Era era, int yearOfEra);

    /**
     * Creates the chronology era object from the numeric value.
     * <p>
     * The era is, conceptually, the largest division of the time-line.
     * Most calendar systems have a single epoch dividing the time-line into two eras.
     * However, some have multiple eras, such as one for the reign of each leader.
     * The exact meaning is determined by the chronology according to the following constraints.
     * <p>
     * The era in use at 1970-01-01 must have the value 1.
     * Later eras must have sequentially higher values.
     * Earlier eras must have sequentially lower values.
     * Each chronology must refer to an enum or similar singleton to provide the era values.
     * <p>
     * This method returns the singleton era of the correct type for the specified era value.
     *
     * @param eraValue  the era value
     * @return the calendar system era, not null
     * @throws DateTimeException if unable to create the era
     */
    public abstract Era eraOf(int eraValue);

    /**
     * Gets the list of eras for the chronology.
     * <p>
     * Most calendar systems have an era, within which the year has meaning.
     * If the calendar system does not support the concept of eras, an empty
     * list must be returned.
     *
     * @return the list of eras for the chronology, may be immutable, not null
     */
    public abstract List<Era> eras();

    //-----------------------------------------------------------------------
    /**
     * Gets the range of valid values for the specified field.
     * <p>
     * All fields can be expressed as a {@code long} integer.
     * This method returns an object that describes the valid range for that value.
     * <p>
     * Note that the result only describes the minimum and maximum valid values
     * and it is important not to read too much into them. For example, there
     * could be values within the range that are invalid for the field.
     * <p>
     * This method will return a result whether or not the chronology supports the field.
     *
     * @param field  the field to get the range for, not null
     * @return the range of valid values for the field, not null
     * @throws DateTimeException if the range for the field cannot be obtained
     */
    public abstract ValueRange range(ChronoField field);

    //-----------------------------------------------------------------------
    /**
     * Gets the textual representation of this chronology.
     * <p>
     * This returns the textual name used to identify the chronology,
     * suitable for presentation to the user.
     * The parameters control the style of the returned text and the locale.
     *
     * @param style  the style of the text required, not null
     * @param locale  the locale to use, not null
     * @return the text value of the chronology, not null
     */
    public String getDisplayName(TextStyle style, Locale locale) {
        return new DateTimeFormatterBuilder().appendChronologyText(style).toFormatter(locale).format(new TemporalAccessor() {
            @Override
            public boolean isSupported(TemporalField field) {
                return false;
            }
            @Override
            public long getLong(TemporalField field) {
                throw new DateTimeException("Unsupported field: " + field);
            }
            @SuppressWarnings("unchecked")
            @Override
            public <R> R query(TemporalQuery<R> query) {
                if (query == Queries.chronology()) {
                    return (R) Chronology.this;
                }
                return TemporalAccessor.super.query(query);
            }
        });
    }

    //-----------------------------------------------------------------------
    /**
     * Compares this chronology to another chronology.
     * <p>
     * The comparison order first by the chronology ID string, then by any
     * additional information specific to the subclass.
     * It is "consistent with equals", as defined by {@link Comparable}.
     * <p>
     * The default implementation compares the chronology ID.
     * Subclasses must compare any additional state that they store.
     *
     * @param other  the other chronology to compare to, not null
     * @return the comparator value, negative if less, positive if greater
     */
    @Override
    public int compareTo(Chronology other) {
        return getId().compareTo(other.getId());
    }

    /**
     * Checks if this chronology is equal to another chronology.
     * <p>
     * The comparison is based on the entire state of the object.
     * <p>
     * The default implementation checks the type and calls {@link #compareTo(Chronology)}.
     *
     * @param obj  the object to check, null returns false
     * @return true if this is equal to the other chronology
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
           return true;
        }
        if (obj instanceof Chronology) {
            return compareTo((Chronology) obj) == 0;
        }
        return false;
    }

    /**
     * A hash code for this chronology.
     * <p>
     * The default implementation is based on the ID and class.
     * Subclasses should add any additional state that they store.
     *
     * @return a suitable hash code
     */
    @Override
    public int hashCode() {
        return getClass().hashCode() ^ getId().hashCode();
    }

    //-----------------------------------------------------------------------
    /**
     * Outputs this chronology as a {@code String}, using the ID.
     *
     * @return a string representation of this chronology, not null
     */
    @Override
    public String toString() {
        return getId();
    }

    //-----------------------------------------------------------------------
    /**
     * Writes the object using a
     * <a href="../../../serialized-form.html#java.time.temporal.Ser">dedicated serialized form</a>.
     * <pre>
     *  out.writeByte(7);  // identifies this as a Chronology
     * out.writeUTF(chronoId);
     * </pre>
     *
     * @return the instance of {@code Ser}, not null
     */
    private Object writeReplace() {
        return new Ser(Ser.CHRONO_TYPE, this);
    }

    /**
     * Defend against malicious streams.
     * @return never
     * @throws InvalidObjectException always
     */
    private Object readResolve() throws ObjectStreamException {
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    void writeExternal(DataOutput out) throws IOException {
        out.writeUTF(getId());
    }

    static Chronology readExternal(DataInput in) throws IOException {
        String id = in.readUTF();
        return Chronology.of(id);
    }

}
