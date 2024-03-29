package xyz.sirblobman.application.hash.gui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import xyz.sirblobman.application.hash.provider.ProviderCRC32;

public class HashCheckerGUI extends JFrame {
    private final Map<String, JTextField> idToTextFieldMap;

    private File file;

    public HashCheckerGUI() {
        super("File Hash Checker");

        setResizable(false);
        setSize(706, 250);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        try {
            String lookAndFeel = UIManager.getSystemLookAndFeelClassName();
            UIManager.setLookAndFeel(lookAndFeel);
        } catch (Exception ex) {
            System.out.println("Failed to set application look and feel:");
            ex.printStackTrace();
        }

        this.idToTextFieldMap = new HashMap<>();
        this.file = null;
    }

    public void initialize() {
        Container container = getContentPane();
        container.setLayout(null);
        setupIcon();

        int labelX = 0;
        int labelWidth = 100;
        int labelHeight = 20;
        int textAlignmentRight = SwingConstants.RIGHT;

        createLabel("MD5 Hash:", labelWidth, labelHeight, labelX, 2, textAlignmentRight);
        createLabel("SHA1 Hash:", labelWidth, labelHeight, labelX, 24, textAlignmentRight);
        createLabel("SHA-256 Hash:", labelWidth, labelHeight, labelX, 46, textAlignmentRight);
        createLabel("CRC32 Hash:", labelWidth, labelHeight, labelX, 68, textAlignmentRight);
        createLabel("File Name:", labelWidth, labelHeight, labelX, 90, textAlignmentRight);

        int textFieldX = 100;
        int textFieldWidth = 590;
        int textFieldHeight = 20;

        createTextField("MD5-Hash", textFieldWidth, textFieldHeight, textFieldX, 2);
        createTextField("SHA1-Hash", textFieldWidth, textFieldHeight, textFieldX, 24);
        createTextField("SHA-256-Hash", textFieldWidth, textFieldHeight, textFieldX, 46);
        createTextField("CRC32-Hash", textFieldWidth, textFieldHeight, textFieldX, 68);
        createTextField("File-Name", textFieldWidth, textFieldHeight, textFieldX, 90);

        int buttonY = 120;
        int buttonWidth = 170;
        int buttonHeight = 75;
        Color lightBlue = new Color(0, 255, 255, 255);
        Color darkBlue = new Color(0, 0, 255, 255);

        int buttonX = 2;
        createButton("Choose File...", buttonWidth, buttonHeight, buttonX, buttonY,
                lightBlue, darkBlue, this::chooseFile);
        createButton("Calculate Hashes", buttonWidth, buttonHeight, buttonX + buttonWidth + 2, buttonY,
                lightBlue, darkBlue, this::calculateHashes);
        createButton("Reset", buttonWidth, buttonHeight, buttonX + 2 * buttonWidth + 4, buttonY,
                lightBlue, darkBlue, e -> resetHashes(""));
        createButton("Save Hashes", buttonWidth, buttonHeight, buttonX + 3 * buttonWidth + 6, buttonY,
                lightBlue, darkBlue, this::saveHashes);
    }

    public void showGUI() {
        setVisible(true);
    }

    private void setupIcon() {
        Class<?> thisClass = getClass();
        URL fileInJar = thisClass.getResource("/assets/icon.png");
        if (fileInJar == null) {
            return;
        }

        ImageIcon icon = new ImageIcon(fileInJar);
        Image iconImage = icon.getImage();
        setIconImage(iconImage);
    }

    private JTextField getTextField(String id) {
        if (id == null) {
            return null;
        }

        return this.idToTextFieldMap.get(id);
    }

    private void createTextField(String id, int width, int height, int x, int y) {
        JTextField textField = new JTextField();
        textField.setSize(width, height);
        textField.setLocation(x, y);
        textField.setEditable(false);

        Font font = new Font(Font.MONOSPACED, Font.PLAIN, 12);
        textField.setFont(font);

        Container container = getContentPane();
        container.add(textField);

        this.idToTextFieldMap.put(id, textField);
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

    private void createButton(String text, int width, int height, int x, int y, Color background, Color foreground,
                              ActionListener clickAction) {
        JButton button = new JButton(text);
        button.setSize(width, height);
        button.setLocation(x, y);

        button.setBackground(background);
        button.setForeground(foreground);
        if (clickAction != null) {
            button.addActionListener(clickAction);
        }

        Container container = getContentPane();
        container.add(button);
    }

    private void chooseFile(ActionEvent e) {
        this.file = null;

        JTextField textFieldFileName = getTextField("File-Name");
        textFieldFileName.setText("");

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setAcceptAllFileFilterUsed(true);
        fileChooser.setMultiSelectionEnabled(false);

        int action = fileChooser.showOpenDialog(this);
        if (action == JFileChooser.APPROVE_OPTION) {
            this.file = fileChooser.getSelectedFile();
            if (this.file != null) {
                try {
                    String fileName = this.file.getCanonicalPath();
                    textFieldFileName.setText(fileName);
                } catch (IOException ex) {
                    String message = "An error occurred, please choose a different file!";
                    JOptionPane.showMessageDialog(this, message, "I/O Error",
                            JOptionPane.ERROR_MESSAGE);
                    textFieldFileName.setText("");
                    this.file = null;
                }
            }
        }
    }

    private void calculateHashes(ActionEvent e) {
        if (this.file == null || !this.file.exists()) {
            String message = "You did not select a file.";
            JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!this.file.isFile()) {
            String message = "You must select a file, not a folder.";
            JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        resetHashes("Calculating...");
        updateHash("MD5");
        updateHash("SHA1");
        updateHash("SHA-256");
        updateHash("CRC32");
    }

    private void resetHashes(String text) {
        Set<Entry<String, JTextField>> entrySet = this.idToTextFieldMap.entrySet();
        for (Entry<String, JTextField> entry : entrySet) {
            String id = entry.getKey();
            if (id.equals("File-Name")) {
                continue;
            }

            JTextField textField = entry.getValue();
            textField.setText(text);
        }
    }

    private void saveHashes(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Text File", "txt");
        fileChooser.setFileFilter(filter);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);

        File defaultFile = new File("hashes.txt");
        fileChooser.setSelectedFile(defaultFile);

        int action = fileChooser.showSaveDialog(this);
        if (action == JFileChooser.APPROVE_OPTION) {
            try {
                File selectedFile = fileChooser.getSelectedFile();
                if (!selectedFile.exists() && !selectedFile.createNewFile()) {
                    throw new IOException("Failed to create the file!");
                }

                long systemMillis = System.currentTimeMillis();
                Date systemDate = new Date(systemMillis);
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS zzz");
                String timeDateFormat = dateFormat.format(systemDate);

                Path path = selectedFile.toPath();
                List<String> lineList = Arrays.asList(
                        "Time: " + systemMillis + " (" + timeDateFormat + ")",
                        "File Path: " + getTextField("File-Name").getText(),
                        "MD5 Hash: " + getTextField("MD5-Hash").getText(),
                        "SHA-1 Hash: " + getTextField("SHA1-Hash").getText(),
                        "SHA-256 Hash: " + getTextField("SHA-256-Hash").getText(),
                        "CRC32 Value: " + getTextField("CRC32-Hash").getText()
                );

                Files.write(path, lineList, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);
                String message = "Successfully saved hash information to\n" + selectedFile;
                JOptionPane.showMessageDialog(this, message);
            } catch (IOException ex) {
                String message = "An error occurred while saving the hashes to that file." + ex.getMessage();
                JOptionPane.showMessageDialog(this, message, "I/O Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateHash(JTextField textField, String hash, Throwable error) {
        if (error != null) {
            String message = error.getLocalizedMessage();
            textField.setText("Error: " + message);
            return;
        }

        if (hash == null) {
            textField.setText("Error: null");
            return;
        }

        textField.setText(hash);
    }

    private void updateHash(String hashType) {
        String textFieldId = (hashType + "-Hash");
        JTextField textField = getTextField(textFieldId);
        Supplier<String> supplier = () -> calculateHash(hashType);
        BiConsumer<String, Throwable> task = (hash, error) -> updateHash(textField, hash, error);
        CompletableFuture.supplyAsync(supplier).whenCompleteAsync(task);
    }

    private String calculateHash(String hashType) {
        if (this.file == null || !this.file.exists()) {
            return "I/O Error: No File Selected";
        }

        if (hashType.equals("CRC32")) {
            return ProviderCRC32.calculateHash(this.file);
        }

        try {
            return calculateDigestHash(hashType);
        } catch (IOException ex) {
            return "I/O Error: " + ex.getLocalizedMessage();
        } catch (NoSuchAlgorithmException ex) {
            return "Algorithm Error: Could not get calculator for hash type '" + hashType + "'.";
        }
    }

    private String calculateDigestHash(String hashType) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(hashType);
        digest.reset();

        FileInputStream stream = new FileInputStream(this.file);

        int byteCount;
        byte[] byteArray = new byte[1024];
        while ((byteCount = stream.read(byteArray)) != -1) {
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
    }
}
