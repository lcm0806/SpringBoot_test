package com.co.kr.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.co.kr.code.Code;
import com.co.kr.domain.BoardContentDomain;
import com.co.kr.domain.BoardFileDomain;
import com.co.kr.domain.BoardListDomain;
import com.co.kr.exception.RequestException;
import com.co.kr.mapper.UploadMapper;
import com.co.kr.util.CommonUtils;
import com.co.kr.vo.fileListVO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class UploadServiceImple implements UploadService{
	@Autowired
	private UploadMapper uploadMapper;
	
	@Override
	public List<BoardListDomain> boardList(){
		return uploadMapper.boardList();
	}
	

	@Override
	public int fileProcess(fileListVO fileListVO, MultipartHttpServletRequest request, HttpServletRequest httpReq) {
		HttpSession session = httpReq.getSession();
		
		//content domain생성
		BoardContentDomain boardContentDomain = BoardContentDomain.builder()
		.mbId(session.getAttribute("id").toString())
		.bdTitle(fileListVO.getTitle())
		.bdContent(fileListVO.getContent())
		.build();
		
		if(fileListVO.getIsEdit() !=null) {
			boardContentDomain.setBdSeq(Integer.parseInt(fileListVO.getSeq()));
			System.out.println("수정 업데이트");
			
			//db 업데이트
			uploadMapper.bdContentUpdate(boardContentDomain);
		}else {
			//db 인서트
			uploadMapper.contentUpload(boardContentDomain);
			System.out.println("db 인서트");
		}
		
		//file 데이터 db 저장시 쓰일 값 추출
		int bdSeq = boardContentDomain.getBdSeq();
		String mbId = boardContentDomain.getMbId();
		
		//파일 객체 담음 
		List<MultipartFile> multipartFiles = request.getFiles("files");
		
		//게시글 수정시 파일관련 물리저장 파일, db 데이터 삭제
		if(fileListVO.getIsEdit() !=null) { //수정시
			
			List<BoardFileDomain> fileList = null;
			
			
			
			
			
			
			for (MultipartFile multipartFile : multipartFiles) {
				
				if(!multipartFile.isEmpty()) {
					
					if(session.getAttribute("files") != null) {
						fileList = (List<BoardFileDomain>) session.getAttribute("files");
						
						
						for(BoardFileDomain list : fileList) {
							list.getUpFilePath();
							Path filePath = Paths.get(list.getUpFilePath());
							
							
								try {
									//파일 삭제
									Files.deleteIfExists(filePath);
									//삭제
									bdFileRemove(list);
								} catch (DirectoryNotEmptyException e) {
									throw RequestException.fire(Code.E404,"디렉토리가 존재하지 않습니다.", HttpStatus.NOT_FOUND);
								} catch(IOException e) {
									e.printStackTrace();
								}				
						}
					}
				}
			}
		}
		
////////////////////////////////////////세로운 파일 저장 //////////////////////////////////////
		
		//저장 root 경로 만들기
		
		Path rootPath = Paths.get(new File("C://").toString(),"upload",File.separator).toAbsolutePath().normalize();
		File pathCheck = new File(rootPath.toString());
		
		//folder check
		if(!pathCheck.exists()) pathCheck.mkdir();
		
		
		
		
		for(MultipartFile multipartFile : multipartFiles) {
			
			if(!multipartFile.isEmpty()) {
				
				//확장자 추출
				String originalFileExtension;
				String contentType = multipartFile.getContentType();
				String origFilename = multipartFile.getOriginalFilename();
				
				//확장자 조재안을경우
				if(ObjectUtils.isEmpty(contentType)) {
					break;
				}else {
					if(contentType.contains("image/jpeg")) {
						originalFileExtension = ".jpg";
					} else if(contentType.contains("image/png")) {
						originalFileExtension = ".png";
					} else {
						break;
					}
				}
				
				//파일명을 업로드한 날짜로 변환해서 저장
				String uuid = UUID.randomUUID().toString();
				String current = CommonUtils.currentTime();
				String newFileName = uuid + current + originalFileExtension;
				
				//최종 경로까지 지정
				Path targetPath = rootPath.resolve(newFileName);
				
				File file = new File(targetPath.toString());
				
				try {
					//파일복사저장
					multipartFile.transferTo(file);
					//파일 권한 설정(쓰기,읽기)
					file.setWritable(true);
					file.setReadable(true);
					
					//파일 domain 설정
					BoardFileDomain boardFileDomain = BoardFileDomain.builder()
					.bdSeq(bdSeq)
					.mbId(mbId)
					.upOriginalFileName(origFilename)
					.upNewFileName("resource/upload/"+newFileName) //WebConfig에 동적이미지 폴더 생성 했기떄문
					.upFilePath(targetPath.toString())
					.upFileSize((int)multipartFile.getSize())
					.build();
					
					//db인서트
					uploadMapper.fileUpload(boardFileDomain);
					System.out.println("upload done");
				} catch (IOException e) {
					throw RequestException.fire(Code.E404,"잘못된 업로드 파일",HttpStatus.NOT_FOUND);
				}
			}
		}
		return bdSeq;
	}

	@Override
	public void bdContentRemove(HashMap<String, Object> map) {
		// TODO Auto-generated method stub
		uploadMapper.bdContentRemove(map);
	}

	@Override
	public void bdFileRemove(BoardFileDomain boardFileDomain) {
		// TODO Auto-generated method stub
		uploadMapper.bdFileRemove(boardFileDomain);
	}
	
	//하나만 가져오기
	@Override
	public BoardListDomain boardSelectOne(HashMap<String, Object> map) {
		return uploadMapper.boardSelectOne(map);
	}
	
	//하나 게시글 파일만 가져오기
	public List<BoardFileDomain> boardSelectOneFile(HashMap<String, Object> map){
		return uploadMapper.boardSelectOneFile(map);
	}


	@Override
	public List<BoardListDomain> taticsboardList() {
		// TODO Auto-generated method stub
		return uploadMapper.taticsboardList();
	}

	
}
