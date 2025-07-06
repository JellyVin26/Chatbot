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
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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
                .apiKey("OPENAI_API_KEY"));
                .modelName("gpt-4o-mini")  // or "gpt-4o" / "gpt-4o-mini"
                .build();

        // Ingest embeddings and create retriever
        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        EmbeddingStoreIngestor.ingest(document, embeddingStore);
        var retriever = EmbeddingStoreContentRetriever.from(embeddingStore);


        //Wire up the assistant using the RAG pipeline
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .contentRetriever(retriever)
                .build();

        // Container for messages
        VBox messageContainer = new VBox(8);
        messageContainer.setPadding(new Insets(10));

        // Scrollable view for chat
        ScrollPane scrollPane = new ScrollPane(messageContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        //Input field
        TextField inputField = new TextField();
        inputField.setPromptText("Ask a question...");
        HBox.setHgrow(inputField, Priority.ALWAYS);

        //Send button
        Button sendButton = new Button("Send");
        sendButton.setDefaultButton(true);
        sendButton.setOnAction(e -> {
            String question = inputField.getText().trim();
            if (!question.isEmpty()) {
                // Display user message
                TextArea userArea = new TextArea("User: " + question);
                userArea.setWrapText(true);
                userArea.setEditable(false);
                userArea.setMaxWidth(Double.MAX_VALUE);
                userArea.setPrefRowCount(2);
                userArea.setStyle("-fx-control-inner-background: #e3f2fd;" +
                                    "-fx-font-family: 'Times New Roman';"+
                                    "-fx-font-size: 13px");
                messageContainer.getChildren().add(userArea);
                scrollPane.layout();
                scrollPane.setVvalue(1.0);

                // Fetch assistant response in background
                new Thread(() -> {
                    String response = assistant.chat(question);
                    Platform.runLater(() -> {
                        TextArea assistantArea = new TextArea("Chatbot: " + response);
                        assistantArea.setWrapText(true);
                        assistantArea.setEditable(false);
                        assistantArea.setMaxWidth(Double.MAX_VALUE);
                        assistantArea.setStyle("-fx-control-inner-background: #f5f5f5;"+
                                                "-fx-font-family: 'Times New Roman';" +
                                                "-fx-font-size: 13px");
                        messageContainer.getChildren().add(assistantArea);
                        scrollPane.layout();
                        scrollPane.setVvalue(1.0);
                    });
                }).start();

                inputField.clear();
            }
        });

        // Input row container
        HBox inputRow = new HBox(8, inputField, sendButton);
        inputRow.setPadding(new Insets(10));
        inputRow.setAlignment(Pos.CENTER);

        // Main layout using BorderPane
        BorderPane root = new BorderPane();
        root.setCenter(scrollPane);
        root.setBottom(inputRow);

        // Scene and stage setup
        Scene scene = new Scene(root, 700, 500);
        primaryStage.setTitle("LangChain4j RAG Chatbot");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

