package com.project.domain.entity;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import lombok.Data;

@Data
@Entity
public class Token extends BaseTimeEntity {

	@Id
	@Column(length = 255)
	private String refreshToken;

	@Column(name = "expiration_time")
	private Instant expirationTime;

	@OneToOne
	@JoinColumn(name = "user_id")
	private User user;

	@Column(name = "session_id")
	private String sessionId;

	public Token() {
		setExpirationTime(); // 생성자에서 만료 시간 설정
	}

	public void setExpirationTime() {
		Instant now = Instant.now();
		Instant expiration = now.plusSeconds(180 * 24 * 60 * 60); // 180일 후로 설정
		this.expirationTime = expiration;
	}

}
