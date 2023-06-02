package com.example.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class KeyStore {
    private final List<String> frequentlyAccessedKeys = new ArrayList<>();
    private final List<String> infrequentlyAccessedKeys = new ArrayList<>();
    private final Random random = new Random();

    public KeyStore() {
        for (int i = 1; i <= 200; i++) {
            frequentlyAccessedKeys.add(i + "");
        }

        for (int i = 201; i <= 1000; i++) {
            infrequentlyAccessedKeys.add(i + "");
        }
    }

    public String randomKeyByPercentage() {
        double randomValue = random.nextDouble();

        if (randomValue < 0.2) {
            int index = random.nextInt(infrequentlyAccessedKeys.size());
            return infrequentlyAccessedKeys.get(index);
        } else {
            int index = random.nextInt(frequentlyAccessedKeys.size());
            return frequentlyAccessedKeys.get(index);
        }
    }
}
