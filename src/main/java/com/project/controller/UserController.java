package com.project.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
//import com.project.repository.CustomSecurityContextRepository;
import com.project.domain.dto.GoogleLoginRequest;
import com.project.domain.dto.IdTokenDTO;
import com.project.domain.dto.TokenResponse;
import com.project.domain.dto.response.FirstLoginResponse;
import com.project.domain.dto.response.LoginResponse;
import com.project.domain.entity.User;
import com.project.service.TokenService;
import com.project.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/google")
public class UserController {

//    private final ConfigUtils configUtils;
	private final UserService userService;
	private final TokenService tokenService;
//    private final CustomSecurityContextRepository securityContextRepository;

//    @PostMapping(value = "/login")
//    public ResponseEntity<Object> moveGoogleInitUrl() {
//        String authUrl = configUtils.googleInitUrl();
//        URI redirectUri = null;
//        try {
//            redirectUri = new URI(authUrl);
//            HttpHeaders httpHeaders = new HttpHeaders();
//            httpHeaders.setLocation(redirectUri);
//            return new ResponseEntity<>(httpHeaders, HttpStatus.SEE_OTHER);
//        } catch (URISyntaxException e) {
//            e.printStackTrace();
//        }
//
//        return ResponseEntity.badRequest().build();
//    }

	@PostMapping(value = "/login/redirect")
	public ResponseEntity<?> redirectGoogleLogin(@RequestParam(value = "code") String authCode,
			HttpServletRequest httpServletRequest) {
		System.out.println("code:" + authCode);

		GoogleLoginRequest requestParams = userService.makeRequest(authCode);

		System.out.println(requestParams);

		try {
			ResponseEntity<String> apiResponseJson = userService.apiResponseJson(requestParams);
			System.out.println("apiResponseJson:" + apiResponseJson);

			TokenResponse googleLoginResponse = userService.googleLoginResponse(apiResponseJson);
			System.out.println("TokenResponse:" + googleLoginResponse);

			String resultJson = userService.resultJson(googleLoginResponse);
			System.out.println("resultJson:" + resultJson);
			if (resultJson != null) {
				ObjectMapper objectMapper = new ObjectMapper();
				IdTokenDTO idTokenDTO = objectMapper.readValue(resultJson, new TypeReference<IdTokenDTO>() {
				});
				System.out.println("idTokenDTO:" + idTokenDTO);

				String accessToken = googleLoginResponse.getAccessToken();
				long expiresIn = Long.parseLong(googleLoginResponse.getExpiresIn());
				String sessionId = httpServletRequest.getSession().getId();
				// SecurityContextHolder에 email, accessToken저장
				userService.setRedis(accessToken, expiresIn, sessionId);

				// 이메일 존재 여부
				boolean exists = userService.existsByEmail(idTokenDTO.getEmail());
				// email 존재 시
				if (exists) {
					Long userId = userService.findByEmail(idTokenDTO.getEmail()).getUser_id();
					// email존재하지만 refreshToken값이 들어올 경우
					if (googleLoginResponse.getRefreshToken() != null) {
						// refreshToken 업데이트
						tokenService.updateRefreshToken(userId, googleLoginResponse.getRefreshToken(), sessionId);
						FirstLoginResponse firstLoginResponse = userService.firstLoginResponse(idTokenDTO,
								googleLoginResponse, sessionId);

						// refreshToken이 포함된 DTO로 전달
						return ResponseEntity.ok().body(firstLoginResponse);
					} else { // email존재, refreshToken값이 들어오지 않을때
						// sessionId 업데이트s
						tokenService.updateSessionId(userId, sessionId);
						LoginResponse loginResponse = userService.loginResponse(idTokenDTO, googleLoginResponse,
								sessionId);
						System.out.println("123==" + ResponseEntity.ok().body(loginResponse));
						// refreshToken이 없는 DTO로 전달
						return ResponseEntity.ok().body(loginResponse);
					}
				} else {
					// 유저 처음 로그인 시
					// User 테이블에 저장
					User newUser = userService.createUser(idTokenDTO);
					// refresh토큰 저장
					tokenService.saveToken(newUser.getUser_id(), googleLoginResponse, sessionId);
					FirstLoginResponse firstLoginResponse = userService.firstLoginResponse(idTokenDTO,
							googleLoginResponse, sessionId);

					System.out.println("123==" + ResponseEntity.ok().body(firstLoginResponse));
					// refreshToken이 포함된 DTO로 전달
					return ResponseEntity.ok().body(firstLoginResponse);
				}

			} else {
				throw new Exception("정보 호출에 실패했습니다.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ResponseEntity.badRequest().body(null);
	}

//	@PostMapping("/logout")
//	public ResponseEntity<?> logout() {
//		try {
//			// 현재 로그인한 사용자의 ID를 가져옵니다.
//			// 이 예제에서는 세션에서 사용자의 ID를 가져오는 것으로 가정합니다.
//			Long userId = getCurrentUserIdFromSession();
//
//			if (userId != null) {
//				// 사용자의 sessionId를 NULL로 설정하고 modifiedDate를 업데이트합니다.
//				tokenRepository.deleteSessionIdByUserId(userId, Instant.now());
//				return ResponseEntity.ok().body("로그아웃이 성공적으로 처리되었습니다.");
//			} else {
//				return ResponseEntity.badRequest().body("로그인된 사용자가 아닙니다.");
//			}
//		} catch (Exception e) {
//			return ResponseEntity.status(500).body("로그아웃 처리 중 오류가 발생했습니다.");
//		}
//	}
}