package com.co.kr.code;

public enum Table {
	MEMBER("member"),
	FILES("files"),
	BOARD("board"),
	TATICSBOARD("taticsboard"),
	TATICSFILES("taticsfiles");
	private String table;
	
	Table(String table) {
		this.table = table;
	}
	
}
