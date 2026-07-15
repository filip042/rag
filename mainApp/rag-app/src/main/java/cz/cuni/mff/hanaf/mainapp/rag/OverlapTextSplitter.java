package cz.cuni.mff.hanaf.mainapp.rag;

import org.springframework.ai.transformer.splitter.TextSplitter;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.EncodingType;
import com.knuddels.jtokkit.api.IntArrayList;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.util.Assert;

/**
 * Token-based text splitter that splits documents into overlapping chunks.
 * Chunks are broken at sentence boundaries where possible, and consecutive chunks
 * share a configurable number of tokens to preserve context across splits.
 */
public class OverlapTextSplitter extends TextSplitter {
    private final Encoding encoding;
    private final int chunkSize;
    private final int minChunkSizeChars;
    private final int minChunkLengthToEmbed;
    private final int maxNumChunks;
    private final boolean keepSeparator;
    private final int overlapTokens;

    /**
     * Creates a new {@code OverlapTextSplitter} with the given configuration.
     *
     * @param chunkSize the maximum number of tokens per chunk
     * @param minChunkSizeChars the minimum number of characters a chunk must have when splitting by sentence boundary
     * @param minChunkLengthToEmbed the number of characters a chunk must exceed to not be discarded
     * @param maxNumChunks the maximum number of chunks to produce from a single document
     * @param keepSeparator {@code true} to preserve line separators within chunks, and {@code false} to replace them with spaces
     * @param overlapTokens the number of tokens from the end of each chunk to repeat at the start of the next
     */
    public OverlapTextSplitter(int chunkSize, int minChunkSizeChars, int minChunkLengthToEmbed, int maxNumChunks, boolean keepSeparator, int overlapTokens) {
        if (chunkSize < 1 || chunkSize <= overlapTokens) {
            throw new IllegalArgumentException();
        }
        EncodingRegistry registry = Encodings.newLazyEncodingRegistry();
        this.encoding = registry.getEncoding(EncodingType.CL100K_BASE);
        this.chunkSize = chunkSize;
        this.minChunkSizeChars = minChunkSizeChars;
        this.minChunkLengthToEmbed = minChunkLengthToEmbed;
        this.maxNumChunks = maxNumChunks;
        this.keepSeparator = keepSeparator;
        this.overlapTokens = overlapTokens;
    }

    /**
     * Splits the given text using the given chunk size.
     *
     * @param text the text to split
     * @return the list of chunk strings
     */
    @Override
    protected List<String> splitText(String text) {
        return this.doSplit(text, this.chunkSize);
    }

    /**
     * Splits the given text into overlapping token-based chunks, trimming each to the
     * nearest sentence boundary where possible. Chunks below {@code minChunkLengthToEmbed}
     * are discarded. Any remaining tokens after {@code maxNumChunks} are appended as a
     * final chunk if long enough.
     *
     * @param text the text to split
     * @param chunkSize the maximum number of tokens per chunk
     * @return the list of chunk strings
     */
    protected List<String> doSplit(String text, int chunkSize) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<Integer> tokens = this.getEncodedTokens(text);
        List<String> chunks = new ArrayList<>();
        int num_chunks = 0;
        boolean maxChunksReached = false;

        while (!tokens.isEmpty()) {
            if (num_chunks >= this.maxNumChunks) {
                maxChunksReached = true;
                break;
            }

            boolean isFinalWindow = tokens.size() <= chunkSize;

            List<Integer> chunk = tokens.subList(0, Math.min(chunkSize, tokens.size()));
            String chunkText = this.decodeTokens(chunk);
            if (chunkText.trim().isEmpty()) {
                tokens = tokens.subList(chunk.size(), tokens.size());
                continue;
            }

            int actualChunkSize = chunk.size();

            int lastPunctuation = Math.max(chunkText.lastIndexOf('.'), Math.max(chunkText.lastIndexOf('?'), Math.max(chunkText.lastIndexOf('!'), chunkText.lastIndexOf('\n'))));
            if (lastPunctuation != -1 && lastPunctuation > this.minChunkSizeChars) {
                chunkText = chunkText.substring(0, lastPunctuation + 1);
                actualChunkSize = this.getEncodedTokens(chunkText).size();
            }

            while (chunkText.indexOf('\uFFFD') != -1 && actualChunkSize > 1) {
                actualChunkSize--;
                chunkText = this.decodeTokens(tokens.subList(0, actualChunkSize));
            }

            String chunkTextToAppend = this.keepSeparator ? chunkText.trim() : chunkText.replace(System.lineSeparator(), " ").trim();
            if (chunkTextToAppend.length() > this.minChunkLengthToEmbed) {
                chunks.add(chunkTextToAppend);
            }
            ++num_chunks;

            if (isFinalWindow && actualChunkSize >= chunk.size()) {
                tokens = new ArrayList<>();
                break;
            }

            if (isFinalWindow) {
                tokens = tokens.subList(actualChunkSize, tokens.size());
                break;
            }

            int stepSize = actualChunkSize - this.overlapTokens;
            if (stepSize <= 0 || stepSize > tokens.size()) {
                tokens = new ArrayList<>();
                break;
            }
            tokens = tokens.subList(stepSize, tokens.size());
        }

        if (!maxChunksReached && !tokens.isEmpty()) {
            String remaining_text = this.decodeTokens(tokens);
            int size = tokens.size();
            while (remaining_text.indexOf('\uFFFD') != -1 && size > 1) {
                size--;
                remaining_text = this.decodeTokens(tokens.subList(0, size));
            }
            remaining_text = remaining_text.replace(System.lineSeparator(), " ").trim();
            if (remaining_text.length() > this.minChunkLengthToEmbed) {
                chunks.add(remaining_text);
            }
        }

        return chunks;
    }

    private List<Integer> getEncodedTokens(String text) {
        Assert.notNull(text, "Text must not be null");
        return this.encoding.encode(text).boxed();
    }

    private String decodeTokens(List<Integer> tokens) {
        Assert.notNull(tokens, "Tokens must not be null");
        IntArrayList tokensIntArray = new IntArrayList(tokens.size());
        Objects.requireNonNull(tokensIntArray);
        tokens.forEach(tokensIntArray::add);
        return this.encoding.decode(tokensIntArray);
    }
}
