package com.example.tickertracker.web.dto; // TODO: change to your package

import java.time.LocalDate;

/** periodStart is the Monday of the week (weekly) or the 1st of the month (monthly). */
public record TrendPoint(LocalDate periodStart, long sightings) {
}
