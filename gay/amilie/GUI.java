package gay.amilie;

import javax.swing.*;
import java.awt.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class GUI {
    public static void UserGUI() {
        try {
            if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                System.setProperty("apple.awt.application.appearance", "dark");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        JFrame gui = new JFrame("ChemFinder");
        gui.setSize(800, 600);
        gui.setTitle("ChemFinder v1.7b");
        gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gui.setLayout(new BorderLayout(10, 10));

        Color backgroundColor = new Color(43, 44, 55);

        gui.getContentPane().setBackground(backgroundColor);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(20, 20));
        mainPanel.setBackground(backgroundColor);
        gui.add(mainPanel, BorderLayout.CENTER);

        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(backgroundColor);
        JLabel title = new JLabel("ChemFinder", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 50));
        title.setForeground(new Color(241, 245, 248));
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        headerPanel.add(title);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        inputPanel.setBackground(backgroundColor);
        JTextField userInputField = new JTextField(20);
        userInputField.setPreferredSize(new Dimension(250, 30));
        userInputField.setFont(new Font("Arial", Font.PLAIN, 14));
        userInputField.setBackground(new Color(54, 59, 71));
        userInputField.setForeground(new Color(241, 245, 248));
        inputPanel.add(userInputField);

        JButton submitUserInput = new JButton("Search");
        submitUserInput.setFont(new Font("Arial", Font.BOLD, 14));
        submitUserInput.setBackground(new Color(125, 132, 156));
        submitUserInput.setForeground(new Color(241, 245, 248));
        submitUserInput.setFocusPainted(false);
        submitUserInput.setBorder(BorderFactory.createLineBorder(new Color(98, 122, 144), 2));
        submitUserInput.setPreferredSize(new Dimension(100, 40));
        inputPanel.add(submitUserInput);
        mainPanel.add(inputPanel, BorderLayout.CENTER);

        JPanel resultPanel = new JPanel();
        resultPanel.setLayout(new BorderLayout(10, 10));
        resultPanel.setBackground(backgroundColor);

        JLabel resultLabel = new JLabel("", SwingConstants.CENTER); // Center alignment
        resultLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        resultLabel.setForeground(new Color(241, 245, 248));
        resultLabel.setBackground(backgroundColor);
        resultLabel.setOpaque(true);

        resultLabel.setHorizontalAlignment(SwingConstants.CENTER);
        resultLabel.setVerticalAlignment(SwingConstants.TOP); // Aligns text to the top of the label (to keep all content in view)

        resultLabel.setPreferredSize(new Dimension(700, 200)); // Set preferred size for the result label
        resultLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Adds some padding around the text

        JScrollPane scrollPane = new JScrollPane(resultLabel);
        scrollPane.setPreferredSize(new Dimension(700, 200));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        resultPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(resultPanel, BorderLayout.SOUTH);

        submitUserInput.addActionListener(e -> {
            try {
                String userInput = userInputField.getText().trim();
                if (userInput.isEmpty()) {
                    JOptionPane.showMessageDialog(gui, "Input Error", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String compoundName = userInput.toLowerCase().replaceAll("[^a-zA-Z0-9]", "");
                String urlString = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/name/" + compoundName + "/JSON";
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);

                int responseCode = conn.getResponseCode();
                resultLabel.setText("");
                if (responseCode == 200) {
                    String jsonResponse = APIRequests.readResponse(conn);
                    String result = APIRequests.processResponse(jsonResponse);
                    resultLabel.setText("<html><pre>" + result + "</pre></html>");
                } else {
                    resultLabel.setText("Error HTTP code " + responseCode);
                }
                conn.disconnect();
            } catch (Exception ex) {
                resultLabel.setText("Error");
            }
        });

        gui.setVisible(true);
    }
}