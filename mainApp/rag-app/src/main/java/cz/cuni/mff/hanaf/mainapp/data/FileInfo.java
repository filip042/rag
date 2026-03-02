package cz.cuni.mff.hanaf.mainapp.data;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class FileInfo {

    @Column(name = "file_id", nullable = false)
    private String fileId;

    @Column(name = "file_hash", nullable = false)
    private String hash;

    public FileInfo() {}

    public FileInfo(String fileId, String hash) {
        this.fileId = fileId;
        this.hash = hash;
    }

    public String getFileId() {
        return fileId;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}

