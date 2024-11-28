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
            String userInput = getInput();
            java.security.Security.setProperty("networkaddress.cache.ttl", "0");
            String compoundName = addHyphens(userInput.toLowerCase());
            String urlString = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/name/" + compoundName + "/JSON";
            handleRequest(urlString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void handleRequest(String urlString) throws Exception {
        System.out.println("[DEBUG] Sending out request.");
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            System.out.println("[DEBUG] Code 200! JSON Found!");
            String response = readResponse(conn);
            processResponse(response);
        } else {
            System.out.println("[ERROR] HTTP ERROR CODE: " + responseCode);
            System.out.println("[ERROR] Did you put the wrong chemical in?");
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
        JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
        System.out.println("[DEBUG] Parsing and sorting JSON.");
        System.out.println("CID: " + jsonObject.getAsJsonArray("PC_Compounds")
                .get(0).getAsJsonObject().get("id"));
        System.out.println("Label: " +
                jsonObject.getAsJsonArray("PC_Compounds")
                        .get(0).getAsJsonObject().getAsJsonArray("props")
                        .get(0).getAsJsonObject().getAsJsonObject("urn")
                        .get("label").getAsString());
    }

    public static String addHyphens(String text) {
        return text.replaceAll("\\s+", "-");
    }

    static String getInput() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("[NOTIFICATION] Enter your query: ");
        String userInput = scanner.nextLine();
        scanner.close();
        return userInput;
    }
}
