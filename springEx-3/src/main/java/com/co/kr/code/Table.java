package com.co.kr.code;

public enum Table {
	MEMBER("member"),
	FILES("files"),
	BOARD("board");
	
	private String table;
	
	Table(String table) {
		this.table = table;
	}
	
}
