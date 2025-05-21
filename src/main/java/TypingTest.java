import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class TypingTest {

    private static volatile String lastInput = "";
    private static Scanner scanner = new Scanner(System.in);
    private static List<Duration> durations = new ArrayList<>();
    private static Instant startTime;
    private static Instant endTime;
    private static int correct = 0;

    public static class InputRunnable implements Runnable {
        @Override
        public void run() {

            try {
                while (!Thread.currentThread().isInterrupted()) {
                    lastInput = scanner.nextLine();
                    if (!lastInput.isEmpty()) {
                        endTime = Instant.now();
                    }
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

        }

    }


    public static void testWord(String wordToTest) {
        try {
            System.out.println(wordToTest);
            lastInput = "";
            startTime = Instant.now();
            Duration duration = Duration.between(startTime, endTime);
            durations.add(duration);
            System.out.println();
            System.out.println("You typed: " + lastInput);
            if (lastInput.equals(wordToTest)) {
                System.out.println("Correct : " + duration.toMillis());
                correct++;
            } else {
                System.out.println("Incorrect");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void typingTest(List<String> inputList) throws InterruptedException {
        Thread thread = new Thread(new InputRunnable());
        thread.start();
        Thread.sleep(1000);
        for (String wordToTest : inputList) {
            testWord(wordToTest);
            Thread.sleep(100); // Pause briefly before showing the next word
        }
        thread.interrupt();
        System.out.println("===============================");
        System.out.println("correct : " + correct);
        System.out.println("accuracy : " + (correct * 100) / inputList.size() + "%");
    }

    public static List<String> readWordsFromFile(String filename) {
        List<String> words = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    words.add(line.trim());
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to read the file: " + filename);
            e.printStackTrace();
        }
        return words;
    }

    public static void main(String[] args) throws InterruptedException {


        List<String> words = new ArrayList<>();
        File wordsFile = new File("src\\main\\resources\\Words.txt");
        try {
            Scanner wordScanner = new Scanner(wordsFile);
            while (wordScanner.hasNextLine()) {
                words.add(wordScanner.nextLine());
            }
            wordScanner.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        Collections.shuffle(words);
        List<String> selectedWords;
        while(true){

            try{
                System.out.println("Type the number of words you want to type : (0 to 100) ");
                int numberOfWords = Integer.parseInt(scanner.nextLine());
                selectedWords = words.subList(0, numberOfWords);
                break;
            }catch(IndexOutOfBoundsException e){
                System.out.println("Enter a valid number");
                continue;
            }
        }

        typingTest(selectedWords);
        System.out.println("Press enter to exit.");
    }
}


