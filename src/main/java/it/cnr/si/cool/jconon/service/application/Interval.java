/*
 * Copyright (C) 2019  Consiglio Nazionale delle Ricerche
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package it.cnr.si.cool.jconon.service.application;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Optional;
import java.util.TimeZone;

public class Interval implements Comparable<Interval> {
    private static TimeZone CURRENT_TIME_ZONE = TimeZone.getTimeZone(ZoneId.of("Europe/Rome"));
    private Instant startDate;
    private Instant endDate;

    public Interval(Instant startDate, Instant endDate) {
        super();
        this.startDate = startDate;
        final Instant now = Calendar.getInstance(CURRENT_TIME_ZONE).toInstant();
        this.endDate = Optional.ofNullable(endDate).map(data -> {
            if (data.isAfter(now)) {
                return now;
            } else {
                return data;
            }
        }).orElse(now);
    }

    public Interval(Calendar startDate, Calendar endDate) {
        super();
        startDate.setTimeZone(CURRENT_TIME_ZONE);
        endDate.setTimeZone(CURRENT_TIME_ZONE);
        this.startDate = startDate.toInstant();
        final Calendar now = Calendar.getInstance(CURRENT_TIME_ZONE);
        this.endDate = Optional.ofNullable(endDate).map(data -> {
            if (data.after(now)) {
                return now;
            } else {
                return data;
            }
        }).orElse(now).toInstant();
    }

    @Override
    public int hashCode() {
        int result = startDate.hashCode();
        result = 31 * result + endDate.hashCode();
        return result;
    }

    @Override
    public int compareTo(Interval o) {
        if (o.startDate.isAfter(startDate))
            return -1;
        if (o.startDate.isBefore(startDate))
            return 1;
        return 0;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public Instant getEndDate() {
        return endDate;
    }

    @Override
    public String toString() {
        return startDate + ".." + endDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Interval interval = (Interval) o;

        if (!startDate.equals(interval.startDate)) return false;
        return endDate.equals(interval.endDate);

    }
}