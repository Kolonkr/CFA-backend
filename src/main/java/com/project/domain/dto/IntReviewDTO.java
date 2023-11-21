package com.project.domain.dto;

import lombok.Data;

@Data
public class IntReviewDTO {
	String writer;
	String content;
	long foodName;
	
	public IntReviewDTO(String writer, String content, long foodName) {
		this.writer = writer;
		this.content = content;
		this.foodName = foodName;
	}
}
