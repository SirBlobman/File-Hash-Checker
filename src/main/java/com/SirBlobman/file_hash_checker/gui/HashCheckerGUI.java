package com.SirBlobman.file_hash_checker.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.SirBlobman.file_hash_checker.provider.ProviderCRC32;

public class HashCheckerGUI extends JFrame {
    public HashCheckerGUI() {
        super("File Hash Checker");
        setSize(706, 250);
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
        int labelX = 0;
        int labelWidth = 100;
        int labelHeight = 20;
        createLabel("MD5 Hash:", labelWidth, labelHeight, labelX, 2, textAlignmentRight);
        createLabel("SHA1 Hash:", labelWidth, labelHeight, labelX, 24, textAlignmentRight);
        createLabel("SHA-256 Hash:", labelWidth, labelHeight, labelX, 46, textAlignmentRight);
        createLabel("CRC32 Hash:", labelWidth, labelHeight, labelX, 68, textAlignmentRight);
        createLabel("File Name:", labelWidth, labelHeight, labelX, 90, textAlignmentRight);

        int textFieldX = 100;
        int textFieldWidth = 590;
        int textFieldHeight = 20;
        createTextField("MD5-Hash", textFieldWidth, textFieldHeight, textFieldX, 2, false);
        createTextField("SHA1-Hash", textFieldWidth, textFieldHeight, textFieldX, 24, false);
        createTextField("SHA-256-Hash", textFieldWidth, textFieldHeight, textFieldX, 46, false);
        createTextField("CRC32-Hash", textFieldWidth, textFieldHeight, textFieldX, 68, false);
        createTextField("File-Name", textFieldWidth, textFieldHeight, textFieldX, 90, false);

        Color lightBlue = new Color(0, 255, 255, 255);
        Color darkBlue = new Color(0, 0, 255, 255);
        int buttonY = 120;
        int buttonWidth = 175;
        int buttonHeight = 75;
        createButton("Choose File...", buttonWidth, buttonHeight, 0, buttonY, lightBlue, darkBlue, this::chooseFile);
        createButton("Calculate Hashes", buttonWidth, buttonHeight, 175, buttonY, lightBlue, darkBlue, this::calculateHashes);
        createButton("Reset", buttonWidth, buttonHeight, 350, buttonY, lightBlue, darkBlue, this::resetHashes);
        createButton("Save Hashes", buttonWidth, buttonHeight, 525, buttonY, lightBlue, darkBlue, this::saveHashes);
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

        Font font = new Font(Font.MONOSPACED, Font.PLAIN, 12);
        textField.setFont(font);

        Container container = getContentPane();
        container.add(textField);

        idToTextFieldMap.put(id, textField);
    }

    private void createLabel(String text, int width, int height, int x, int y, int textAlign) {
        JLabel label = new JLabel(text);
        label.setSize(width, height);
        label.setLocation(x, y);
        label.setHorizontalAlignment(textAlign);

        Font font = new Font(Font.MONOSPACED, Font.PLAIN, 12);
        label.setFont(font);

        Container container = getContentPane();
        container.add(label);
    }

    private void createButton(String text, int width, int height, int x, int y, Color background, Color foreground, ActionListener onClick) {
        JButton button = new JButton(text);
        button.setSize(width, height);
        button.setLocation(x, y);

        button.setBackground(background);
        button.setForeground(foreground);
        if(onClick != null) button.addActionListener(onClick);

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

    private void resetHashes(ActionEvent e) {
        for(JTextField textField : this.idToTextFieldMap.values()) textField.setText("");
    }

    private void saveHashes(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Text File", "txt");
        fileChooser.setFileFilter(filter);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        fileChooser.setSelectedFile(new File("hashes.txt"));

        int action = fileChooser.showSaveDialog(this);
        if(action == JFileChooser.APPROVE_OPTION) {
            try {
                File file = fileChooser.getSelectedFile();
                if(!file.getName().endsWith(".txt")) file = new File(file.getParentFile(), file.getName() + ".txt");
                file = file.getCanonicalFile();

                if(!file.exists()) {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                }

                long timeLong = System.currentTimeMillis();
                Date timeDate = new Date(timeLong);
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy hh:mm:ss.SSSa zzz");
                String timeDateFormat = dateFormat.format(timeDate);

                Path path = file.toPath();
                List<String> lineList = Arrays.asList(
                        "Time: " + timeLong + " " + timeDateFormat,
                        "File Name: " + getTextField("File-Name").getText(),
                        "MD5 Hash: " + getTextField("MD5-Hash").getText(),
                        "SHA-1 Hash: " + getTextField("SHA1-Hash").getText(),
                        "SHA-256 Hash: " + getTextField("SHA-256-Hash").getText(),
                        "CRC32 Value: " + getTextField("CRC32-Hash").getText()
                );
                Files.write(path, lineList, StandardCharsets.UTF_8);

                String message = "Successfully saved hash information to\n" + file;
                JOptionPane.showMessageDialog(this, message);
            } catch(IOException ex) {
                String message = "An error occurred while saving the hashes to that file." + ex.getMessage();
                JOptionPane.showMessageDialog(this, message, "I/O Error", JOptionPane.ERROR_MESSAGE);
            }
        }
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
        if(hashType.equals("CRC32")) return ProviderCRC32.calculateHash(this.file);

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
}