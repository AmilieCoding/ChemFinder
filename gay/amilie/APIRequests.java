package gay.amilie;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class APIRequests {

    public static String readResponse(HttpURLConnection conn) throws Exception {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            return response.toString();
        }
    }

    public static String processResponse(String response) {
        JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();

        if (jsonObject.has("PC_Compounds") && jsonObject.getAsJsonArray("PC_Compounds").size() > 0) {
            JsonObject firstCompound = jsonObject.getAsJsonArray("PC_Compounds").get(0).getAsJsonObject();

            if (firstCompound.has("id")) {
                JsonObject idObject = firstCompound.getAsJsonObject("id");
                if (idObject.has("id")) {
                    JsonObject innerIdObject = idObject.getAsJsonObject("id");
                    if (innerIdObject.has("cid")) {
                        int cid = innerIdObject.get("cid").getAsInt();

                        StringBuilder result = new StringBuilder("CID: " + cid + "\n");

                        String iupacName = extractValue(firstCompound, "IUPAC Name");
                        if (iupacName != null) result.append("IUPAC Name: " + iupacName + "\n");

                        String molecularFormula = extractValue(firstCompound, "Molecular Formula");
                        if (molecularFormula != null) result.append("Molecular Formula: " + molecularFormula + "\n");

                        String molecularWeight = extractValue(firstCompound, "Molecular Weight");
                        if (molecularWeight != null) result.append("Molecular Weight: " + molecularWeight + "\n");

                        String smiles = extractValue(firstCompound, "SMILES");
                        if (smiles != null) result.append("SMILES: " + smiles + "\n");

                        String inchi = extractValue(firstCompound, "InChI");
                        if (inchi != null) result.append("InChI: " + inchi + "\n");

                        return result.toString();
                    }
                }
            }
            return "CID not found in 'id' object.";
        }
        return "No compounds found in the response.";
    }

    private static String extractValue(JsonObject firstCompound, String label) {
        for (JsonElement prop : firstCompound.getAsJsonArray("props")) {
            JsonObject urn = prop.getAsJsonObject().getAsJsonObject("urn");
            if (urn.has("label") && urn.get("label").getAsString().equals(label)) {
                JsonElement value = prop.getAsJsonObject().get("value");
                if (value.isJsonObject()) {
                    JsonObject valueObject = value.getAsJsonObject();
                    if (valueObject.has("sval")) {
                        return valueObject.get("sval").getAsString();
                    } else if (valueObject.has("ival")) {
                        return String.valueOf(valueObject.get("ival").getAsInt());
                    } else if (valueObject.has("fval")) {
                        return String.valueOf(valueObject.get("fval").getAsFloat());
                    }
                } else if (value.isJsonPrimitive()) {
                    return value.getAsString();
                }
            }
        }
        return null;
    }
}