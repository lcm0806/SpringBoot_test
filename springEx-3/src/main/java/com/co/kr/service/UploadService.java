package com.co.kr.service;

import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.co.kr.domain.BoardFileDomain;
import com.co.kr.domain.BoardListDomain;
import com.co.kr.vo.fileListVO;


public interface UploadService {
	public List<BoardListDomain> boardList();

	//인서트 및 업데이트
	public int fileProcess(fileListVO fileListVO, MultipartHttpServletRequest request, HttpServletRequest httpReq);
	
	//하나 삭제
	public void bdContentRemove(HashMap<String, Object> map);
	
	//하나 삭제
	public void bdFileRemove(BoardFileDomain boardFileDomain);

	public BoardListDomain boardSelectOne(HashMap<String, Object> map);

	public List<BoardFileDomain> boardSelectOneFile(HashMap<String, Object> map);
}
