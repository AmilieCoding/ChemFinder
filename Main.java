import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try {
            System.out.println("[DEBUG] Successfully started program!");
            String userInput = getInput();
            java.security.Security.setProperty("networkaddress.cache.ttl", "0");
            String compoundNameHalfSanitized = addHyphens(userInput.toLowerCase());
            System.out.println("[DEBUG] Successfully added hyphens and made lower case.");
            String compoundName = removeSpecialCharacters(compoundNameHalfSanitized);
            System.out.println("[DEBUG] Removed special characters.");
            String urlString = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/name/" + compoundName + "/JSON";
            handleRequest(urlString);
        } catch (Exception e) {
            System.err.println("[ERROR] An exception occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void handleRequest(String urlString) throws Exception {
        System.out.println("[DEBUG] Sending out request.");
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
        conn.setRequestMethod("GET");
        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            System.out.println("[DEBUG] HTTP CODE 200! JSON Found! Continuing with analysis!");
            String response = readResponse(conn);
            processResponse(response);
        } else if (responseCode == 429 || responseCode == 503) {
            System.out.println("[ERROR] HTTP ERROR CODE: " + responseCode);
            System.out.println("[ERROR] You have been rate-limited!");
            int maxRetries = 3;
            for (int retryCount = 0; retryCount < maxRetries; retryCount++) {
                System.out.println("[NOTIFICATION] Retrying in 15 seconds...");
                try {
                    Thread.sleep(15000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("[ERROR] Retry interrupted.");
                    break;
                }
                System.out.println("[NOTIFICATION] Attempting retry " + (retryCount + 1) + " of " + maxRetries + "...");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    System.out.println("[NOTIFICATION] Request succeeded on retry " + (retryCount + 1) + ".");
                    String response = readResponse(conn);
                    processResponse(response);
                    break;
                } else if (retryCount == maxRetries - 1) {
                    System.err.println("[ERROR] Max retries reached. Exiting.");
                }
            }
        } else {
            System.out.println("[ERROR] HTTP ERROR CODE: " + responseCode);
            System.out.println("[ERROR] Are you sure you spelt it correctly?");
        }
        conn.disconnect();
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

    public static void processResponse(String response) {
        System.out.println("[DEBUG] Parsing and sorting JSON.");
        JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
        System.out.println("[NOTIFICATION] CID: " + jsonObject.getAsJsonArray("PC_Compounds")
                .get(0).getAsJsonObject().get("id"));
        System.out.println("[NOTIFICATION] Label: " + jsonObject.getAsJsonArray("PC_Compounds")
                .get(0).getAsJsonObject().getAsJsonArray("props")
                .get(0).getAsJsonObject().getAsJsonObject("urn")
                .get("label").getAsString());
        System.out.println("[DEBUG] JSON Parsed and Sorted!");
    }

    public static String addHyphens(String text) {
        return text.replaceAll("\\s+", "-");
    }

    public static String removeSpecialCharacters(String input) {
        return input.replaceAll("[^a-zA-Z0-9\\s]", "");
    }

    static String getInput() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("[NOTIFICATION] Enter your query: ");
        String userInput = scanner.nextLine();
        scanner.close();
        return userInput;
    }
}
