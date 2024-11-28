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
            Scanner scanner = new Scanner(System.in);
            System.out.print("[NOTIFICATION] Enter your query: ");
            String userInput = scanner.nextLine();
            scanner.close();

            java.security.Security.setProperty("networkaddress.cache.ttl", "0");
            String compoundNamePreHyphen = userInput.toLowerCase();
            String compoundName = addHyphens(compoundNamePreHyphen);
            String urlString = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/name/" + compoundName + "/JSON";

            System.out.println("[DEBUG] Sending out request.");

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                System.out.println("[DEBUG] Code 200! JSON Found!");
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null){
                    response.append(inputLine);
                }
                in.close();

                System.out.println("[NOTIFICATION] Response from PubChem API:");
                System.out.println(response.toString());

                JsonObject jsonObject = JsonParser.parseString(response.toString()).getAsJsonObject();
                System.out.println("[DEBUG] Parsing and sorting JSON.");

                System.out.println("CID: " + jsonObject.getAsJsonArray("PC_Compounds")
                        .get(0).getAsJsonObject().get("id"));

                System.out.println("Label: " +
                        jsonObject.getAsJsonArray("PC_Compounds")
                                .get(0).getAsJsonObject().getAsJsonArray("props")
                                .get(0).getAsJsonObject().getAsJsonObject("urn")
                                .get("label").getAsString());

            } else {
                System.out.println("[ERROR] HTTP ERROR CODE: " + responseCode);
                System.out.println("[ERROR] Did you put the wrong chemical in?");
            }
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String addHyphens(String text) {
        return text.replaceAll("\\s+", "-");
    }
}
