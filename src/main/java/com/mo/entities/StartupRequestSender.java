package com.mo.entities;

import java.util.Map;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Data @AllArgsConstructor @NoArgsConstructor
public class StartupRequestSender implements ApplicationListener<ApplicationReadyEvent> {

    private String targetUrl;
    
    private String data;

    
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        RestTemplate restTemplate = new RestTemplate();

        // Créer les en-têtes de la requête
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Créer l'entité HTTP avec les en-têtes et le corps
        HttpEntity<String> request = new HttpEntity<>(data, headers);

        // Envoyer la requête POST et récupérer la réponse
        String response = restTemplate.postForObject(targetUrl, request, String.class);

        // Afficher la réponse
        System.out.println(response);
    }
}

