package com.project.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.dialogflow.v2.DetectIntentResponse;
import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.SessionName;
import com.google.cloud.dialogflow.v2.SessionsClient;
import com.google.cloud.dialogflow.v2.SessionsSettings;
import com.google.cloud.dialogflow.v2.TextInput;
import com.google.protobuf.util.JsonFormat;
import com.project.domain.dto.ProcssedTextDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WebhookServiceImpl implements WebhookService {
    private final Resource credentialsResource;  // 서비스 계정 키 파일을 주입

    @SuppressWarnings("unchecked")
    public ProcssedTextDTO textNER(String inputText, String inputSession) {
        System.out.println("-----Start textNer-----");// 로그
        ProcssedTextDTO procssedText = new ProcssedTextDTO();
        Map<String, Object> resultMap = new HashMap<String, Object>();
        Map<String, Object> displayMap = new HashMap<String, Object>();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
        	System.out.println("-----Start Service-----");// 로그
            // 서비스 계정 키를 credentialsResource에서 로드
            ServiceAccountCredentials credentials = ServiceAccountCredentials.fromStream(credentialsResource.getInputStream());
            FixedCredentialsProvider credentialsProvider = FixedCredentialsProvider.create(credentials);

            SessionsSettings sessionsSettings = SessionsSettings.newBuilder().setCredentialsProvider(credentialsProvider).build();
            try (SessionsClient sessionsClient = SessionsClient.create(sessionsSettings)) {
            	System.out.println("-----Start Session-----");// 로그
                SessionName session = SessionName.of("chatbot-401706", inputSession);
                TextInput.Builder textInput = TextInput.newBuilder().setText(inputText).setLanguageCode("en-US");
                QueryInput queryInput = QueryInput.newBuilder().setText(textInput).build();
                DetectIntentResponse response = sessionsClient.detectIntent(session, queryInput);

                String jsonResponse = JsonFormat.printer().print(response);

                resultMap = objectMapper.readValue(jsonResponse, Map.class);
                resultMap = objectMapper.convertValue(resultMap.get("queryResult"), Map.class);
                procssedText.setFulfillmentText((String) resultMap.get("fulfillmentText"));

                displayMap = objectMapper.convertValue(resultMap.get("intent"), Map.class);
                procssedText.setDisplayName((String) displayMap.get("displayName"));

                resultMap = objectMapper.convertValue(resultMap.get("parameters"), Map.class);
                procssedText.setUserFoodName((String) resultMap.get("user_foodname"));
                procssedText.setUserIngredient((ArrayList<String>) resultMap.get("user_ingredient"));
                procssedText.setUserTaste((ArrayList<String>) resultMap.get("user_taste"));

            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    	System.out.println("-----result-----");// 로그
    	System.out.println("-----" + procssedText.toString() + "-----");// 로그
        return procssedText;
    }

	public String sendMenu(String inputMenu, double latitude, double longitude) {
		System.out.println("-----sendMenu Start-----");//로그
		String url = "http://43.202.150.249:8088/restaurants"; // POST 요청을 보낼 URL을 지정

		// JSON 데이터를 포함하는 객체 생성
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		// RestTemplate 객체 생성
		RestTemplate restTemplate = new RestTemplate();
		Map<String, String> request = new HashMap<>();
		request.put("latitude", String.valueOf(latitude));
		request.put("longitude", String.valueOf(longitude));
		request.put("foodName", inputMenu);
		System.out.println("-----" + request.toString() + "-----");//로그
		
		// POST 요청 보내기
		ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
		
		// 서버로부터 받은 응답 데이터
		
		String str = response.getBody();
		ObjectMapper objectMapper = new ObjectMapper();
		
		try {
			str = objectMapper.readTree(str).toString();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return str;
	}

	public String sendproperty(ArrayList<String> inputIngredient, ArrayList<String> inputTaste) {
		String url = "http://43.202.150.249:8888/get_matching_menu"; // POST 요청을 보낼 URL을 지정
		System.out.println("Start sendproperty");
		// JSON 데이터를 포함하는 객체 생성
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		// RestTemplate 객체 생성
		RestTemplate restTemplate = new RestTemplate();
		Map<String, String> request = new HashMap<>();
		request.put("taste", String.join(",",inputTaste));
		request.put("made_with", String.join(",",inputIngredient));
		System.out.println("send request");

		// POST 요청 보내기
		ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

		// 서버로부터 받은 응답 데이터
		String str = response.getBody();
		
		System.out.println(str);
		
		return str;
	}
}