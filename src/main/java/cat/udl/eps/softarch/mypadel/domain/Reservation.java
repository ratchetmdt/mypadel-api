package cat.udl.eps.softarch.mypadel.domain;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.time.ZonedDateTime;


@Entity
public class Reservation extends UriEntity<Long> {


	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	private ZonedDateTime startDate;

	@NotNull
	private Duration duration;

	@NotNull
	private CourtType courtType;

	@ManyToOne
	@JsonIdentityReference(alwaysAsId = true)
	private Court court;

	@OneToOne
	private Match reservingMatch;

	@Override
	public Long getId() {
		return id;
	}

	public ZonedDateTime getStartDate() {
		return startDate;
	}

	public void setStartDate(ZonedDateTime startDate) {
		this.startDate = startDate;
	}

	public Duration getDuration() {
		return duration;
	}

	public void setDuration(Duration duration) {
		this.duration = duration;
	}

	public CourtType getCourtType() {
		return courtType;
	}

	public void setCourtType(CourtType court) {
		this.courtType = court;
	}

	public Court getCourt() {
		return court;
	}

	public void setCourt(Court court) {
		this.court = court;
	}

	public Match getReservingMatch() {
		return reservingMatch;
	}

	public void setReservingMatch(Match reservingMatch) {
		this.reservingMatch = reservingMatch;
	}
}
