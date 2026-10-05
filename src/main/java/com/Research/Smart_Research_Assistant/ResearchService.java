package com.Research.Smart_Research_Assistant;

import io.netty.resolver.DefaultAddressResolverGroup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Service
public class ResearchService {

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public ResearchService(ObjectMapper objectMapper) {

        this.webClient = WebClient.builder()
                .clientConnector(
                        new ReactorClientHttpConnector(
                                HttpClient.create()
                                        .resolver(DefaultAddressResolverGroup.INSTANCE)
                        )
                )
                .build();

        this.objectMapper = objectMapper;
    }

    public String processcontent(ResearchRequest request) {

        String prompt = buildprompt(request);

        Map<String, Object> requestbody = Map.of(
                "contents", new Object[]{
                        Map.of(
                                "parts", new Object[]{
                                        Map.of(
                                                "text", prompt
                                        )
                                }
                        )
                }
        );

        try {

            String response = webClient.post()
                    .uri(geminiApiUrl)
                    .header("Content-Type", "application/json")
                    .header("x-goog-api-key", geminiApiKey)
                    .bodyValue(requestbody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return extractTextFromResponse(response);

        } catch (Exception e) {

            e.printStackTrace();

            return "Gemini API Error: " + e.getMessage();
        }
    }

    private String extractTextFromResponse(String response) {

        try {

            GeminiResponse geminiResponse =
                    objectMapper.readValue(
                            response,
                            GeminiResponse.class
                    );

            if (geminiResponse.getCandidates() != null
                    && !geminiResponse.getCandidates().isEmpty()) {

                GeminiResponse.Candidate firstCandidate =
                        geminiResponse.getCandidates().get(0);

                if (firstCandidate.getContent() != null
                        && firstCandidate.getContent().getParts() != null
                        && !firstCandidate.getContent().getParts().isEmpty()) {

                    return firstCandidate
                            .getContent()
                            .getParts()
                            .get(0)
                            .getText();
                }
            }

            return "No content found in Gemini response.";

        } catch (Exception e) {

            return "Error Parsing Gemini Response: " + e.getMessage();
        }
    }

    private String buildprompt(ResearchRequest request) {

        StringBuilder prompt = new StringBuilder();

        switch (request.getOperation()) {

            case "summarize":

                prompt.append(
                        "Summarize the following text in exactly 3-4 short sentences. "
                                + "Include only the most important facts. "
                                + "Do not add any information that is not present "
                                + "in the given text:\n\n"
                );

                break;

            case "suggest":

                prompt.append(
                        "Based on the following content, suggest related topics "
                                + "and further reading. Format the response with clear "
                                + "heading and bullet points:\n\n"
                );

                break;

            default:

                throw new IllegalArgumentException(
                        "Unknown Operation: " + request.getOperation()
                );
        }

        prompt.append(request.getContent());

        return prompt.toString();
    }
}