package com.solana.scanner;

import org.bitcoinj.crypto.MnemonicCode;
import java.util.*;

public class BIP39Validator {
    
    static {
        try {
            // Initialize MnemonicCode with English wordlist
            MnemonicCode.INSTANCE = new MnemonicCode();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Validate if a mnemonic is valid BIP39 format
     */
    public static boolean isValid(String mnemonic) {
        try {
            if (mnemonic == null || mnemonic.trim().isEmpty()) {
                return false;
            }
            
            String[] words = mnemonic.trim().split("\\s+");
            
            // Must be 12 words for 128-bit entropy
            if (words.length != 12) {
                return false;
            }
            
            // Check if all words are valid English words
            List<String> wordList = new ArrayList<>(Arrays.asList(words));
            
            try {
                byte[] entropy = MnemonicCode.INSTANCE.toEntropy(wordList);
                List<String> recovered = MnemonicCode.INSTANCE.toMnemonic(entropy);
                
                // Verify it matches the original
                return recovered.size() == 12 && String.join(" ", recovered).equals(mnemonic);
            } catch (Exception e) {
                return false;
            }
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Convert mnemonic to entropy bytes
     */
    public static byte[] mnemonicToEntropy(String mnemonic) throws Exception {
        String[] words = mnemonic.trim().split("\\s+");
        List<String> wordList = new ArrayList<>(Arrays.asList(words));
        return MnemonicCode.INSTANCE.toEntropy(wordList);
    }
}
