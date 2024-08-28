package org.example;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.Scanner;

public class DestinationHashGenerator {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java -jar DestinationHashGenerator.jar <PRN> <path/to/file.json>");
            System.exit(1);
        }

        String prn = args[0];
        String filePath = args[1];

        // Read the JSON file
        String destinationValue = readDestinationValue(filePath);
        if (destinationValue == null) {
            System.err.println("Failed to find 'destination' key in the JSON file.");
            System.exit(1);
        }

        // Generate a random alphanumeric string
        String randomString = generateRandomString(8);

        // Concatenate PRN, destination value, and random string
        String inputString = prn + destinationValue + randomString;

        // Generate MD5 hash
        String md5Hash = generateMD5Hash(inputString);

        // Print the result
        System.out.println(md5Hash + ";" + randomString);
    }

    private static String readDestinationValue(String filePath) {
        try (FileInputStream fis = new FileInputStream(filePath);
             Scanner scanner = new Scanner(fis, "UTF-8")) {

            JSONTokener tokener = new JSONTokener(scanner.useDelimiter("\\A").next());
            JSONObject jsonObject = new JSONObject(tokener);

            return findDestinationValue(jsonObject);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String findDestinationValue(JSONObject jsonObject) {
        // Traverse the JSON object to find the "destination" key
        for (String key : jsonObject.keySet()) {
            Object value = jsonObject.get(key);
            if (value instanceof JSONObject) {
                String result = findDestinationValue((JSONObject) value);
                if (result != null) return result;
            } else if (value instanceof org.json.JSONArray) {
                org.json.JSONArray array = (org.json.JSONArray) value;
                for (int i = 0; i < array.length(); i++) {
                    Object item = array.get(i);
                    if (item instanceof JSONObject) {
                        String result = findDestinationValue((JSONObject) item);
                        if (result != null) return result;
                    }
                }
            } else if ("destination".equals(key)) {
                return value.toString();
            }
        }
        return null;
    }

    private static String generateRandomString(int length) {
        String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }
        return sb.toString();
    }

    private static String generateMD5Hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}

