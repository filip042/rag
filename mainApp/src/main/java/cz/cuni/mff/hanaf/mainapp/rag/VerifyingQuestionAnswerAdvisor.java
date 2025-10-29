package cz.cuni.mff.hanaf.mainapp.rag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import cz.cuni.mff.hanaf.mainapp.llm.LlmMethods;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionTextParser;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

public class VerifyingQuestionAnswerAdvisor implements BaseAdvisor {
    public static final String RETRIEVED_DOCUMENTS = "qa_retrieved_documents";
    public static final String FILTER_EXPRESSION = "qa_filter_expression";
    private static final PromptTemplate DEFAULT_PROMPT_TEMPLATE = new PromptTemplate("{query}\n\nContext information is below, surrounded by ---------------------\n\n---------------------\n{question_answer_context}\n---------------------\n\nGiven the context and provided history information and not prior knowledge,\nreply to the user comment. If the answer is not in the context, inform\nthe user that you can't answer the question.\n");
    private static final int DEFAULT_ORDER = 0;
    private final VectorStore vectorStore;
    private final PromptTemplate promptTemplate;
    private final SearchRequest searchRequest;
    private final Scheduler scheduler;
    private final int order;
    private final LlmMethods llmMethods;

    private List<Document> verifiedDocuments = List.of(); // todo maybe unnecessary
    private AtomicInteger counter;
    private AtomicBoolean verified;

    VerifyingQuestionAnswerAdvisor(VectorStore vectorStore, SearchRequest searchRequest, @Nullable PromptTemplate promptTemplate, @Nullable Scheduler scheduler, int order, LlmMethods llmMethods, AtomicInteger verifiedCounter, AtomicBoolean verified) {
        Assert.notNull(vectorStore, "vectorStore cannot be null");
        Assert.notNull(searchRequest, "searchRequest cannot be null");
        Assert.notNull(llmMethods, "llmMethods cannot be null");
        this.vectorStore = vectorStore;
        this.searchRequest = searchRequest;
        this.promptTemplate = promptTemplate != null ? promptTemplate : DEFAULT_PROMPT_TEMPLATE;
        this.scheduler = scheduler != null ? scheduler : BaseAdvisor.DEFAULT_SCHEDULER;
        this.order = order;
        this.llmMethods = llmMethods;
        this.counter = verifiedCounter;
        this.verified = verified;
    }

    public static Builder builder(VectorStore vectorStore) {
        return new Builder(vectorStore);
    }

    public int getOrder() {
        return this.order;
    }

    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        String query = chatClientRequest.prompt().getUserMessage().getText();
        SearchRequest searchRequestToUse = SearchRequest.from(this.searchRequest).query(query).filterExpression(this.doGetFilterExpression(chatClientRequest.context())).build();
        List<Document> documents = this.vectorStore.similaritySearch(searchRequestToUse);
        verifiedDocuments = documents.stream()
                .filter(doc -> {
                    boolean ok = llmMethods.verifySource(doc.getText(), query);
                    counter.incrementAndGet();
                    return ok;
                })
                .toList();
        verified.set(true);
        Map<String, Object> context = new HashMap(chatClientRequest.context());
        context.put("qa_retrieved_documents", documents);
        String documentContext = documents == null ? "" : (String)documents.stream().map(Document::getText).collect(Collectors.joining(System.lineSeparator()));
        String augmentedUserText = this.promptTemplate.render(Map.of("query", query, "question_answer_context", documentContext));
        return chatClientRequest.mutate().prompt(chatClientRequest.prompt().augmentUserMessage(augmentedUserText)).context(context).build();
    }

    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        ChatResponse.Builder chatResponseBuilder;
        if (chatClientResponse.chatResponse() == null) {
            chatResponseBuilder = ChatResponse.builder();
        } else {
            chatResponseBuilder = ChatResponse.builder().from(chatClientResponse.chatResponse());
        }

        chatResponseBuilder.metadata("qa_retrieved_documents", chatClientResponse.context().get("qa_retrieved_documents"));
        return ChatClientResponse.builder().chatResponse(chatResponseBuilder.build()).context(chatClientResponse.context()).build();
    }

    @Nullable
    protected Filter.Expression doGetFilterExpression(Map<String, Object> context) {
        return context.containsKey("qa_filter_expression") && StringUtils.hasText(context.get("qa_filter_expression").toString()) ? (new FilterExpressionTextParser()).parse(context.get("qa_filter_expression").toString()) : this.searchRequest.getFilterExpression();
    }

    public List<Document> getVerifiedDocuments() {
        return this.verifiedDocuments;
    }

    public Scheduler getScheduler() {
        return this.scheduler;
    }

    public static final class Builder {
        private final VectorStore vectorStore;
        private SearchRequest searchRequest = SearchRequest.builder().build();
        private PromptTemplate promptTemplate;
        private Scheduler scheduler;
        private int order = 0;
        private LlmMethods llmMethods;
        private AtomicInteger counter;
        private AtomicBoolean verified;

        private Builder(VectorStore vectorStore) {
            Assert.notNull(vectorStore, "The vectorStore must not be null!");
            this.vectorStore = vectorStore;
        }

        public Builder promptTemplate(PromptTemplate promptTemplate) {
            Assert.notNull(promptTemplate, "promptTemplate cannot be null");
            this.promptTemplate = promptTemplate;
            return this;
        }

        public Builder searchRequest(SearchRequest searchRequest) {
            Assert.notNull(searchRequest, "The searchRequest must not be null!");
            this.searchRequest = searchRequest;
            return this;
        }

        public Builder protectFromBlocking(boolean protectFromBlocking) {
            this.scheduler = protectFromBlocking ? BaseAdvisor.DEFAULT_SCHEDULER : Schedulers.immediate();
            return this;
        }

        public Builder scheduler(Scheduler scheduler) {
            this.scheduler = scheduler;
            return this;
        }

        public Builder order(int order) {
            this.order = order;
            return this;
        }

        public Builder llmMethods(LlmMethods llmMethods) {
            this.llmMethods = llmMethods;
            return this;
        }

        public Builder counter(AtomicInteger counter) {
            this.counter = counter;
            return this;
        }

        public Builder verified(AtomicBoolean verified) {
            this.verified = verified;
            return this;
        }

        public VerifyingQuestionAnswerAdvisor build() {
            return new VerifyingQuestionAnswerAdvisor(this.vectorStore, this.searchRequest, this.promptTemplate, this.scheduler, this.order, this.llmMethods, this.counter, this.verified);
        }
    }
}
