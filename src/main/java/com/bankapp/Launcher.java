package com.bankapp;

/**
 * IDE / fat-JAR entry point.
 *
 * WHY THIS EXISTS:
 * When an IDE (VS Code, IntelliJ, Eclipse) runs a class that extends
 * javafx.application.Application directly, the JVM checks for JavaFX
 * modules on the module-path BEFORE main() runs and throws:
 *   "Error: JavaFX runtime components are missing"
 *
 * A plain class with no JavaFX parent bypasses that check entirely.
 * JavaFX loads fine once Application.launch() is called from Main.
 *
 * HOW TO RUN:
 *   From IDE  → run this file (com.bankapp.Launcher)
 *   From Maven → ./tools/apache-maven-3.9.9/bin/mvn javafx:run
 *   Fat JAR   → java -jar target/neobank.jar
 */
public class Launcher {
    public static void main(String[] args) {
        Main.main(args);
    }
}

