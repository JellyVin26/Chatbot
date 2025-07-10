package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import DAO.ChatbotDAO;
import service.ChatbotService;

public class ChatbotController {

    @FXML private VBox messageContainer;
    @FXML private ScrollPane scrollPane;
    @FXML private TextField inputField;
    @FXML private Button sendButton;

    private ChatbotService chatbot;

    @FXML
    public void initialize() {
        // build DAO → load doc → create service
        var dao = new ChatbotDAO();
        var doc = dao.loadKnowledgeBase();
        chatbot = new ChatbotService(doc, System.getenv("OPENAI_API_KEY"));

        sendButton.setOnAction(e -> onSend());
    }

    private void onSend() {
        var text = inputField.getText().trim();
        if (text.isEmpty()) return;

        addBubble(text, true);
        inputField.clear();

        new Thread(() -> {
            var resp = chatbot.chat(text);
            Platform.runLater(() -> addBubble(resp, false));
        }).start();
    }

    private void addBubble(String text, boolean isUser) {
        Label bubble = new Label(text);
        bubble.setWrapText(true);
        bubble.setMaxWidth(500);
        bubble.setStyle("-fx-padding:8; -fx-background-color:white; -fx-background-radius:16;");
        if (isUser) bubble.setStyle(bubble.getStyle() + "-fx-background-radius:16 0 16 16;");
        else bubble.setStyle(bubble.getStyle() + "-fx-background-radius:0 16 16 16;");

        Circle avatar = new Circle(10, Color.web("#6200ea"));
        HBox box = new HBox(6, isUser ? bubble : avatar, isUser ? avatar : bubble);
        box.setPrefWidth(Double.MAX_VALUE);
        box.setStyle(isUser ? "-fx-alignment:center-right;" : "-fx-alignment:center-left;");

        messageContainer.getChildren().add(box);
        scrollPane.layout(); scrollPane.setVvalue(1.0);
    }
}
