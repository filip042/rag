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

public class OverlapTextSplitter extends TextSplitter {
    private final Encoding encoding;
    private final int chunkSize;
    private final int minChunkSizeChars;
    private final int minChunkLengthToEmbed;
    private final int maxNumChunks;
    private final boolean keepSeparator;
    private final int overlapTokens;

    public OverlapTextSplitter(int chunkSize, int minChunkSizeChars, int minChunkLengthToEmbed, int maxNumChunks, boolean keepSeparator, int overlapTokens) {
        EncodingRegistry registry = Encodings.newLazyEncodingRegistry();
        this.encoding = registry.getEncoding(EncodingType.CL100K_BASE);
        this.chunkSize = chunkSize;
        this.minChunkSizeChars = minChunkSizeChars;
        this.minChunkLengthToEmbed = minChunkLengthToEmbed;
        this.maxNumChunks = maxNumChunks;
        this.keepSeparator = keepSeparator;
        this.overlapTokens = overlapTokens;
    }

    protected List<String> splitText(String text) {
        return this.doSplit(text, this.chunkSize);
    }

    protected List<String> doSplit(String text, int chunkSize) {
        if (text != null && !text.trim().isEmpty()) {
            List<Integer> tokens = this.getEncodedTokens(text);
            List<String> chunks = new ArrayList<>();
            int num_chunks = 0;

            while(!tokens.isEmpty() && num_chunks < this.maxNumChunks) {
                List<Integer> chunk = tokens.subList(0, Math.min(chunkSize, tokens.size()));
                String chunkText = this.decodeTokens(chunk);
                if (chunkText.trim().isEmpty()) {
                    tokens = tokens.subList(chunk.size(), tokens.size());
                } else {
                    int lastPunctuation = Math.max(chunkText.lastIndexOf(46), Math.max(chunkText.lastIndexOf(63), Math.max(chunkText.lastIndexOf(33), chunkText.lastIndexOf(10))));
                    if (lastPunctuation != -1 && lastPunctuation > this.minChunkSizeChars) {
                        chunkText = chunkText.substring(0, lastPunctuation + 1);
                    }

                    String chunkTextToAppend = this.keepSeparator ? chunkText.trim() : chunkText.replace(System.lineSeparator(), " ").trim();
                    if (chunkTextToAppend.length() > this.minChunkLengthToEmbed) {
                        chunks.add(chunkTextToAppend);
                    }

                    int stepSize = chunk.size() - this.overlapTokens;
                    // System.out.println(stepSize);
                    if (stepSize <= 0 || stepSize > tokens.size()) {
                        tokens = new ArrayList<>();
                        break;  // Avoid infinite loop
                    }
                    tokens = tokens.subList(stepSize, tokens.size());
                    // tokens = tokens.subList(this.getEncodedTokens(chunkText).size() - this.overlapTokens, tokens.size()); // todo this is bad
                    ++num_chunks;
                }
            }

            if (!tokens.isEmpty()) {
                // System.out.println("not empty:" + tokens);
                String remaining_text = this.decodeTokens(tokens).replace(System.lineSeparator(), " ").trim();
                if (remaining_text.length() > this.minChunkLengthToEmbed) {
                    chunks.add(remaining_text);
                }
            }

            // System.out.println(chunks);
            // System.out.println(chunkSize);
            return chunks;
        } else {
            return new ArrayList();
        }
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
