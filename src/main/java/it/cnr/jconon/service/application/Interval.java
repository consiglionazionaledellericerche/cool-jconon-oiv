package it.cnr.jconon.service.application;

import java.time.Instant;
import java.util.Calendar;

public class Interval implements Comparable<Interval>{
	private Instant startDate;
	private Instant endDate;
			
	public Interval() {
		super();
	}

	public Interval(Instant startDate, Instant endDate) {
		super();
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public Interval startDate(Calendar startDate) {
		this.startDate = startDate.toInstant();
		return this;
	}

	public Interval endDate(Calendar endDate) {
		this.endDate = endDate.toInstant();
		return this;
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
}