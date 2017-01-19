package it.cnr.si.cool.jconon.service.application;

import java.time.Instant;
import java.util.Calendar;
import java.util.Optional;

public class Interval implements Comparable<Interval>{
	@Override
	public int hashCode() {
		int result = startDate.hashCode();
		result = 31 * result + endDate.hashCode();
		return result;
	}

	private Instant startDate;

	private Instant endDate;

	public Interval(Instant startDate, Instant endDate) {
		super();
		this.startDate = startDate;
		final Instant now = Calendar.getInstance().toInstant();
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
		this.startDate = startDate.toInstant();
		final Calendar now = Calendar.getInstance();
		this.endDate = Optional.ofNullable(endDate).map(data -> {
			if (data.after(now)) {
				return now;
			} else {
				return data;
			}
		}).orElse(now).toInstant();
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