package com.codetoculture.agents;

import com.google.adk.agents.LlmAgent;
import com.google.adk.models.langchain4j.LangChain4j;
import com.google.adk.tools.FunctionTool;
import com.google.adk.tools.Annotations.Schema;
import com.google.adk.web.AdkWebServer;
import dev.langchain4j.model.openai.OpenAiChatModel;

import java.time.Duration;

public class AdTechAnalyzerAgent {

    // 1. Define a Tool for the Agent to use
    @Schema(description = "Fetches the current OpenRTB win rate and average bid floor data for a given SSP or DSP campaign ID.")
    public static String getBiddingMetrics(
            @Schema(description = "The ID of the SSP or DSP campaign") String campaignId) {

        // In a production environment, this would query your AWS RDS or internal data warehouse.
        // For this example, we mock the telemetry response.
        return String.format("Campaign %s: OpenRTB win rate is 38%%, average bid floor is $1.25 CPM. Latency is compliant with IAB Tech Lab standards.", campaignId);
    }

    public static void main(String[] args) {
        // 2. Configure the local server connection
        // baseUrl to "http://localhost:11434/v1" if using Ollama
        // I was unable to make this work with Lm Studio, so I'm using Ollama instead.
        OpenAiChatModel localModel = OpenAiChatModel.builder()
                .baseUrl("http://localhost:11434/v1")
                .apiKey("local-ignore")
                .modelName("gemma4:latest")
                .timeout(Duration.ofSeconds(120))
                .build();

        // 3. Wrap the LangChain4j model for the Google ADK
        LangChain4j adkLocalModel = LangChain4j.builder()
                .chatModel(localModel)
                .modelName("gemma4:latest")
                .build();

        //4. Start the Agent
        AdkWebServer.start( LlmAgent.builder()
                .name("AdTech Analyzer")
                .description("An AI assistant for programmatic ad bidding analysis.")
                .model(adkLocalModel)
                .instruction("You are an expert AdTech assistant. Use the provided tools to fetch campaign metrics and provide clear optimization recommendations based on the data.")
                .tools(FunctionTool.create(AdTechAnalyzerAgent.class, "getBiddingMetrics"))
                .build()
        );

        System.out.println("Agent initialized successfully.");
    }
}