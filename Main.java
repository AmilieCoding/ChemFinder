import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            UserGUI();
            System.out.println("[DEBUG] Successfully started program!");
        } catch (Exception e) {
            System.err.println("[ERROR] An exception occurred: " + e.getMessage());
            e.printStackTrace();
        }
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

        // Parse the response into a JsonObject
        JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();

        // Check if the PC_Compounds array exists and is not empty
        if (jsonObject.has("PC_Compounds") && jsonObject.getAsJsonArray("PC_Compounds").size() > 0) {
            // Extract the first compound from the array
            JsonObject firstCompound = jsonObject.getAsJsonArray("PC_Compounds").get(0).getAsJsonObject();

            // Access the nested "id" -> "id" -> "cid" to get the CID
            if (firstCompound.has("id") && firstCompound.getAsJsonObject("id").has("id")) {
                JsonObject idObject = firstCompound.getAsJsonObject("id").getAsJsonObject("id");
                if (idObject.has("cid")) {
                    int cid = idObject.get("cid").getAsInt();  // Get the CID value
                    return "CID: " + cid;  // Return the CID as a string
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
        JFrame gui = new JFrame();
        gui.setSize(600, 500);
        String appName = "ChemFinder";
        double versionEdition = 1.5;
        String stagingEdition = "b";
        gui.setTitle(appName + " " + stagingEdition + versionEdition);
        gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gui.setLayout(new BorderLayout());

        JLabel title = new JLabel("ChemFinder - Enter what you need.", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.PLAIN, 18));
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        gui.add(title, BorderLayout.NORTH);

        // Panel for user input and search button
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        JTextField userInputField = new JTextField(20);
        userInputField.setPreferredSize(new Dimension(200, 30));
        inputPanel.add(userInputField);

        JButton submitUserInput = new JButton("Search");
        inputPanel.add(submitUserInput);

        gui.add(inputPanel, BorderLayout.CENTER); // Add the panel to the center

        // Persistent result label
        JLabel resultLabel = new JLabel("", SwingConstants.CENTER);
        resultLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        gui.add(resultLabel, BorderLayout.SOUTH);

        submitUserInput.addActionListener(e -> {
            try {
                String userInput = userInputField.getText().trim();
                if (userInput.isEmpty()) {
                    JOptionPane.showMessageDialog(gui, "Please enter a compound name.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Sanitize input
                String compoundName = userInput.toLowerCase().replaceAll("[^a-zA-Z0-9]", "");
                String urlString = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/name/" + compoundName + "/JSON";
                System.out.println("[DEBUG] Sending out request: " + urlString);

                // HTTP Request
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    String jsonResponse = readResponse(conn); // Fetch response
                    String result = processResponse(jsonResponse); // Process JSON
                    resultLabel.setText(result); // Update result in GUI
                    System.out.println("[DEBUG] Result: " + result);
                } else {
                    resultLabel.setText("Error: Received HTTP code " + responseCode);
                    System.err.println("[ERROR] HTTP ERROR CODE: " + responseCode);
                }
                conn.disconnect();
            } catch (Exception ex) {
                System.err.println("[ERROR] An error occurred: " + ex.getMessage());
                ex.printStackTrace();
                resultLabel.setText("Error: Could not fetch data.");
            }
        });

        gui.setVisible(true); // Show the GUI
    }
}
