package org.example.myapp.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.example.myapp.client.MyMemoryClient;
import org.example.myapp.dto.TranslatedText;

@ApplicationScoped
public class TranslationService {

    @Inject
    @RestClient
    MyMemoryClient myMemoryClient;

    public TranslatedText translate(String text, String sourceLang, String targetLang) {
        if (text == null || text.isBlank()) {
            return new TranslatedText("");
        }

        // If source and target languages are identical, no translation API call needed
        if (sourceLang != null && sourceLang.equalsIgnoreCase(targetLang)) {
            return new TranslatedText(text);
        }

        try {
            // Build explicit language pair (e.g., "en|pl" or "autodetect|pl")
            String src = (sourceLang != null && !sourceLang.isBlank()) ? sourceLang : "autodetect";
            String langPair = src + "|" + targetLang;

            var response = myMemoryClient.translate(text, langPair);

            if (response != null && response.responseData != null && response.responseData.translatedText != null) {
                String translated = response.responseData.translatedText;

                // Handle MyMemory error response strings
                if (translated.contains("PLEASE SELECT TWO DISTINCT LANGUAGES")) {
                    return new TranslatedText(text);
                }

                return new TranslatedText(translated);
            }
        } catch (Exception e) {
            System.err.println("Translation warning: " + e.getMessage());
        }

        // Fallback: return original text if translation service fails
        return new TranslatedText(text);
    }

    // Overload for convenience if source language isn't known
    public TranslatedText translate(String text, String targetLang) {
        return translate(text, null, targetLang);
    }
}