import models.Question;
import models.User;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.test.Fixtures;

@OnApplicationStart
public class Bootstrap extends Job {

	public void doJob() {
		if (User.count() == 0) {
			Fixtures.load("initial-data.yml");

			User tobias = User.find("byName", "Tobias").first();
			User simon = User.find("byName", "Simon").first();
			Question question = Question.find("byOwner", tobias).first();
			question.voteUp(simon);

		}

	}

}