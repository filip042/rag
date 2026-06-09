package cz.cuni.mff.hanaf.mainapp.rag.dto;

import java.util.List;

/**
 * Status of the file indexing process for a specific project.
 *
 * @param totalFiles the total number of files scheduled for indexing
 * @param finishedFiles a list of names of the files that have successfully finished indexing
 */
public record IndexStatusResponse(int totalFiles, List<String> finishedFiles) {}