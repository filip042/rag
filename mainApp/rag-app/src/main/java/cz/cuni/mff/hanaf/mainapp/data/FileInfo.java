package cz.cuni.mff.hanaf.mainapp.data;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

/**
 * Embeddable JPA component holding metadata about a stored file.
 * Instances are treated as immutable after construction and embedded
 * within a {@link Project} rather than mapped to their own table.
 */
@Embeddable
public class FileInfo {

    @Column(name = "file_id", nullable = false)
    private String fileId;

    @Column(name = "file_hash", nullable = false)
    private String hash;

    private Instant indexTime;

    /**
     * Default constructor required by JPA/Hibernate for reflective instantiation.
     * Not intended for direct use.
     */
    protected FileInfo() {
    }

    /**
     * Creates a new {@code FileInfo} with the given file identifier and hash.
     *
     * @param fileId the unique identifier of the stored file; must not be {@code null}
     * @param hash the content hash of the file (e.g. MD5 hex string); must not be {@code null}
     */
    public FileInfo(String fileId, String hash) {
        this.fileId = fileId;
        this.hash = hash;
        this.indexTime = Instant.now();
    }

    /**
     * Returns the unique identifier of the stored file.
     *
     * @return the file id
     */
    public String getFileId() {
        return fileId;
    }

    /**
     * Returns the content hash of the file.
     *
     * @return the file hash
     */
    public String getHash() {
        return hash;
    }

    /**
     * Returns the indexation time of the file.
     *
     * @return the file hash
     */
    public Instant getIndexTime() {
        return indexTime;
    }

    /**
     * Returns the indexation time formatted with the system default timezone and a medium format style.
     *
     * @return the formatted answer time
     */
    public String getFormattedIndexTime() {
        return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withZone(ZoneId.systemDefault()).format(indexTime);
    }
}
