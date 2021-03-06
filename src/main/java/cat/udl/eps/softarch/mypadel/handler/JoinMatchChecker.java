package cat.udl.eps.softarch.mypadel.handler;

import cat.udl.eps.softarch.mypadel.domain.*;
import cat.udl.eps.softarch.mypadel.handler.exception.JoinMatchException;
import cat.udl.eps.softarch.mypadel.repository.*;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class JoinMatchChecker {
	@Autowired
	private MatchRepository matchRepository;

	@Autowired
	private ReservationRepository reservationRepository;

	@Autowired
	private JoinMatchRepository joinMatchRepository;

	@Autowired
	private MatchResultRepository matchResultRepository;

	@Autowired
	private MatchResultVerificationRepository matchResultVerificationRepository;

	private List<Match> matchList;

	boolean isInvited(JoinMatch joinMatch) throws JoinMatchException {
		if(joinMatch.getMatch() instanceof PrivateMatch) {
			List<MatchInvitation> matchInvitationList = joinMatch.getMatch().getInvitations();
			if(matchInvitationList.size() == 0){
				throw new JoinMatchException("There is no invitations for this match");
			}
			for (MatchInvitation matchInvitation : matchInvitationList) {
				if(matchInvitation.getInvites().getEmail().equals(joinMatch.getPlayer().getEmail())){
					return true;
				}
			}
		}else {
			return true;
		}
		return false;
	}

	boolean isJoinedAtTheSameDatetime(JoinMatch joinMatch) throws JoinMatchException{
		matchList = matchRepository.findByStartDateStringBetween(
			joinMatch.getMatch().getStartDate()
				.withZoneSameInstant(ZoneId.of("Z")).format(DateTimeFormatter.ISO_DATE_TIME),
			joinMatch.getMatch().getStartDate().plusMinutes(joinMatch.getMatch().getDuration().toMinutes())
				.withZoneSameInstant(ZoneId.of("Z")).format(DateTimeFormatter.ISO_DATE_TIME));

		for(Match match : matchList){
			if(Objects.equals(match.getStartDate(), joinMatch.getMatch().getStartDate()) &&
				!Objects.equals(match.getId(), joinMatch.getMatch().getId())){
				return true;
			}
		}
		return false;
	}

	void reserveCourt(Match match){
		Reservation reservation = new Reservation();
		reservation.setCourtType(match.getCourtType());
		reservation.setReservingMatch(match);
		reservation.setStartDate(match.getStartDate());
		reservation.setDuration(match.getDuration());
		match.setReservation(reservation);
		matchRepository.save(match);
		reservationRepository.save(reservation);
	}

	boolean isMatchFull(Match match){
		List<JoinMatch> joinMatchList = joinMatchRepository.findByMatch(match);
		return joinMatchList.size() == 4;
	}

	void cancelReservation(Match match) {
		Reservation reservation = match.getReservation();
		match.setReservation(null);
		reservation.setReservingMatch(null);
		reservationRepository.delete(reservation.getId());
	}

	boolean pendingResult(JoinMatch joinMatch){
		List<JoinMatch> joinMatches;
		MatchResult matchResult;
		List<MatchResultVerification> matchResultVerifications;
		Player player = joinMatch.getPlayer();

		joinMatches = joinMatchRepository.findByPlayer(player);

		for(JoinMatch j : joinMatches){
			matchResult = matchResultRepository.findByMatch(j.getMatch());
			matchResultVerifications = matchResultVerificationRepository.findByMatchToAgree(matchResult);
			for(MatchResultVerification matchResultVerification: matchResultVerifications){
				if(!matchResultVerification.isAgrees()){
					return true;
				}
			}
		}
		return false;
	}
}
