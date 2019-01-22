import java.io.*;
import java.util.*;

public class Main {

    public static void main(String[] args) throws IOException {
        System.out.println("running...");
        if(args[0].equals("NB"))
            naiveBayes(args);
        else if( args[0].equals("knn"))
            kNN(args);
        else
            System.out.println("Incorrect argument, please pass NB or knn");
    }

    public static ArrayList<TreeMap> realEmails = new ArrayList<TreeMap>();
    public static ArrayList<TreeMap> spamEmails = new ArrayList<TreeMap>();
    public static TreeSet<String> allWords = new TreeSet<String>();
    public static void kNN(String[] args) throws IOException {
        //Counts the correct amount of classified emails
        double correct = 0;
        //Counts the wrong amount of classified emails
        double wrong = 0;
        //pass args and set it to the k
        int k = Integer.parseInt(args[1]);

        //gets training files
        String target_dir = args[2] + "\\train";
        File dir = new File(target_dir);
        File[] files = dir.listFiles();
        //runs through all files in traing folder
        for (File f : files) {
            String filename = f.toString().replace(target_dir + "\\", "");
            //splits spam emails and real emails
            if (filename.startsWith("spm")) {
                writeSpam(f);
            } else {
                writeReal(f);
            }
        }
        //gets test files
        String test_dir = args[2] + "\\train";
        File test = new File(test_dir);
        File[] testFiles = test.listFiles();
        //run through all test files
        for (File t : testFiles) {
            String filename = t.toString().replace(test_dir + "\\", "");
            TreeMap<String, Integer> testCounts = new TreeMap<String, Integer>();
            if (t.isFile()) {
                BufferedReader inputStream = null;
                try {
                    inputStream = new BufferedReader(
                            new FileReader(t));
                    String line;
                    while ((line = inputStream.readLine()) != null) {
                        line.toLowerCase();
                        //parses words with delimeters
                        StringTokenizer parser = new StringTokenizer(line, " \t\n\r\f.,;:!?'");
                        while (parser.hasMoreTokens()) {
                            //Puts current word in treemap an either increments or sets value of 1
                            String currentWord = parser.nextToken();
                            if (testCounts.containsKey(currentWord)) {
                                Integer count = testCounts.get(currentWord) + 1;
                                testCounts.put(currentWord, count);
                            } else {
                                testCounts.put(currentWord, 1);
                            }
                        }
                    }
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                }
               //Holds a list of all cos values for the 300 email to 1 test email
                ArrayList<Double> allCos = new ArrayList<Double>();
                //Holds a list of real or spam for the 300 emails in training set
                ArrayList<String> options = new ArrayList<String>();

                //Get all Cos Similarity values for real emails
                for (TreeMap realMaps : realEmails) {
                    ArrayList<Double> valuelist = new ArrayList<Double>();
                    ArrayList<Double> testlist = new ArrayList<Double>();

                    //Determines what value to put in the two list, depending if the word exists in the
                    //test email and real email
                    for (String entry : allWords) {
                        if (testCounts.containsKey(entry))
                            testlist.add(1.0);
                        else
                            testlist.add(0.0);
                        if (realMaps.containsKey(entry)) {
                            valuelist.add(1.0);
                        } else {
                            valuelist.add(0.0);
                        }
                    }
                    Double cos = cosSimilarity(valuelist, testlist);
                    //adds value to list
                    allCos.add(cos);
                    //adds real to list
                    options.add("Real");
                }


                //Gets all cos similarity values of Spam emails
                for (TreeMap testMaps : spamEmails) {
                    ArrayList<Double> valuelist = new ArrayList<Double>();
                    ArrayList<Double> testlist = new ArrayList<Double>();
                    //Determines what value to put in the two list, depending if the word exists in the
                    //test email and spam email
                    for (String entry : allWords) {
                        if (testCounts.containsKey(entry))
                            //testlist.add(testCounts.get(entry).doubleValue());
                            testlist.add(1.0);
                        else
                            testlist.add(0.0);
                        if (testMaps.containsKey(entry)) {
                            //Double temp = ((Integer) testMaps.get(entry)).doubleValue();
                            valuelist.add(1.0);
                        } else {
                            valuelist.add(0.0);
                        }
                    }
                    Double cos = cosSimilarity(valuelist, testlist);
                    //adds value to list
                    allCos.add(cos);
                    //adds spam to list
                    options.add("Spam");
                }
                //Counters
                int spam = 0;
                int real = 0;

                //Bubble Sort from lowest to highest
                for (int i = 0; i < allCos.size() - 1; i++) {
                    // Last i elements are already in place
                    for (int j = 0; j < allCos.size() - i - 1; j++) {
                        if (allCos.get(j) > allCos.get(j + 1)) {
                            Double temp = allCos.get(j);
                            allCos.set(j, allCos.get(j + 1));
                            allCos.set(j + 1, temp);
                            String temp2 = options.get(j);
                            options.set(j, options.get(j + 1));
                            options.set(j + 1, temp2);
                        }
                    }
                }
                //determines the real or spam using k
                for (int i = allCos.size() - k; i < allCos.size(); i++) {
                    if (options.get(i) == "Spam") {
                        spam++;
                    } else
                        real++;
                }

                //Gets number of classification right and wrong
                if (spam < real) {
                    if (!filename.startsWith("spm"))
                        correct++;
                    else
                        wrong++;
                }
                if (real < spam) {
                    if (filename.startsWith("spm"))
                        correct++;
                    else
                        wrong++;
                }
            }

        }
        //calculate percentage
        System.out.println(correct / (correct + wrong) * 100 + " percentage correct!");

    }

    //writeSpam get all unique words and finds the weight of each word and puts it in a treemap,
    //which goes into an arraylist, for all spam emails
    public static void writeSpam(File f)throws IOException{
        TreeMap<String, Integer> wordCounts = new TreeMap<String, Integer>();
        if (f.isFile()) {
            BufferedReader inputStream = null;
            try {
                inputStream = new BufferedReader(
                        new FileReader(f));
                String line;

                while ((line = inputStream.readLine()) != null) {
                    line.toLowerCase();
                    StringTokenizer parser = new StringTokenizer(line, " \t\n\r\f.,;:!?'");
                    while (parser.hasMoreTokens()) {
                        String currentWord = parser.nextToken();

                        if (wordCounts.containsKey(currentWord)) {
                            Integer count = wordCounts.get(currentWord) + 1;
                            wordCounts.put(currentWord, count);
                        } else {
                            wordCounts.put(currentWord, 1);
                            allWords.add(currentWord);
                        }
                    }
                }
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
            spamEmails.add(wordCounts);
        }
    }

    //writeSpam get all unique words and finds the weight of each word and puts it in a treemap,
    //which goes into an arraylist, for all real emails
    public static void writeReal(File f)throws IOException{
        TreeMap<String, Integer> wordCounts = new TreeMap<String, Integer>();
        if (f.isFile()) {
            BufferedReader inputStream = null;

            try {
                inputStream = new BufferedReader(
                        new FileReader(f));
                String line;

                while ((line = inputStream.readLine()) != null) {
                    line.toLowerCase();
                    StringTokenizer parser = new StringTokenizer(line, " \t\n\r\f.,;:!?'");
                    while (parser.hasMoreTokens()) {
                        String currentWord = parser.nextToken();

                        if (wordCounts.containsKey(currentWord)) {
                            Integer count = wordCounts.get(currentWord) + 1;
                            wordCounts.put(currentWord, count);
                        } else {
                            wordCounts.put(currentWord, 1);
                            allWords.add(currentWord);
                        }
                    }


                }
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
            realEmails.add(wordCounts);
        }
    }

    //returns the Cos Similarity of two array lists
    public static Double cosSimilarity(ArrayList<Double> valuelist, ArrayList<Double> testlist){
        Double dotProduct = 0.0;
        for (int i = 0; i < valuelist.size(); i++) {
            Double temp;
            temp = valuelist.get(i) * testlist.get(i);
            dotProduct += temp;
        }
        Double absoluteValueList = 0.0;
        for (int i = 0; i < valuelist.size(); i++) {
            Double temp;
            temp = Math.pow(valuelist.get(i), 2);
            absoluteValueList += temp;
        }
        Double absoluteTestList = 0.0;
        for (int i = 0; i < valuelist.size(); i++) {
            Double temp;
            temp = Math.pow(testlist.get(i), 2);
            absoluteTestList += temp;
        }
        Double cos = dotProduct / (absoluteTestList * absoluteValueList);
        return cos;
    }

    public static void naiveBayes(String[] args){
    int numHam = 0; // Number legitimate emails
    int numSpam = 0; // Number spam emails
    int numHamWords = 0; // Total words across all legitimate emails
    int numSpamWords = 0; // Total words across all spam emails

    TreeMap<String, Integer> wordEmailCountHam = new TreeMap<String, Integer>();
    TreeMap<String, Integer> wordEmailCountSpam = new TreeMap<String, Integer>();
    TreeMap<String, Integer> hamTrainWords = new TreeMap<String, Integer>();
    TreeMap<String, Integer> spamTrainWords = new TreeMap<String, Integer>();

    TreeMap<String, Integer[]> testResults = new TreeMap<String, Integer[]>();

    TreeSet<String> allWords = new TreeSet<String>();

    // Variables for directories where training & testing data stored
    String trainDirectoy = args[1] + "\\train";
    String testDirectoy = args[1] + "\\test";

    File trainFolder = new File(trainDirectoy);
    File testFolder = new File(testDirectoy);

    File[] trainFiles = trainFolder.listFiles();
    File[] testFiles = testFolder.listFiles();

    Scanner input = null;

    // Process all of the emails in the training set
    for (File email : trainFiles) {

        TreeSet<String> trainWordSetHam = new TreeSet<String>();
        TreeSet<String> trainWordSetSpam = new TreeSet<String>();

        // This try-catch block handles the opening of a text file to be
        // scanned, but if an exception is thrown because no file can be found
        // then the program simply closes
        try {
            input = new Scanner(email);
            if (email.getName().contains("spm")) {
                numSpam++;
            } else {
                numHam++;
            }
        } catch (FileNotFoundException e) {
            System.out.println("Error openning file..");
            System.exit(1);
        } // End try-catch

        // Assuming a file file is found, this try block scans the entire text,
        // breaks it down into tokens(words), then assigns those words to an
        // ArrayList & HashSet for later comparison.
        try {
            String line;

            // While loop scans each line of text and assigns it to the line
            // variable, then stops running once a null character (end of file)
            // is reached
            while ((line = input.nextLine()) != null) {

                // String tokenizer breaks each text line into raw tokens
                StringTokenizer st = new StringTokenizer(line);

                // While loop runs while for each token found within a line
                while (st.hasMoreTokens()) {

                    // Converts the string token to lowercase and assigns it to
                    // a new string for further processing
                    String tokenWord = st.nextToken().toLowerCase();
                    String editedWord = ""; // String to hold the final string

                    // For loop parses through each character of the initial
                    // token, and only assigns letters or digits to editedWord,
                    // thus removing any punctuation at the end of the string
                    for (int i = 0; i < tokenWord.length(); i++) {
                        if (Character.isLetterOrDigit(tokenWord.charAt(i))) {
                            editedWord += tokenWord.charAt(i);
                        } // End if
                    } // End for loop

                    // Adds fully processed word to the respective TreeSets (which
                    // automatically removes any duplicate entries) based upon the
                    // origin was a spam or ham email.
                    if (email.getName().contains("spm")) {
                        trainWordSetSpam.add(editedWord);
                        numSpamWords++;
                        allWords.add(editedWord);
                        if (spamTrainWords.containsKey(editedWord)) {
                            Integer count = spamTrainWords.get(editedWord) + 1;
                            spamTrainWords.put(editedWord, count);
                        } else {
                            spamTrainWords.put(editedWord, 1);
                        } // End if-else
                    } else {
                        trainWordSetHam.add(editedWord);
                        numHamWords++;
                        allWords.add(editedWord);
                        if (hamTrainWords.containsKey(editedWord)) {
                            Integer count = hamTrainWords.get(editedWord) + 1;
                            hamTrainWords.put(editedWord, count);
                        } else {
                            hamTrainWords.put(editedWord, 1);
                        } // End if-else
                    } // End if-else
                } // End while
            } // End while
        } catch (NoSuchElementException e) {
            // no more lines in the file, no handler is necessary
        } // End try-catch

        // Use list of words in ham or spam email to update the master word list
        // for each category with the count for number of email types the word
        // appeared in, as opposed to the total number of times the word appears
        // in a specific email.
        if (email.getName().contains("spm")) {
            for (String spamWord : trainWordSetSpam) {
                if (wordEmailCountSpam.containsKey(spamWord)) {
                    Integer count = wordEmailCountSpam.get(spamWord) + 1;
                    wordEmailCountSpam.put(spamWord, count);
                } else {
                    wordEmailCountSpam.put(spamWord, 1);
                } // End if-else
            } // End for loop
        } else {
            for (String hamWord : trainWordSetHam) {
                if (wordEmailCountHam.containsKey(hamWord)) {
                    Integer count = wordEmailCountHam.get(hamWord) + 1;
                    wordEmailCountHam.put(hamWord, count);
                } else {
                    wordEmailCountHam.put(hamWord, 1);
                } // End if-else
            } // End for loop
        } // End if-else

    } // End for loop

    // Remove any words deemed irrelevant, such as a blank space, that may be
    // unnecessary for probability computations or might negatively skew the
    // numbers.
    wordEmailCountHam.remove("");
    wordEmailCountSpam.remove("");

    // Output complete Ham wordlist to file
    // try {
    // FileWriter output = new FileWriter("trainingWords.txt");
    // // System.out.println("Processing Legit Emails");
    // output.write("Legit Emails (" + wordEmailCountHam.size() + ")*** \r\n");
    // for (String word : wordEmailCountHam.keySet()) {
    // output.write(word + " - " + wordEmailCountHam.get(word) + " - \r\n");
    // }
    // output.write("\r\n");
    // output.write("\r\n");
    // output.close();
    // } catch (IOException e) {
    // System.out.println("Error writing to file..");
    // e.printStackTrace();
    // System.exit(1);
    // } // End try-catch

    // Output complete Spam wordlist to file
    // try {
    // FileWriter output = new FileWriter("trainingWords.txt", true);
    // // System.out.println("Processing Spam Emails");
    // output
    // .write("***Spam Emails (" + wordEmailCountSpam.size() + ")*** \r\n");
    // for (String word : wordEmailCountSpam.keySet()) {
    // output.write(word + " - " + wordEmailCountSpam.get(word) + " - \r\n");
    // }
    // output.write("\r\n");
    // output.close();
    // } catch (IOException e) {
    // System.out.println("Error writing to file..");
    // e.printStackTrace();
    // System.exit(1);
    // } // End try-catch

    // System.out.println("ALL DONE, Ham=" + numHam + ", Spam=" + numSpam);
    // System.out
    // .println("Ham Words=" + numHamWords + ", Spam Words =" + numSpamWords);
    int vocabulary = allWords.size();
    System.out.println("Total number of words: " + vocabulary);

    // System.out.println();
    // System.out.println("Processing Test Emails");

    // M-Estimate variables, constants
    double mValue = 1.0;// (double) numHamWords + (double)
    // numSpamWords;//
    double pValue = 0.3; // = 1.0 / mValue; // 1.0 / 3.0;

    double probHamClass = (double) numHam
            / ((double) numHam + (double) numSpam);
    // double probHamClass = (double) numHamWords
    // / ((double) numHamWords + (double) numSpamWords);
    double probSpamClass = (double) numSpam
            / ((double) numHam + (double) numSpam);
    // double probSpamClass = (double) numSpamWords
    // / ((double) numHamWords + (double) numSpamWords);

    // Process all of the emails in the testing set
    for (File email : testFiles) {

        double condProbHam = 1;
        double condProbSpam = 1;

        TreeSet<String> testEmailWords = new TreeSet<String>();

        try {
            input = new Scanner(email);
        } catch (FileNotFoundException e) {
            System.out.println("Error openning file..");
            System.exit(1);
        } // End try-catch

        // Each word in the email is tokenized and stored in a map with its count
        try {
            String line;

            // While loop scans each line of text and assigns it to the line
            // variable, then stops running once a null character (end of file)
            // is reached
            while ((line = input.nextLine()) != null) {

                // String tokenizer breaks each text line into raw tokens
                StringTokenizer st = new StringTokenizer(line);

                // While loop runs while for each token found within a line
                while (st.hasMoreTokens()) {

                    // Converts the string token to lowercase and assigns it to
                    // a new string for further processing
                    String tokenWord = st.nextToken().toLowerCase();
                    String editedWord = ""; // String to hold the final string

                    // For loop parses through each character of the initial
                    // token, and only assigns letters or digits to editedWord,
                    // thus removing any punctuation at the end of the string
                    for (int i = 0; i < tokenWord.length(); i++) {
                        if (Character.isLetterOrDigit(tokenWord.charAt(i))) {
                            editedWord += tokenWord.charAt(i);
                        } // End if
                    } // End for loop

                    // Adds fully processed word to the email's TreeSet which will take
                    // care of eliminating any duplicates already present in the Map
                    testEmailWords.add(editedWord);
                } // End while
            } // End while
        } catch (NoSuchElementException e) {
            // no more lines in the file, no handler is necessary
        } // End try-catch

        // Remove any blank spaces or irrelevant words
        testEmailWords.remove("");

        // Conditional probabilities are calculated for each word in the test
        // email's TreeMap based on its number of occurrences in comparison to how
        // many times, if any, the word appeared in legit or spam emails from the
        // training set.

        int hamWords = hamTrainWords.values().stream().mapToInt(i -> i).sum();
        // Legit email conditional probability calculated first
        for (String word : testEmailWords) {
            int wordCount = 0;
            if (wordEmailCountHam.containsKey(word)) {
                wordCount = wordEmailCountHam.get(word);
                if (condProbHam
                        * mEstCondProb(wordCount, numHam, mValue, pValue) != 0.0) {
                    condProbHam *= mEstCondProb(wordCount, numHam, mValue, pValue);
                }
            } else {
                if (condProbHam
                        * mEstCondProb(wordCount, numHam, mValue, pValue) != 0.0) {
                    condProbHam *= mEstCondProb(wordCount, numHam, mValue, pValue);
                }
            } // End if-else
            // System.out.println("Current cond. prob. is: " + condProbHam);
        } // End for loop
        // Then probability that email is legit is calculated
        double probEmailHam = condProbHam * probHamClass;
        // System.out.println("condProbHam is: " + condProbHam);
        // System.out.println("probHamClass is: " + probHamClass);
        // System.out.println("probEmailHam is: " + probEmailHam);

        int spamWords = spamTrainWords.values().stream().mapToInt(i -> i).sum();
        // Spam email conditional probability calculated second
        for (String word : testEmailWords) {
            int wordCount = 0;
            if (wordEmailCountSpam.containsKey(word)) {
                wordCount = wordEmailCountSpam.get(word);
                if (condProbHam
                        * mEstCondProb(wordCount, numSpam, mValue, 1.0 - pValue) != 0.0) {
                    condProbSpam *= mEstCondProb(wordCount, numSpam, mValue,
                            1.0 - pValue);
                }
            } else {
                if (condProbSpam
                        * mEstCondProb(wordCount, numSpam, mValue, 1.0 - pValue) != 0.0) {
                    condProbSpam *= mEstCondProb(wordCount, numSpam, mValue,
                            1.0 - pValue);
                }
            } // End if-else
            // System.out.println("mValue: " + mValue);
        } // End for loop
        // Then probability that email is spam is calculated
        double probEmailSpam = condProbSpam * probSpamClass;
        // System.out.println("condProbSpam is: " + condProbSpam);
        // System.out.println("probSpamClass is: " + probSpamClass);
        // System.out.println("probEmailSpam is: " + probEmailSpam);

        Integer[] classResults = new Integer[2];
        if (probEmailSpam > probEmailHam) {
            if (email.getName().contains("spm")) {
                classResults[0] = 0;
                classResults[1] = 1;
                testResults.put(email.getName(), classResults);
            } else {
                classResults[0] = 0;
                classResults[1] = 0;
                testResults.put(email.getName(), classResults);
            }
        } else if (probEmailSpam <= probEmailHam) {
            if (email.getName().contains("spm")) {
                classResults[0] = 1;
                classResults[1] = 0;
                testResults.put(email.getName(), classResults);
            } else {
                classResults[0] = 1;
                classResults[1] = 1;
                testResults.put(email.getName(), classResults);
            } // End if-else
        } // End if-else
    } // End for loop

    double correctPredictions = 0;
    double totalPredictions = 0;

    // Output complete classification test results to file
    try {
        FileWriter output = new FileWriter("naiveBayesTestResults.txt");
        for (String fileName : testResults.keySet()) {
            Integer[] classResults = testResults.get(fileName);
            output.write(fileName + " | " + classResults[0] + " | "
                    + classResults[1] + "\r\n");
            totalPredictions++;
            correctPredictions += (double) classResults[1];
        }
        output.close();
    } catch (IOException e) {
        System.out.println("Error writing to file..");
        e.printStackTrace();
        System.exit(1);
    } // End try-catch
    double accuracy = correctPredictions / totalPredictions;
    // System.out.println("ALL DONE");
    System.out.println("Naive Bayes Accuracy: " + accuracy);
    int hamWords = hamTrainWords.values().stream().mapToInt(i -> i).sum();
    int spamWords = spamTrainWords.values().stream().mapToInt(i -> i).sum();
    // System.out.println("hamWords: " + hamWords);
    // System.out.println("spamWords: " + spamWords);
} // End main()

    public static double mEstCondProb(int wordCount, int totalClassNum,
                                      double mValue, double pValue) {
        double result = ((double) wordCount + mValue)
                / ((double) totalClassNum + mValue);
        return result;
    } // End mEstCondProb()


}
