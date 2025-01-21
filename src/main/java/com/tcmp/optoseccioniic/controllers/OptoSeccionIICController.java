package com.tcmp.optoseccioniic.controllers;

import org.apache.camel.ProducerTemplate;
import org.apache.camel.Exchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/optoseccioniic")
@CrossOrigin(origins = "http://localhost:3000")
public class OptoSeccionIICController {

    @Autowired
    private ProducerTemplate producerTemplate;

    @GetMapping("/start-flow")
    public ResponseEntity<String> startFlow() {
        // Crear un nuevo Exchange para enviar al ProducerTemplate
        Exchange exchange = producerTemplate.send("direct:start", e -> {});

        // Obtener el contenido CSV del Exchange
        String csvContent = exchange.getMessage().getBody(String.class);

        // Construir los encabezados de la respuesta HTTP para la descarga
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "text/csv");
        headers.add("Content-Disposition", "attachment; filename=OptoSeccionIIC.csv");

        return new ResponseEntity<>(csvContent, headers, HttpStatus.OK);
    }
}
