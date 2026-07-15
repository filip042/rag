package cz.cuni.mff.hanaf.mainapp.rag;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OverlapTextSplitterTest {

    static class TestSplitter extends OverlapTextSplitter {

        TestSplitter(int chunkSize, int minChunkSizeChars, int minChunkLengthToEmbed,
                     int maxNumChunks, boolean keepSeparator, int overlapTokens) {
            super(chunkSize, minChunkSizeChars, minChunkLengthToEmbed, maxNumChunks, keepSeparator, overlapTokens);
        }

        List<String> split(String text) {
            return doSplit(text, thisChunkSize());
        }

        List<String> split(String text, int chunkSize) {
            return doSplit(text, chunkSize);
        }

        private int thisChunkSize() {
            return 20;
        }
    }

    @Test
    void nullTextReturnsEmptyList() {
        TestSplitter splitter = new TestSplitter(20, 0, 0, 100, true, 5);

        assertTrue(splitter.split((String) null).isEmpty());
    }

    @Test
    void blankTextReturnsEmptyList() {
        TestSplitter splitter = new TestSplitter(20, 0, 0, 100, true, 5);

        assertTrue(splitter.split("   \n\t ").isEmpty());
    }

    @Test
    void filtersTooSmallChunks() {
        TestSplitter splitter = new TestSplitter(20, 0, 100, 100, true, 5);

        assertTrue(splitter.split("Short.").isEmpty());
    }

    @Test
    void shouldNotLoseContentBetweenSentenceBoundaryAndOverlap() {
        TestSplitter splitter = new TestSplitter(50, 10, 0, 100, true, 10);

        String text =
                "Sentence one. " +
                        "Sentence two is intentionally very long so that the token window " +
                        "ends in the middle of this sentence and the splitter trims back " +
                        "to the previous sentence boundary. " +
                        "Sentence three contains information that must still be present.";

        List<String> chunks = splitter.split(text, 50);
        String reconstructed = String.join(" ", chunks);

        assertTrue(reconstructed.contains("Sentence three"), "Content disappeared after trimming.");
    }

    @Test
    void overlapGreaterThanChunkSizeShouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> new OverlapTextSplitter(50, 0, 0, 100, true, 60));
    }

    @Test
    void shouldNotDuplicateLastChunk() {
        TestSplitter splitter = new TestSplitter(20, 0, 0, 100, true, 10);

        String text = """
                First sentence.
                Second sentence.
                Third sentence.
                Fourth sentence.
                Fifth sentence.
                Sixth sentence.
                Seventh sentence.
                """;

        List<String> chunks = splitter.split(text);

        assertTrue(chunks.size() > 1);

        String last = chunks.getLast();
        String previous = chunks.get(chunks.size() - 2);

        assertFalse(previous.endsWith(last), "End of final chunk should not be duplicated.");
    }

    @Test
    void chunksShouldNotContainReplacementCharacterAndShouldNotDropContext() {
        TestSplitter splitter = new TestSplitter(30, 0, 0, 100, true, 10);

        // LLM-generated English text containing common multi-byte UTF-8 characters
        String text = """
            “Hello there!” she said—smiling slightly at the façade of the new café.
            It was quite naïve to think that a résumé alone would secure the job... 🚀✨
            However, the jalapeño poppers they served were absolutely top-tier! 🌶️🤤
            Just adding a bit more text to ensure we cross multiple 30-token chunk boundaries.
            """;

        List<String> chunks = splitter.split(text, 30);

        assertTrue(chunks.stream().noneMatch(chunk -> chunk.contains("\uFFFD")), "Chunks contain Unicode replacement characters.");

    }

    @Test
    void finalChunkShouldNotBecomeHugeWhenMaxChunksReached() {
        TestSplitter splitter = new TestSplitter(20, 0, 0, 1, true, 5);
        String text = "Sentence. ".repeat(500);
        List<String> chunks = splitter.split(text, 20);

        assertEquals(1, chunks.size());
        assertTrue(chunks.getFirst().length() < 500, "Remaining document was emitted as one huge chunk.");
    }

    @Test
    void zeroChunkSizeShouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> new OverlapTextSplitter(0, 0, 0, 100, true, 0));
    }
}