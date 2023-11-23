package game;

import java.util.Random;

public class GameIdGenerator {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int NUM_CHARACTERS = CHARACTERS.length();
    private static final int NUM_DIGITS = 3;
    private static final Random RANDOM = new Random();

    public static String generateUniqueArenaId() {
        StringBuilder arenaId = new StringBuilder("hg-");

        for (int i = 0; i < NUM_DIGITS; i++) {
            int digit = RANDOM.nextInt(10); // Random integer between 0 and 9
            arenaId.append(digit);
        }

        char capitalLetter = CHARACTERS.charAt(RANDOM.nextInt(NUM_CHARACTERS));
        arenaId.append(capitalLetter);

        return arenaId.toString();
    }

    public static void main(String[] args) {
        String uniqueArenaId = generateUniqueArenaId();
        System.out.println("Generated Arena ID: " + uniqueArenaId);
    }
}