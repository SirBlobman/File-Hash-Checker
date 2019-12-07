package com.SirBlobman.file_hash_checker.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import javax.swing.*;

import com.SirBlobman.file_hash_checker.provider.ProviderCRC32;

public class HashCheckerGUI extends JFrame {
    public HashCheckerGUI() {
        super("File Hash Checker");
        setSize(700, 250);
        setResizable(false);
        setLocation(0, 0);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        try {
            String systemLF = UIManager.getSystemLookAndFeelClassName();
            UIManager.setLookAndFeel(systemLF);
        } catch(Exception ignored) {}
    }

    public void initialize() {
        Container container = getContentPane();
        container.setLayout(null);

        setupIcon();

        int textAlignmentRight = SwingConstants.RIGHT;
        int labelX = 2;
        createLabel("MD5 Hash:", 90, 20, labelX, 2, textAlignmentRight);
        createLabel("SHA1 Hash:", 90, 20, labelX, 24, textAlignmentRight);
        createLabel("SHA-256 Hash:", 90, 20, labelX, 46, textAlignmentRight);
        createLabel("CRC32 Hash:", 90, 20, labelX, 68, textAlignmentRight);
        createLabel("File Name:", 90, 20, labelX, 90, textAlignmentRight);

        int textFieldX = 94;
        createTextField("MD5-Hash", 595, 20, textFieldX, 2, false);
        createTextField("SHA1-Hash", 595, 20, textFieldX, 24, false);
        createTextField("SHA-256-Hash", 595, 20, textFieldX, 46, false);
        createTextField("CRC32-Hash", 595, 20, textFieldX, 68, false);
        createTextField("File-Name", 595, 20, textFieldX, 90, false);

        int buttonY = 120;
        Color lightBlue = new Color(0, 255, 255, 255);
        Color darkBlue = new Color(0, 0, 255, 255);
        Color lightGreen = new Color(0, 255, 0, 255);
        Color darkGreen = new Color(0, 128, 0, 255);
        createButton("Choose File...", 150, 75, 100, buttonY, lightBlue, darkBlue, this::chooseFile);
        createButton("Calculate Hashes", 150, 75, 375, buttonY, lightGreen, darkGreen, this::calculateHashes);
    }

    public void showGUI() {
        setVisible(true);
    }

    private void setupIcon() {
        Class<?> clazz = getClass();
        URL fileInJar = clazz.getResource("/assets/icon.png");
        if(fileInJar == null) return;

        ImageIcon icon = new ImageIcon(fileInJar);
        setIconImage(icon.getImage());
    }

    private final Map<String, JTextField> idToTextFieldMap = new HashMap<>();
    private JTextField getTextField(String id) {
        if(id == null) return null;
        return idToTextFieldMap.getOrDefault(id, null);
    }

    private void createTextField(String id, int width, int height, int x, int y, boolean canEdit) {
        JTextField textField = new JTextField();
        textField.setSize(width, height);
        textField.setLocation(x, y);
        textField.setEditable(canEdit);

        Container container = getContentPane();
        container.add(textField);

        idToTextFieldMap.put(id, textField);
    }

    private void createLabel(String text, int width, int height, int x, int y, int textAlign) {
        JLabel label = new JLabel(text);
        label.setSize(width, height);
        label.setLocation(x, y);
        label.setHorizontalAlignment(textAlign);

        Container container = getContentPane();
        container.add(label);
    }

    private void createButton(String text, int width, int height, int x, int y, Color background, Color foreground, ActionListener onClick) {
        JButton button = new JButton(text);
        button.setSize(width, height);
        button.setLocation(x, y);

        button.setBackground(background);
        button.setForeground(foreground);
        button.addActionListener(onClick);

        Container container = getContentPane();
        container.add(button);
    }

    private File file = null;
    private void chooseFile(ActionEvent e) {
        this.file = null;

        JTextField textFieldFileName = getTextField("File-Name");
        textFieldFileName.setText("");

        JFileChooser fileChooser = new JFileChooser();
        int action = fileChooser.showOpenDialog(this);
        if(action == JFileChooser.APPROVE_OPTION) {
            this.file = fileChooser.getSelectedFile();
            if(this.file != null) {
                try {
                    String fileName = this.file.getCanonicalPath();
                    textFieldFileName.setText(fileName);
                } catch(IOException ex) {
                    String message = "An error occurred, please choose a different file!";
                    JOptionPane.showMessageDialog(this, message, "I/O Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void calculateHashes(ActionEvent e) {
        if(this.file == null || !this.file.exists()) {
            String message = "You must choose a file before calculating hashes.";
            JOptionPane.showMessageDialog(this, message, "I/O Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        calculateHashMD5();
        calculateHashSHA1();
        calculateHashSHA256();
        calculateHashCRC32();
    }

    private void calculateHashMD5() {
        JTextField textField = getTextField("MD5-Hash");
        textField.setText("Calculating...");

        CompletableFuture.supplyAsync(() -> calculateHash("MD5")).whenComplete((string, error) -> {
            if(error != null) {
                String message = error.getLocalizedMessage();
                textField.setText("Error: " + message);
            }

            textField.setText(string);
        });
    }

    private void calculateHashSHA1() {
        JTextField textField = getTextField("SHA1-Hash");
        textField.setText("Calculating...");

        CompletableFuture.supplyAsync(() -> calculateHash("SHA1")).whenComplete((string, error) -> {
            if(error != null) {
                String message = error.getLocalizedMessage();
                textField.setText("Error: " + message);
            }

            textField.setText(string);
        });
    }

    private void calculateHashSHA256() {
        JTextField textField = getTextField("SHA-256-Hash");
        textField.setText("Calculating...");

        CompletableFuture.supplyAsync(() -> calculateHash("SHA-256")).whenComplete((string, error) -> {
            if(error != null) {
                String message = error.getLocalizedMessage();
                textField.setText("Error: " + message);
            }

            textField.setText(string);
        });
    }

    private void calculateHashCRC32() {
        JTextField textField = getTextField("CRC32-Hash");
        textField.setText("Calculating...");

        CompletableFuture.supplyAsync(() -> calculateHash("CRC32")).whenComplete((string, error) -> {
            if(error != null) {
                String message = error.getLocalizedMessage();
                textField.setText("Error: " + message);
            }

            textField.setText(string);
        });
    }

    private String calculateHash(String hashType) {
        if(this.file == null || !this.file.exists()) return "I/O Error: No File Selected";
        if(hashType.equals("CRC32")) return calculateCRC32();

        try {
            MessageDigest digest = MessageDigest.getInstance(hashType);
            digest.reset();

            FileInputStream stream = new FileInputStream(file);

            byte[] byteArray = new byte[1024];
            int byteCount;
            while((byteCount = stream.read(byteArray)) != -1) {
                digest.update(byteArray, 0, byteCount);
            }

            stream.close();

            byte[] bytes = digest.digest();
            StringBuilder builder = new StringBuilder();
            for (byte value : bytes) {
                int andFF = (value & 0xFF);
                int plus100 = (andFF + 0x100);
                String intString = Integer.toString(plus100, 16);
                String substring = intString.substring(1);
                builder.append(substring);
            }

            return builder.toString();
        } catch(IOException ex) {
            return "I/O Error: " + ex.getLocalizedMessage();
        } catch(NoSuchAlgorithmException ex) {
            return "I/O Error: Could not get calculator for hash type '" + hashType + "'.";
        }
    }

    private String calculateCRC32() {
        if(this.file == null || !this.file.exists()) return "I/O Error: No File Selected";

        ProviderCRC32 algorithm = new ProviderCRC32();
        algorithm.reset();

        try {
            FileInputStream stream = new FileInputStream(file);
            BufferedInputStream buffer = new BufferedInputStream(stream);
            DigestInputStream digest = new DigestInputStream(buffer, algorithm);
            while(digest.read() != -1) doNothing();

            StringBuilder hexString = new StringBuilder();
            byte[] bytes = algorithm.digest();
            for (byte value : bytes) {
                String hexDigit = hexDigitCRC32(value);
                hexString.append(hexDigit);
                hexString.append(" ");
            }

            return hexString.toString();
        } catch(IOException ex) {
            return "I/O Error: " + ex.getLocalizedMessage();
        }
    }

    private String hexDigitCRC32(byte value) {
        StringBuilder buffer = new StringBuilder();
        char character;
        character = (char) ((value >> 4) & 0xF);
        if(character > 9) {
            character = (char) ((character - 10) + 'a');
        } else {
            character = (char) (character + '0');
        }
        buffer.append(character);

        character = (char) (value & 0xF);
        if(character > 9) {
            character = (char) ((character - 10) + 'a');
        } else {
            character = (char) (character + '0');
        }
        buffer.append(character);

        return buffer.toString();
    }

    private void doNothing() {
        // Do Nothing
    }
}