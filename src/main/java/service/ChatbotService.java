package service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.example.Main;

public class ChatbotService {

    interface Assistant{
        String chat(String userMessage);
    }

    private ChatbotService.Assistant assistant;

    public ChatbotService(Document knowledgeBase, String apiKey) {
        // build model
        OpenAiChatModel model = OpenAiChatModel.builder()
                .apiKey("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")
                .modelName("gpt-4o") // or "gpt-4o-mini"
                .build();

        // ingest
        EmbeddingStore<TextSegment> store = new InMemoryEmbeddingStore<>();
        EmbeddingStoreIngestor.ingest(knowledgeBase, store);
        var retriever = EmbeddingStoreContentRetriever.from(store);

        // wire RAG assistant
        this.assistant = AiServices.builder(ChatbotService.Assistant.class)
                .chatModel(model)
                .contentRetriever(retriever)
                .build();


    }
//    /** sends user message, returns assistant’s reply */
    public String chat(String userMessage) {
        return assistant.chat(userMessage);
    }
}
