package com.project.controller;

import com.project.service.WebhookServiceImpl;
import lombok.RequiredArgsConstructor;
import com.project.domain.dto.InputTextDTO;
import com.project.domain.dto.OutputTextDTO;
import com.project.domain.dto.ProcssedTextDTO;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class WebhookController {
	private final WebhookServiceImpl webhookService;
	
	
	
    @PostMapping("/chatbot")
    public OutputTextDTO mainStream(HttpServletRequest request, @RequestBody InputTextDTO inputText) {
    	OutputTextDTO outPut = new OutputTextDTO(); 
        System.out.println("-----start Chatbot MainStream-----");
        ProcssedTextDTO processedInput = webhookService.textNER(inputText.getText(), request.getSession().getId());
        
        
        if("input_property".equals(processedInput.getDisplayName())) {
        	outPut.setText("property : " + processedInput.getFulfillmentText() + webhookService.sendproperty(processedInput.getUserIngredient(), processedInput.getUserTaste()) + "?");
        	outPut.setType(0);
        }else if("input_property_yes".equals(processedInput.getDisplayName())) {
        	outPut.setText("property_yes : " + processedInput.getFulfillmentText());
        	outPut.setData(webhookService.sendMenu(processedInput.getUserFoodName(),inputText.getLatitude(),inputText.getLongitude()));
        	outPut.setType(1);
        }else if("input_property_no".equals(processedInput.getDisplayName())) {
        	outPut.setText("property_no : " + processedInput.getFulfillmentText());
        	outPut.setType(0);
        }else if("input_food".equals(processedInput.getDisplayName())) {
        	outPut.setText("food : " + processedInput.getFulfillmentText());
        	outPut.setData(webhookService.sendMenu(processedInput.getUserFoodName(),inputText.getLatitude(),inputText.getLongitude()));
        	outPut.setType(1);
        }else {
        	outPut.setText("other : " + processedInput.getFulfillmentText());
        	outPut.setType(0);
        }
        return outPut;
    }
}

