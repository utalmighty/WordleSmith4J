package com.utkarsh.wordle;

import java.io.*;
import java.util.List;
import java.util.Scanner;

public class Wordle {

    enum Color {
        GREEN, YELLOW, RED
    }

    class Letter {
        char letter;
        Color color;

        public Letter(char letter, Color color) {
            this.letter = letter;
            this.color = color;
        }
    }

    private static final int LENGTH = 5;
    private static final Scanner sc = new Scanner(System.in);

    private final Letter[] guesses = new Letter[LENGTH];
    private List<String> words;

    Wordle(String str, String color) {
        final String FILE_PATH = "./resources/FiveLetterWords.txt";
        readWordList(FILE_PATH);
        parse(str, color);
    }

    private void readWordList(String filePath) {
        try (InputStream is = new FileInputStream(filePath)) {
            InputStreamReader reader = new InputStreamReader(is);
            this.words = new BufferedReader(reader).lines().toList();
        } catch (IOException exp) {
            System.err.println("Unable to read file. Error: " + exp.getLocalizedMessage());
        }
    }

    private void parse(String str, String color) {
        str = str.toLowerCase();
        color = color.toLowerCase();
        for (int i=0; i<LENGTH; i++) {
            Color c = color.charAt(i) == 'g' ? Color.GREEN : color.charAt(i) == 'y' ? Color.YELLOW : Color.RED;
            guesses[i] = new Letter(str.charAt(i), c);
        }
        filter();
    }

    private void filter() {
        for (int i=0; i<LENGTH; i++) {
            final int indx = i;
            if (guesses[indx].color == Color.GREEN) {
                words = words.parallelStream().filter(w-> w.charAt(indx) == guesses[indx].letter).toList();
            } else if (guesses[indx].color == Color.YELLOW) {
                words = words.parallelStream()
                    // contains letter
                    .filter(w-> w.contains(guesses[indx].letter + ""))
                    // does not contains letter at current index
                    .filter(w-> w.charAt(indx) != guesses[indx].letter).toList();
                for (int j=0; j<LENGTH; j++) {
                    final int greenIndx = j;
                    if (guesses[indx].color == Color.GREEN) {
                        words = words.parallelStream()
                            // does not conatin letter at green position
                            .filter(w-> w.charAt(indx) != guesses[greenIndx].letter).toList();
                    }
                }
            } else {
                words = words.parallelStream().filter(w-> checkGreenLetters(guesses[indx].letter) || !w.contains(guesses[indx].letter + "")).toList();
            }
        }
        final String nextWord = words.get(0);
        System.out.println("Try: " + nextWord.toUpperCase());
        parse(nextWord, sc.next());
    }

    public boolean checkGreenLetters(char letter) {
        for (int i=0; i<LENGTH; i++) {
            if (guesses[i].color == Color.GREEN && guesses[i].letter == letter) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        System.out.println("What word you want to start with? (recommended: AUDIO)");
        final String startingWord = sc.next();
        System.out.println("OK! whats the output?");
        final String output = sc.next(); // eg: rrryg
        new Wordle(startingWord, output);
    }

    
}