import java.util.Random;

/**
 * @author eric
 * @since 1/12/2022
 */
public class RandomWordGenerator {
    public static void main(String[] args) {
        for (int j = 0; j < 20; j++) {
            int leftLimit = 97; // letter 'a'
            int rightLimit = 122; // letter 'z'
            int targetStringLength = 2;
            Random random = new Random();
            StringBuilder buffer = new StringBuilder(targetStringLength);
            for (int i = 0; i < targetStringLength; i++) {
                int randomLimitedInt = leftLimit + (int)
                        (random.nextFloat() * (rightLimit - leftLimit + 1));
                buffer.append((char) randomLimitedInt);
            }
            String generatedString = buffer.toString();

            System.out.println(generatedString);
        }
    }
}
