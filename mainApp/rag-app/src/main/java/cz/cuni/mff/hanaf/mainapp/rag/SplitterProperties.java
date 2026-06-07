package cz.cuni.mff.hanaf.mainapp.rag;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for the text splitter, bound from the {@code app.rag.splitter} prefix.
 */
@Configuration
@ConfigurationProperties(prefix = "app.rag.splitter")
public class SplitterProperties {
    private int chunkSize;
    private int minChunkSizeChars;
    private int minChunkLengthToEmbed;
    private int maxNumChunks;
    private boolean keepSeparator;
    private int overlapTokens;

    /**
     * Returns the maximum number of tokens per chunk.
     *
     * @return the chunk size
     */
    public int getChunkSize() {
        return chunkSize;
    }

    /**
     * Sets the maximum number of tokens per chunk.
     *
     * @param chunkSize the chunk size to set
     */
    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    /**
     * Returns the minimum number of characters a chunk must have when splitting by sentence boundary.
     *
     * @return the minimum chunk size in characters
     */
    public int getMinChunkSizeChars() {
        return minChunkSizeChars;
    }

    /**
     * Sets the minimum number of characters a chunk must have when splitting by sentence boundary
     *
     * @param minChunkSizeChars the minimum chunk size in characters to set
     */
    public void setMinChunkSizeChars(int minChunkSizeChars) {
        this.minChunkSizeChars = minChunkSizeChars;
    }

    /**
     * Returns the minimum number of characters a chunk must exceed to not be discarded.
     *
     * @return the minimum chunk length to embed
     */
    public int getMinChunkLengthToEmbed() {
        return minChunkLengthToEmbed;
    }

    /**
     * Sets the minimum number of characters a chunk must exceed to not be discarded.
     *
     * @param minChunkLengthToEmbed the minimum chunk length to embed to set
     */
    public void setMinChunkLengthToEmbed(int minChunkLengthToEmbed) {
        this.minChunkLengthToEmbed = minChunkLengthToEmbed;
    }

    /**
     * Returns the maximum number of chunks to divide a document into.
     *
     * @return the maximum number of chunks
     */
    public int getMaxNumChunks() {
        return maxNumChunks;
    }

    /**
     * Sets the maximum number of chunks to divide a document into.
     *
     * @param maxNumChunks the maximum number of chunks to set
     */
    public void setMaxNumChunks(int maxNumChunks) {
        this.maxNumChunks = maxNumChunks;
    }

    /**
     * Returns whether line separators are kept within chunks.
     *
     * @return {@code true} if line separators are kept, and {@code false} if replaced with spaces
     */
    public boolean isKeepSeparator() {
        return keepSeparator;
    }

    /**
     * Sets whether line separators are kept within chunks.
     *
     * @param keepSeparator {@code true} to keep line separators, and {@code false} to replace them with spaces
     */
    public void setKeepSeparator(boolean keepSeparator) {
        this.keepSeparator = keepSeparator;
    }

    /**
     * Returns the number of tokens from the end of each chunk to repeat at the start of the next.
     *
     * @return the number of overlap tokens
     */
    public int getOverlapTokens() {
        return overlapTokens;
    }

    /**
     * Sets the number of tokens from the end of each chunk to repeat at the start of the next chunk.
     *
     * @param overlapTokens the number of overlap tokens to set
     */
    public void setOverlapTokens(int overlapTokens) {
        this.overlapTokens = overlapTokens;
    }
}