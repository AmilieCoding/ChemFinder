import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javax.swing.*;
import java.io.*;

public class Main {
    private static Map<String, Map<String, String>> translations = new HashMap<>();
    private static String currentLanguage = "en";

    public static int startupErrCode() {
        return 1;
    }

    public static void main(String[] args) {
        try {
            initializeTranslations();
            UserGUI();
            //throw new RuntimeException("Intentional Exception - Debugging Purposes.");
        } catch (Exception e) {
            try {
                FileWriter fileWriter = new FileWriter("error_log.txt", true);
                PrintWriter printWriter = new PrintWriter(fileWriter);
                e.printStackTrace(printWriter);
                printWriter.println("[ERROR] Occurred at: " + System.currentTimeMillis());
                printWriter.close();
                System.out.println("[WARNING] Error logged to error_log.txt");
            } catch (IOException ex) {
                System.err.println("[ERROR] Failed to write to log file! " + ex.getMessage());
            }
            int errCode = startupErrCode();
            fatalError(errCode);
        }
    }

    public static void initializeTranslations() {
        Map<String, String> en = new HashMap<>();
        en.put("title", "ChemFinder - Enter what you need.");
        en.put("search", "Search");
        en.put("input_error", "Please enter a compound name.");
        en.put("error", "Error: Could not fetch data.");
        en.put("language", "Language: ");

        Map<String, String> nl = new HashMap<>();
        nl.put("title", "ChemFinder - Voer in wat u nodig heeft.");
        nl.put("search", "Zoeken");
        nl.put("input_error", "Voer een verbindingnaam in.");
        nl.put("error", "Fout: Kan gegevens niet ophalen.");
        nl.put("language", "Taal: ");

        translations.put("en", en);
        translations.put("nl", nl);
    }

    public static String translate(String key) {
        return translations.getOrDefault(currentLanguage, translations.get("en")).getOrDefault(key, key);
    }

    public static String readResponse(HttpURLConnection conn) throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }

    public static String processResponse(String response) {
        System.out.println("[DEBUG] Parsing and sorting JSON.");
        JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();

        if (jsonObject.has("PC_Compounds") && jsonObject.getAsJsonArray("PC_Compounds").size() > 0) {
            JsonObject firstCompound = jsonObject.getAsJsonArray("PC_Compounds").get(0).getAsJsonObject();

            if (firstCompound.has("id") && firstCompound.getAsJsonObject("id").has("id")) {
                JsonObject idObject = firstCompound.getAsJsonObject("id").getAsJsonObject("id");
                if (idObject.has("cid")) {
                    int cid = idObject.get("cid").getAsInt();
                    return "CID: " + cid;
                } else {
                    return "CID not found in 'id' object.";
                }
            } else {
                return "'id' or nested 'id' object not found.";
            }
        } else {
            return "No compounds found in the response.";
        }
    }

    public static void UserGUI() {
        JFrame gui = new JFrame("ChemFinder");
        gui.setSize(800, 600);
        String appName = "ChemFinder";
        double versionEdition = 1.7;
        String stagingEdition = "b";
        gui.setTitle(appName + " " + stagingEdition + versionEdition);
        gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gui.setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(20, 20));
        gui.add(mainPanel, BorderLayout.CENTER);

        JPanel headerPanel = new JPanel();
        JLabel title = new JLabel(translate("title"), SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.PLAIN, 18));
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        headerPanel.add(title);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JTextField userInputField = new JTextField(20);
        userInputField.setPreferredSize(new Dimension(200, 30));
        inputPanel.add(userInputField);

        JButton submitUserInput = new JButton(translate("search"));
        inputPanel.add(submitUserInput);
        mainPanel.add(inputPanel, BorderLayout.CENTER);

        JPanel languagePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JComboBox<String> languageSelector = new JComboBox<>(new String[]{"English", "Nederlands"});
        languagePanel.add(new JLabel(translate("language")));
        languagePanel.add(languageSelector);
        languagePanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        mainPanel.add(languagePanel, BorderLayout.SOUTH);

        JLabel resultLabel = new JLabel("", SwingConstants.CENTER);
        resultLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        gui.add(resultLabel, BorderLayout.SOUTH);

        languageSelector.addActionListener(e -> {
            String selectedLanguage = (String) languageSelector.getSelectedItem();
            if ("English".equals(selectedLanguage)) {
                currentLanguage = "en";
            } else if ("Nederlands".equals(selectedLanguage)) {
                currentLanguage = "nl";
            }
            title.setText(translate("title"));
            submitUserInput.setText(translate("search"));
            ((JLabel) languagePanel.getComponent(0)).setText(translate("language"));
        });

        submitUserInput.addActionListener(e -> {
            try {
                String userInput = userInputField.getText().trim();
                if (userInput.isEmpty()) {
                    JOptionPane.showMessageDialog(gui, translate("input_error"), "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String compoundName = userInput.toLowerCase().replaceAll("[^a-zA-Z0-9]", "");
                String urlString = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/name/" + compoundName + "/JSON";
                System.out.println("[DEBUG] Sending out request: " + urlString);

                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    String jsonResponse = readResponse(conn);
                    String result = processResponse(jsonResponse);
                    resultLabel.setText(result);
                    System.out.println("[DEBUG] Result: " + result);
                } else {
                    resultLabel.setText(translate("error") + " HTTP code " + responseCode);
                    System.err.println("[ERROR] HTTP ERROR CODE: " + responseCode);
                }
                conn.disconnect();
            } catch (Exception ex) {
                System.err.println("[ERROR] An error occurred: " + ex.getMessage());
                ex.printStackTrace();
                resultLabel.setText(translate("error"));
            }
        });

        gui.setVisible(true);
    }

    public static void fatalError(int errCode) {
        JFrame errGUI = new JFrame("ChemFinder - ERROR!");
        errGUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        errGUI.setSize(500,500);
        errGUI.setLayout(null);
        JLabel errorLabel = new JLabel("[FATAL ERROR] Program Crashed! Error Code: " + errCode, SwingConstants.CENTER);
        int frameWidth = 500;
        int frameHeight = 500;
        int labelWidth = 400;
        int labelHeight = 30;
        int x = (frameWidth - labelWidth) / 2;
        int y = (frameHeight - labelHeight) / 2;
        errorLabel.setBounds(x, y, labelWidth, labelHeight);
        errGUI.add(errorLabel);
        errGUI.setVisible(true);
    }
}
