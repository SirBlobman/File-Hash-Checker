package com.gmail.rivera.ingram.file_hash_checker;

import com.gmail.rivera.ingram.file_hash_checker.gui.HashCheckerGUI;

public final class FileHashChecker {
    public static void main(String... args) {
        HashCheckerGUI menu = new HashCheckerGUI();
        menu.initialize();
        menu.showGUI();
    }
}