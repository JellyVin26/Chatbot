package org.example;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;

import java.nio.file.Path;
import java.nio.file.Paths;

public class LoaderExample {
    public static void main(String[] args) throws Exception {
        Path path = Paths.get("src/main/resources/docs/knowledge.txt");

        // this parser simply reads the file as UTF‑8 text
        TextDocumentParser parser = new TextDocumentParser();

        Document document = FileSystemDocumentLoader
                .loadDocument(path, parser);

        System.out.println(document.text());
    }
}