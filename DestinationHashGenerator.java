import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

public class DestinationHashGenerator {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java -jar DestinationHashGenerator.jar <PRN Number> <path to JSON file>");
            return;
        }

        String prnNumber = args[0].toLowerCase().trim();
        String jsonFilePath = args[1];

        String destinationValue = null;
        try {
            destinationValue = findDestinationValue(jsonFilePath);
        } catch (IOException e) {
            System.out.println("Error reading the JSON file: " + e.getMessage());
            return;
        }

        if (destinationValue == null) {
            System.out.println("The key 'destination' was not found in the provided JSON file.");
            return;
        }

        String randomString = generateRandomString(8);
        String combinedString = prnNumber + destinationValue + randomString;

        String md5Hash = generateMD5Hash(combinedString);
        if (md5Hash == null) {
            System.out.println("Error generating MD5 hash.");
            return;
        }

        System.out.println(md5Hash + ";" + randomString);
    }

    private static String findDestinationValue(String jsonFilePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(new File(jsonFilePath));

        return traverseJson(rootNode);
    }

    private static String traverseJson(JsonNode node) {
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                if (field.getKey().equals("destination")) {
                    return field.getValue().asText();
                }
                String value = traverseJson(field.getValue());
                if (value != null) return value;
            }
        } else if (node.isArray()) {
            for (JsonNode arrayElement : node) {
                String value = traverseJson(arrayElement);
                if (value != null) return value;
            }
        }
        return null;
    }

    private static String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }

        return sb.toString();
    }

    private static String generateMD5Hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();

            for (byte b : messageDigest) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }
}
