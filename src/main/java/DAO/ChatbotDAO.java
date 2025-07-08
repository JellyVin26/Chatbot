package DAO;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ChatbotDAO {

    private static final Path KNOWLEDGE_PATH = Paths.get("src", "main", "resources", "docs", "knowledge.txt");
    private final TextDocumentParser parser = new TextDocumentParser();

    public Document loadKnowledgeBase() {
        return FileSystemDocumentLoader.loadDocument(KNOWLEDGE_PATH, parser);
    }
}

