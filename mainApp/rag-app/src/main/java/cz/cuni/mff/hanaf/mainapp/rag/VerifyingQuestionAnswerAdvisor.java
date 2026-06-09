package cz.cuni.mff.hanaf.mainapp.rag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * A {@link BaseAdvisor} that augments chat prompts with pre-verified relevant documents
 * rather than performing its own vector store retrieval.
 * If no relevant documents are available, substitutes a configurable fallback prompt.
 */
public class VerifyingQuestionAnswerAdvisor implements BaseAdvisor {
    private static final PromptTemplate DEFAULT_PROMPT_TEMPLATE = new PromptTemplate("{query}\n\nContext information is below, surrounded by ---------------------\n\n---------------------\n{question_answer_context}\n---------------------\n\nGiven the context and provided history information and not prior knowledge,\nreply to the user comment. If the answer is not in the context, inform\nthe user that you can't answer the question.\n");
    private static final int DEFAULT_ORDER = 0;
    private final PromptTemplate promptTemplate;
    private final Scheduler scheduler;
    private final int order;
    private final String doNotKnowPrompt;
    private List<Document> verifiedDocuments;

    private VerifyingQuestionAnswerAdvisor(@Nullable PromptTemplate promptTemplate, @Nullable String doNotKnowPrompt, @Nullable Scheduler scheduler, int order, List<Document> verifiedDocuments) {
        this.promptTemplate = promptTemplate != null ? promptTemplate : DEFAULT_PROMPT_TEMPLATE;
        this.scheduler = scheduler != null ? scheduler : BaseAdvisor.DEFAULT_SCHEDULER;
        this.order = order;
        this.doNotKnowPrompt = doNotKnowPrompt;
        this.verifiedDocuments = verifiedDocuments;
    }

    /**
     * Returns a new {@link Builder} for a {@code VerifyingQuestionAnswerAdvisor}.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /** {@inheritDoc} */
    public int getOrder() {
        return this.order;
    }

    /**
     * Augments the user message with the verified documents as context.
     * If no documents are available, uses the fallback prompt.
     *
     * @param chatClientRequest the incoming request
     * @param advisorChain the advisor chain
     * @return the augmented request
     */
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        String query = chatClientRequest.prompt().getUserMessage().getText();
        Map<String, Object> context = new HashMap(chatClientRequest.context());
        context.put("qa_retrieved_documents", verifiedDocuments);
        String documentContext = verifiedDocuments.stream()
                .map(Document::getText)
                .collect(Collectors.joining(System.lineSeparator()));
        String augmentedUserText = this.promptTemplate.render(
                Map.of("query", query, "question_answer_context", documentContext));
        if (verifiedDocuments.isEmpty()) {
            augmentedUserText = doNotKnowPrompt;
        }
        return chatClientRequest.mutate()
                .prompt(chatClientRequest.prompt().augmentUserMessage(augmentedUserText))
                .context(context)
                .build();
    }

    /** {@inheritDoc} */
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        ChatResponse.Builder chatResponseBuilder;
        if (chatClientResponse.chatResponse() == null) {
            chatResponseBuilder = ChatResponse.builder();
        } else {
            chatResponseBuilder = ChatResponse.builder().from(chatClientResponse.chatResponse());
        }
        chatResponseBuilder.metadata("qa_retrieved_documents",
                chatClientResponse.context().get("qa_retrieved_documents"));
        return ChatClientResponse.builder()
                .chatResponse(chatResponseBuilder.build())
                .context(chatClientResponse.context())
                .build();
    }

    /**
     * Returns the documents that were added to the prompt as context.
     *
     * @return the verified documents
     */
    public List<Document> getVerifiedDocuments() {
        return this.verifiedDocuments;
    }

    /** {@inheritDoc} */
    public Scheduler getScheduler() {
        return this.scheduler;
    }

    /**
     * Builder for {@link VerifyingQuestionAnswerAdvisor}.
     */
    public static final class Builder {
        private PromptTemplate promptTemplate;
        private String doNotKnowPrompt;
        private Scheduler scheduler;
        private int order = 0;
        private List<Document> verifiedDocuments;

        private Builder() {}

        /**
         * Sets the prompt template used to augment the user message with document context.
         *
         * @param promptTemplate the prompt template to use
         * @return this builder
         */
        public Builder promptTemplate(PromptTemplate promptTemplate) {
            Assert.notNull(promptTemplate, "promptTemplate cannot be null");
            this.promptTemplate = promptTemplate;
            return this;
        }

        /**
         * Sets the fallback prompt used when no relevant documents are found.
         *
         * @param prompt the fallback prompt
         * @return this builder
         */
        public Builder doNotKnowPrompt(String prompt) {
            Assert.notNull(prompt, "prompt cannot be null");
            this.doNotKnowPrompt = prompt;
            return this;
        }

        /**
         * Sets whether to protect stream processing from blocking by using a bounded elastic scheduler.
         *
         * @param protectFromBlocking {@code true} to use the default non-blocking scheduler, and {@code false} to use an immediate scheduler
         * @return this builder
         */
        public Builder protectFromBlocking(boolean protectFromBlocking) {
            this.scheduler = protectFromBlocking ? BaseAdvisor.DEFAULT_SCHEDULER : Schedulers.immediate();
            return this;
        }

        /**
         * Sets the scheduler used for stream processing.
         *
         * @param scheduler the scheduler to use
         * @return this builder
         */
        public Builder scheduler(Scheduler scheduler) {
            this.scheduler = scheduler;
            return this;
        }

        /**
         * Sets the advisor order.
         *
         * @param order the order to use
         * @return this builder
         */
        public Builder order(int order) {
            this.order = order;
            return this;
        }

        /**
         * Sets the pre-verified documents to inject into the prompt as context.
         *
         * @param verifiedDocuments the documents to use
         * @return this builder
         */
        public Builder documents(List<Document> verifiedDocuments) {
            this.verifiedDocuments = verifiedDocuments;
            return this;
        }

        /**
         * Builds and returns a new {@link VerifyingQuestionAnswerAdvisor}.
         *
         * @return a new advisor instance
         */
        public VerifyingQuestionAnswerAdvisor build() {
            return new VerifyingQuestionAnswerAdvisor(promptTemplate, doNotKnowPrompt, scheduler, order, verifiedDocuments);
        }
    }
}