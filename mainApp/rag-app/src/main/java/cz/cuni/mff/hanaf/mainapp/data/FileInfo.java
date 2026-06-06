package cz.cuni.mff.hanaf.mainapp.data;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

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

    /**
     * No-arg constructor required by JPA.
     */
    public FileInfo() {}

    /**
     * Creates a new {@code FileInfo} with the given file identifier and hash.
     *
     * @param fileId the unique identifier of the stored file; must not be {@code null}
     * @param hash the content hash of the file (e.g. MD5 hex string); must not be {@code null}
     */
    public FileInfo(String fileId, String hash) {
        this.fileId = fileId;
        this.hash = hash;
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
}
