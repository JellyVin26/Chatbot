package org.example;

import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.data.document.loader.*   ;
import dev.langchain4j.data.document.Document;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;


public class Main extends Application {

    //public static void main(String[] args) {

    interface Assistant{
        String chat(String userMessage);
    }

    private Assistant assistant;


    public void start(Stage primaryStage) throws Exception {
        // Load the text file from the filesystem
        Path docPath = Paths.get("src", "main", "resources", "docs", "knowledge.txt");
        TextDocumentParser parser = new TextDocumentParser();
        Document document = FileSystemDocumentLoader.loadDocument(docPath, parser);
        // Build the LangChain4j OpenAI chat model
        OpenAiChatModel model = OpenAiChatModel.builder()
                .apiKey("OPENAI_API_KEY"))
                .modelName("gpt-4o-mini")  // or "gpt-4o" / "gpt-4o-mini"
                .build();

        //Load your knowledge.txt base document
        InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        EmbeddingStoreIngestor.ingest(document, embeddingStore);

        //Create a retriever from the store
        var retriever = EmbeddingStoreContentRetriever.from(embeddingStore);

        //Wire up the assistant using the RAG pipeline
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .contentRetriever(retriever)
                .build();

        //Build a minimal JavaFX chat UI

        TextArea conversationArea = new TextArea();
        conversationArea.setEditable(false);
        conversationArea.setWrapText(true);

        TextField inputField = new TextField();
        inputField.setPromptText("Ask a question...");
        inputField.setPrefHeight(24);

        Button sendButton = new Button("Send");
        sendButton.setPrefHeight(24);
        sendButton.setOnAction(e -> {
            String question = inputField.getText().trim();
            if (!question.isEmpty()) {
                conversationArea.appendText("User: " + question + "\n");
                String response = assistant.chat(question);
                conversationArea.appendText("Assistant: " + response + "\n\n");
                inputField.clear();
            }
        });

        HBox inputRow = new HBox(6, inputField, sendButton);
        inputRow.setPadding(new Insets(6));

        VBox root = new VBox(10, conversationArea, inputField, sendButton);
        root.setPadding(new Insets(10));
        Scene scene = new Scene(root, 600, 400);

        primaryStage.setTitle("LangChain4j RAG Chatbot");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

