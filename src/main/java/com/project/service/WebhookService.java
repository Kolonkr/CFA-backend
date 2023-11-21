package com.project.service;

import java.util.ArrayList;
import com.project.domain.dto.ProcssedTextDTO;

public interface WebhookService {
	public ProcssedTextDTO textNER(String inputText,String session);
	public String sendMenu(String inputMenu, double latitude, double longitude);
	public String sendproperty(ArrayList<String> inputIngredient, ArrayList<String> inputTaste);
}
