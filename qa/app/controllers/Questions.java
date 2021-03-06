package controllers;

import java.io.File;
import java.util.List;

import models.Answer;
import models.Comment;
import models.Entry;
import models.FileEntry;
import models.Question;
import models.Search;
import models.User;
import models.Vote;
import models.Entry.ContentState;
import play.data.validation.Required;
import play.mvc.Before;
import play.mvc.Controller;

/**
 * View questions.
 */
public class Questions extends Controller {

	/**
	 * Sets the connected user.
	 */
	@Before
	static void setConnectedUser() {
		if (Security.isConnected()) {
			User user = User.find("byName", Security.connected()).first();
			renderArgs.put("user", user);
		}
	}

	/**** VIEWS ****/

	/**
	 * List and show questions.
	 */

	public static void home() {
		List<Question> questions = Question.questions();
		render(questions);
	}

	public static void question(long id) {
		Question display = Question.findById(id);
		List<Question> questions = Question.questions();
		render("Questions/home.html", questions, display);
	}

	/*** AJAX ***/

	/**
	 * Get a question.
	 */
	public static void get(long id) {
		Question question = Question.findById(id);
		if (question == null) {
			render("Questions/question.html");
		} else {
			List<Answer> answers = question.answers();
			render("Questions/question.html", question, answers);
		}
	}

	public static void hot() {
		List<Question> questions = Question.questions();
		render("Questions/list.html", questions);
	}

	public static void active() {
		List<Question> questions = Question.recentQuestions();
		render("Questions/list.html", questions);
	}

	public static void mine() {
		if (!Security.isConnected()) {
			badRequest();
		} else {
			List<Question> questions = ((User) User.find("byName",
					Security.connected()).first()).questions();
			render("Questions/list.html", questions);
		}
	}

	public static void search(String string) {
		List<Question> questions = Search.searchQuestions(string);
		render("Questions/list.html", questions);
	}

	public static void form(String type, long id) {
		Entry entry = Entry.findById(id);
		render(type, entry);
	}

	public static void entry(long id) {
		Entry entry = Entry.findById(id);
		render("Questions/entry.html", entry);
	}

	public static void add(@Required String title, @Required String content,
			String tags) {
		User user = User.find("byName", Security.connected()).first();
		if (user != null && !validation.hasErrors()) {
			Question question = user.addQuestion(title, content);
			String[] separatedTags = tags.split(", ");
			for (String tag : separatedTags) {
				if (!tag.equals(""))
					question.tagItWith(tag);
			}

			renderJSON("{\"success\": 1, \"id\": " + question.id + "}");
		} else {
			renderJSON("{\"success\": 0}");
		}
	}

	public static void answer(long id, @Required String content, File file) {
		User user = User.find("byName", Security.connected()).first();
		Question question = Question.findById(id);
		if (user != null && question != null && !validation.hasErrors()) {
			Answer answer = question.answer(user, content);
			if (file != null && file.exists()) {
				user.addFileToEntry(file, answer);
				question(id);
			}
			renderJSON("{\"success\": 1, \"id\": " + answer.id + "}");
		} else {
			renderJSON("{\"success\": 0}");
		}
	}

	public static void edit(long id, String content, String tags, File file) {
		User user = (User) User.find("byName", Security.connected()).first();
		Entry entry = Entry.findById(id);
		entry.edit(content, user);
		if (entry instanceof Question) {
			((Question) entry).removeAllTags();
			if (!tags.equals("Tags")) {
				String[] separatedTags = tags.split(", ");

				for (String tag : separatedTags) {
					((Question) entry).tagItWith(tag);
				}

			}
			entry.save();
		} else if (file != null && file.exists()) {
			user.addFileToEntry(file, entry);
		}

		if (entry instanceof Answer) {
			question(((Answer) entry).question.id);
		} else {
			question(entry.id);

		}

	}

	public static void voteUp(long id) {
		Entry entry = Entry.<Entry> findById(id);
		User user = User.find("byName", Security.connected()).first();
		if (entry != null && user != null) {
			entry.voteUp(user);
			entry.save();
		}
		render("Questions/entry.html", entry);
	}

	public static void voteDown(long id) {
		Entry entry = Entry.<Entry> findById(id);
		User user = User.find("byName", Security.connected()).first();
		if (entry != null && user != null) {
			entry.voteDown(user);
			entry.save();
		}
		render("Questions/entry.html", entry);
	}

	public static void removeVote(long id) {
		User user = User.find("byName", Security.connected()).first();
		Entry entry = Entry.findById(id);
		if (entry != null && user != null) {
			entry.removeVote((Vote) Vote.find("byOwnerAndEntry", user, entry)
					.first());
		}
		render("Questions/entry.html", entry);
	}

	public static void setBestAnswer(long id) {
		Answer answer = Answer.<Answer> findById(id);
		if (answer == null) {
			badRequest();
		} else {
			if (answer.question.owner.name.equals(Security.connected())
					&& answer.question.canSetBestAnswer()) {
				answer.question.setBestAnswer(answer);
			}
			Question question = answer.question;
			render("Questions/question.html", question);
		}
	}

	public static void resetBestAnswer(long id) {
		Question question = Question.<Question> findById(id);
		if (question == null) {
			badRequest();
		} else {
			if (question.owner.name.equals(Security.connected())
					&& question.canSetBestAnswer()) {
				question.resetBestAnswer();
			}
			render("Questions/question.html", question);
		}
	}

	public static void delete(long id) {

		Entry entry = Entry.findById(id);
		if (entry == null)
			home();

		entry.delete();

		if (entry instanceof Question)
			home();

		question(((Answer) entry).question.id);
	}

	public static void setNotificationAsRed(long id) {

		models.Notification.hasBeenRed(id);
	}

	public static void deleteEntry(long id) {

		Entry entry = Entry.findById(id);
		entry.delete();

		Question question = entry instanceof Question ? (Question) entry
				: ((Answer) entry).question;

		render("Questions/question.html", question);
	}

	public static void deleteFileEntry(long id, long qid) {
		FileEntry entry = FileEntry.findById(id);
		if (entry != null)
			entry.delete();
		question(qid);
	}

	public static void getFile(long id) {

		FileEntry entry = FileEntry.findById(id);

		File file = new File(entry.getAbsolutePath());

		renderBinary(file, entry.uploadFilename);
	}

	public static void version(long id) {
		ContentState state = ContentState.findById(id);
		if (state != null) {
			renderText(state.content);
		}
	}

	public static void comment(long id, @Required String content) {
		User user = User.find("byName", Security.connected()).first();
		Entry entry = Entry.findById(id);

		if (!validation.hasErrors() && entry != null && user != null) {
			user.addComment(entry, content);

			if (entry instanceof models.Question)
				question(id);
			else
				question(((Answer) entry).question.id);
		} else {
			home();
		}
	}

	public static void deleteComment(long id) {

		Comment comment = Comment.findById(id);
		if (comment != null)
			comment.delete();

		renderText("");
	}
}