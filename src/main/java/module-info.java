module org.example {
    requires javafx.controls;               // standard javafx modules
    requires javafx.fxml;
    requires langchain4j.open.ai;           // must add langchain4j references
    requires langchain4j.core;
    requires langchain4j;
    requires org.apache.logging.log4j;      // must add log4j references
    requires org.slf4j;                     // must add slf4j
    requires java.net.http;                 // needed if HttpTimeoutException occurs
    requires com.fasterxml.jackson.core;    // needed if assistant is null
    exports org.example;
    exports controller;

    opens controller to javafx.fxml;
}
