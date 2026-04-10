package cz.cuni.mff.hanaf.mainapp.rag;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.rag.splitter")
public class SplitterProperties {
    private int chunkSize;
    private int minChunkSizeChars;
    private int minChunkLengthToEmbed;
    private int maxNumChunks;
    private boolean keepSeparator;
    private int overlapTokens;

    public int getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public int getMinChunkSizeChars() {
        return minChunkSizeChars;
    }

    public void setMinChunkSizeChars(int minChunkSizeChars) {
        this.minChunkSizeChars = minChunkSizeChars;
    }

    public int getMinChunkLengthToEmbed() {
        return minChunkLengthToEmbed;
    }

    public void setMinChunkLengthToEmbed(int minChunkLengthToEmbed) {
        this.minChunkLengthToEmbed = minChunkLengthToEmbed;
    }

    public int getMaxNumChunks() {
        return maxNumChunks;
    }

    public void setMaxNumChunks(int maxNumChunks) {
        this.maxNumChunks = maxNumChunks;
    }

    public boolean isKeepSeparator() {
        return keepSeparator;
    }

    public void setKeepSeparator(boolean keepSeparator) {
        this.keepSeparator = keepSeparator;
    }

    public int getOverlapTokens() {
        return overlapTokens;
    }

    public void setOverlapTokens(int overlapTokens) {
        this.overlapTokens = overlapTokens;
    }
}