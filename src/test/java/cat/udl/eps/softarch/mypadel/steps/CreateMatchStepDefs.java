package cat.udl.eps.softarch.mypadel.steps;

import static cat.udl.eps.softarch.mypadel.steps.AuthenticationStepDefs.authenticate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import cat.udl.eps.softarch.mypadel.domain.*;
import cat.udl.eps.softarch.mypadel.repository.PublicMatchRepository;
import cat.udl.eps.softarch.mypadel.repository.UserRepository;
import cucumber.api.java.en.And;
import cucumber.api.java.en.When;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

public class CreateMatchStepDefs {

	@Autowired
	private StepDefs stepDefs;

	private ZonedDateTime startDate;

	private Duration duration;

	private PublicMatch match = new PublicMatch();

	@Autowired
	private UserRepository userRepository;

	private long id;

	@Autowired
	private PublicMatchRepository publicMatchRepository;

	@When("^I set a new public match on (\\d+)-(\\d+)-(\\d+) at (\\d+)h for (\\d+) minutes$")
	public void iSetANewPublicMatchOnAtPmForMinutesAndDeadline(int day, int month, int year, int hour, int duration) throws Throwable {
		startDate = ZonedDateTime.of(year, month, day, hour, 0, 0,
			0, ZoneId.of("+00:00"));
		this.duration = Duration.ofMinutes(duration);
		match.setStartDate(startDate);
		match.setDuration(this.duration);
		match.setCourtType(CourtType.INDOOR);
		match.setLevel(Level.ADVANCED);
	}

	@And("^the user creating it is \"([^\"]*)\"$")
	public void theUserCreatingItIs(String username) throws Throwable {
		match.setMatchCreator((Player) userRepository.findByEmail(username));
	}

	@And("^I create it$")
	public void iCreateIt() throws Throwable {
		String message = stepDefs.mapper.writeValueAsString(match);
		stepDefs.result = stepDefs.mockMvc.perform(
			post("/publicMatches")
				.contentType(MediaType.APPLICATION_JSON)
				.content(message)
				.accept(MediaType.APPLICATION_JSON)
				.with(authenticate()))
			.andDo(print());
	}

	@And("^I create a match with a similar hour, (\\d+)h$")
	public void iCreateAMatchWithASimilarHourPm(int matchHour) throws Throwable {
		match.setStartDate(match.getStartDate().withHour(matchHour));
		String message = stepDefs.mapper.writeValueAsString(match);
		stepDefs.result = stepDefs.mockMvc.perform(
			post("/publicMatches")
				.contentType(MediaType.APPLICATION_JSON)
				.content(message)
				.accept(MediaType.APPLICATION_JSON)
				.with(authenticate()))
			.andDo(print());
	}

	@And("^A match with the id (\\d+) has been created$")
	public void aMatchWithTheIdHasBeenCreated(int id) throws Throwable {
		this.id = id;
		PublicMatch publicMatch = publicMatchRepository.findOne(this.id);
		assertThat(publicMatch.getId(), is(this.id));
		assertThat(publicMatch.getDuration(), is(duration));
		assertThat(formatDate(publicMatch.getStartDate()), is(formatDate(startDate)));
		assertThat(formatDate(publicMatch.getCancelationDeadline()), is(formatDate(startDate.minusDays(1))));
		assertThat(publicMatch.getCourtType(), is(CourtType.INDOOR));
		assertThat(publicMatch.getLevel(), is(Level.ADVANCED));
	}

	private String formatDate(ZonedDateTime date) {
		return DateTimeFormatter.ofPattern("dd/MM/yyyy - hh:mm").format(date);
	}

	@And("^The match creator is \"([^\"]*)\"$")
	public void theMatchCreatorIs(String player) throws Throwable {
		stepDefs.result = stepDefs.mockMvc.perform(
			get("/publicMatches/{id}/matchCreator", id)
				.accept(MediaType.APPLICATION_JSON)
				.with(authenticate()))
			.andDo(print())
			.andExpect(jsonPath("$.username", is(player))
			);
	}

	@And("^A join match with the id (\\d+) has been created, having the match (\\d+) and the player \"([^\"]*)\"$")
	public void aJoinMatchHasBeenCreatedHavingTheMatchAndThePlayer(int joinId, int matchId, String playerUsername) throws Throwable {
		stepDefs.result = stepDefs.mockMvc.perform(
			get("/joinMatches/{id}/player", joinId)
				.accept(MediaType.APPLICATION_JSON)
				.with(authenticate()))
			.andDo(print())
			.andExpect(jsonPath("$.username", is(playerUsername))
			);
		stepDefs.result = stepDefs.mockMvc.perform(
			get("/joinMatches/{id}/match", joinId)
				.accept(MediaType.APPLICATION_JSON)
				.with(authenticate()))
			.andDo(print())
			.andExpect(jsonPath("$.id", is(matchId))
			);
	}
}
