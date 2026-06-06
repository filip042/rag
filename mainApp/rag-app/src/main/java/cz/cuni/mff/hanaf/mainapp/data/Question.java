package cz.cuni.mff.hanaf.mainapp.data;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Set;

/**
 * JPA entity representing a question asked against a project's indexed documents,
 * along with the generated answer, its sources, and the time it was answered.
 */
@Entity
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    private String question;

    @Lob
    private String answer;

    private Instant answerTime;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> sources;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    /**
     * Returns the unique identifier of this question, or {@code null} if not yet persisted.
     *
     * @return the question id
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the unique identifier of this question.
     *
     * @param id the id to set
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Returns the question text.
     *
     * @return the question
     */
    public String getQuestion() {
        return question;
    }

    /**
     * Sets the question text.
     *
     * @param question the question to set
     */
    public void setQuestion(String question) {
        this.question = question;
    }

    /**
     * Returns the answer text.
     *
     * @return the answer
     */
    public String getAnswer() {
        return answer;
    }

    /**
     * Sets the answer text.
     *
     * @param answer the answer to set
     */
    public void setAnswer(String answer) {
        this.answer = answer;
    }

    /**
     * Returns the time at which the answer was generated.
     *
     * @return the answer time
     */
    public Instant getAnswerTime() {
        return this.answerTime;
    }

    /**
     * Returns the answer time formatted with the system default timezone and a medium format style.
     *
     * @return the formatted answer time
     */
    public String getFormattedAnswerTime() {
        return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withZone(ZoneId.systemDefault()).format(answerTime);
    }

    /**
     * Sets the time at which the answer was generated.
     *
     * @param answerTime the answer time to set
     */
    public void setAnswerTime(Instant answerTime) {
        this.answerTime = answerTime;
    }

    /**
     * Returns the set of source file names that contributed to the answer.
     *
     * @return the sources
     */
    public Set<String> getSources() {
        return sources;
    }

    /**
     * Sets the source file names that contributed to the answer.
     *
     * @param sources the sources to set
     */
    public void setSources(Set<String> sources) {
        this.sources = sources;
    }

    /**
     * Returns the project this question belongs to.
     *
     * @return the project
     */
    public Project getProject() {
        return project;
    }

    /**
     * Sets the project this question belongs to.
     *
     * @param project the project to set
     */
    public void setProject(Project project) {
        this.project = project;
    }
}
