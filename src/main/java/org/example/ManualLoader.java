package org.example;
import dev.langchain4j.data.document.Document;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;

public class ManualLoader {
    public static void main(String[] args) throws Exception {
        Path path = Path.of("src/main/resources/docs/knowledge.txt");
        String text = Files.readString(path, StandardCharsets.UTF_8).trim();

        if (text.isEmpty()) {
            throw new IllegalArgumentException("knowledge.txt must contain some text");
        }

        // Create a Document directly from the raw String
        Document document = Document.from(text);

        System.out.println(document.text());
    }
}
