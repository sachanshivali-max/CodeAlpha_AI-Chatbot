package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JPanel;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Main extends JFrame {

    private JTextField textField;
    private JTextArea responseArea;

    private void fetchAIResponse(String prompt) {
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                // API Key
                String apiKey = System.getenv("GROQ_API_KEY");
                String url = "https://api.groq.com/openai/v1/chat/completions";
                System.out.println(apiKey);

                // JSON structure
                String json = "{\"model\": \"llama-3.3-70b-versatile\", \"messages\": [{\"role\": \"user\", \"content\": \"" + prompt.replace("\"", "\\\"") + "\"}]}";

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + apiKey)
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                return response.body();
            }

            @Override
            protected void done() {
                try {
                    String responseBody = get();
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode root = mapper.readTree(responseBody);
                    String aiResponse = "No response";
                    if(root.has("choices") && root.get("choices").isArray() && root.get("choices").size() > 0){
                        aiResponse = root.get("choices").get(0).path("message").path("content").asText();
                    }
                    responseArea.append("You: " + textField.getText() + "\n");
                    responseArea.append("AI: " + aiResponse + "\n\n");
                    textField.setText("");
                } catch (Exception e) {
                    responseArea.append("Error: " + e.getMessage() + "\n");

                }
            }
        }.execute();
    }
     public Main() {
        setTitle("My AI Chatbot");
        setSize(400,400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        textField = new JTextField();
        JButton button = new JButton("Send");
         responseArea = new JTextArea();
         responseArea.setEditable(false);

        JButton clearButton = new JButton("Clear Chat");
        clearButton.setBackground(new Color(200,100,100));
        clearButton.setForeground(Color.WHITE);
        clearButton.setFocusPainted(false);

        clearButton.addActionListener(e -> responseArea.setText(""));


        //for design
         Font customFont = new Font("SansSerif", Font.PLAIN, 14);
         responseArea.setFont(customFont);
         textField.setFont(customFont);

         Color darkBackground = new Color(45, 45, 45);
         Color textColor = new Color(230, 230, 230);

         responseArea.setBackground(darkBackground);
         responseArea.setForeground(textColor);
         textField.setBackground(new Color(60, 60, 60));
         textField.setForeground(textColor);

         button.setBackground(new Color(100,150, 200));
         button.setForeground(Color.WHITE);
         button.setFocusPainted(false);

        button.addActionListener(e -> fetchAIResponse(textField.getText()));


        JPanel bottomPanel = new JPanel(new BorderLayout());
         JPanel buttonPanel = new JPanel(new GridLayout(1, 2));

         buttonPanel.add(button);
         buttonPanel.add(clearButton);

         bottomPanel.add(textField, BorderLayout.CENTER);
         bottomPanel.add(buttonPanel, BorderLayout.EAST);

         add(new JScrollPane(responseArea), BorderLayout.CENTER);
         add(bottomPanel, BorderLayout.SOUTH);
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try{
                //for modern System theme
               // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }catch(Exception e){
                e.printStackTrace();
            }
            Main frame = new Main();
            frame.setSize(400,400);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
    }
}
