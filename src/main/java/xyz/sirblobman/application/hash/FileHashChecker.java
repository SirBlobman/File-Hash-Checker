package xyz.sirblobman.application.hash;

import xyz.sirblobman.application.hash.gui.HashCheckerGUI;

public final class FileHashChecker {
    public static void main(String... args) {
        HashCheckerGUI menu = new HashCheckerGUI();
        menu.initialize();
        menu.showGUI();
    }
}
