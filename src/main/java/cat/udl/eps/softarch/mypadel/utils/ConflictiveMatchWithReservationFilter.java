package cat.udl.eps.softarch.mypadel.utils;

import static cat.udl.eps.softarch.mypadel.domain.CourtType.INDOOR;
import static cat.udl.eps.softarch.mypadel.domain.CourtType.OUTDOOR;
import static java.util.stream.Collectors.toList;

import cat.udl.eps.softarch.mypadel.domain.Court;
import cat.udl.eps.softarch.mypadel.domain.CourtType;
import cat.udl.eps.softarch.mypadel.domain.Match;
import cat.udl.eps.softarch.mypadel.domain.Reservation;
import cat.udl.eps.softarch.mypadel.repository.CourtRepository;
import cat.udl.eps.softarch.mypadel.repository.MatchRepository;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConflictiveMatchWithReservationFilter {

	@Autowired
	private MatchRepository matchRepository;

	@Autowired
	private CourtRepository courtRepository;

	private List<Court> availableCourts;
	private boolean indoorCourtAvailable;
	private boolean outdoorCourtAvailable;

	public List<Match> findConflictiveMatchesWithReservation(Reservation reservation) {
		initAvailableCourtTypes();
		ZonedDateTime starDateTime = reservation.getStartDate();
		ZonedDateTime endDateTime = reservation.getStartDate().plus(reservation.getDuration());
		return matchRepository.findByStartDateStringBetween(
			starDateTime.withZoneSameInstant(ZoneId.of("Z")).format(DateTimeFormatter.ISO_DATE_TIME),
			endDateTime.withZoneSameInstant(ZoneId.of("Z")).format(DateTimeFormatter.ISO_DATE_TIME))
			.stream()
			.filter(m -> !isReserved(m))
			.filter(m -> !hasAvailableCourt(m))
			.collect(toList());
	}

	private void initAvailableCourtTypes() {
		availableCourts = courtRepository.findByAvailableTrue();
		for (Court c : availableCourts) {
			if (c.isIndoor())
				indoorCourtAvailable = true;
			else
				outdoorCourtAvailable = true;
		}
	}

	private boolean isReserved(Match match) {
		return match.getReservation() != null;
	}

	private boolean hasAvailableCourt(Match match) {
		CourtType courtType = match.getCourtType();
		if (courtType == INDOOR)
			return indoorCourtAvailable;
		else if (courtType == OUTDOOR)
			return outdoorCourtAvailable;
		else
			return indoorCourtAvailable || outdoorCourtAvailable;
	}
}
