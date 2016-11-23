package it.cnr.jconon.service.application;

import java.time.Instant;
import java.util.Calendar;

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
		this.endDate = endDate;
	}

	public Interval(Calendar startDate, Calendar endDate) {
		super();
		this.startDate = startDate.toInstant();
		this.endDate = endDate.toInstant();
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