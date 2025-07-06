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
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

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

        // Ingest embeddings and create retriever
        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        EmbeddingStoreIngestor.ingest(document, embeddingStore);
        var retriever = EmbeddingStoreContentRetriever.from(embeddingStore);


        //Wire up the assistant using the RAG pipeline
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .contentRetriever(retriever)
                .build();

        // Header bar
        HBox header = new HBox();
        header.setStyle("-fx-background-color: #6200ea;");
        header.setPadding(new Insets(8));
        Label title = new Label("Chatbot");
        title.setTextFill(Color.WHITE);
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        // Window controls placeholder
        HBox controls = new HBox(6);
        header.getChildren().addAll(title, spacer, controls);


        // Chat container
        VBox messageContainer = new VBox(6);
        messageContainer.setPadding(new Insets(10));
        ScrollPane scrollPane = new ScrollPane(messageContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background: #d3d3d3;");

        // Input area
        TextField inputField = new TextField();
        inputField.setPromptText("Type anything here");
        inputField.setStyle("-fx-background-radius: 20; -fx-border-radius: 20; -fx-background-color: #f2f2f2;");
        HBox.setHgrow(inputField, Priority.ALWAYS);

        //Send button
        Button sendButton = new Button("ENTER");
        sendButton.setStyle("-fx-background-color: #6200ea; -fx-text-fill: white; -fx-background-radius: 20;");
        sendButton.setDefaultButton(true);
        sendButton.setOnAction(e -> {
            String text = inputField.getText().trim();
            if (!text.isEmpty()) {
                addBubble(messageContainer, scrollPane, text, true);
                inputField.clear();
                new Thread(() -> {
                    String resp = assistant.chat(text);
                    Platform.runLater(() -> addBubble(messageContainer, scrollPane, resp, false));
                }).start();
            }
        });

        // Input bar
        HBox inputBar = new HBox(8, inputField, sendButton);
        inputBar.setPadding(new Insets(10));
        inputBar.setStyle("-fx-background-color: #eeeeee; -fx-background-radius: 20;");
        inputBar.setAlignment(Pos.CENTER);


        // Main layout
        BorderPane root = new BorderPane();
        root.setTop(header);
        root.setCenter(scrollPane);
        root.setBottom(inputBar);
        Scene scene = new Scene(root, 700, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("LangChain4j RAG Chatbot");
        primaryStage.show();
    }

    private void addBubble(VBox container, ScrollPane scroll, String text, boolean isUser) {
        Label bubble = new Label(text);
        bubble.setWrapText(true);
        bubble.setMaxWidth(700);
        bubble.setPadding(new Insets(8));

        Circle avatar = new Circle(10, Color.web("#6200ea"));
        HBox box = new HBox(6);
        if (isUser) {
            bubble.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-background-radius: 16 0 16 16;");
            box.setAlignment(Pos.CENTER_RIGHT);
            box.getChildren().addAll(bubble, avatar);
        } else {
            bubble.setStyle("-fx-background-color: white;" + "-fx-text-fill: black;" + "-fx-background-radius: 0 16 16 16;");
            box.setAlignment(Pos.CENTER_LEFT);
            box.getChildren().addAll(avatar, bubble);
        }
        container.getChildren().add(box);
        scroll.layout(); scroll.setVvalue(1.0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

