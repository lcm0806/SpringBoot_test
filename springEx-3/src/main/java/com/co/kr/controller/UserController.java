package com.co.kr.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.co.kr.domain.BoardListDomain;
import com.co.kr.domain.LoginDomain;
import com.co.kr.service.UploadService;
import com.co.kr.service.UserService;
import com.co.kr.util.CommonUtils;
import com.co.kr.util.Pagination;
import com.co.kr.vo.LoginVO;

import lombok.extern.slf4j.Slf4j;

@ComponentScan(basePackages = {"com.co.kr.service.UploadService"})
@Controller
@Slf4j
@RequestMapping(value = "/")
public class UserController {

	@Autowired
	private UserService userService;
	
	@Autowired
	private UploadService uploadService;
	
	@RequestMapping(value = "board")
	public ModelAndView login(LoginVO loginDTO, HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		//session 처리
		HttpSession session = request.getSession();
		ModelAndView mav = new ModelAndView();
		
		//중복체크
		Map<String,String> map = new HashMap<>();
		map.put("mbId", loginDTO.getId());
		map.put("mbPw", loginDTO.getPw());
		
		//중복체크
		int dupleCheck = userService.mbDuplicationCheck(map);
		LoginDomain loginDomain = userService.mbGetId(map);
		
		if(dupleCheck == 0) {
			String alertText = "없는 아이디이거나 패스워드가 잘못되었습니다.";
			String redirectPath = "/main/signin";
			CommonUtils.redirect(alertText, redirectPath, response);
			return mav;
		}
		
		
		//현재아이피 추출
		String IP = CommonUtils.getClientIP(request);
		
		//session 저장
		session.setAttribute("ip", IP);
		session.setAttribute("id", loginDomain.getMbId());
		session.setAttribute("mbLevel", loginDomain.getMbLevel());
		
		List<BoardListDomain> items = uploadService.boardList();
		System.out.println("items ==>" + items);
		mav.addObject("items",items);
		
		mav.setViewName("board/boardList.html");
		
		return mav;
	}
	
	
	
	//좌측 메뉴 클릭시 보드화면 이동 (로그인된 상태)
	@RequestMapping(value = "bdList")
	public ModelAndView bdList() {
		ModelAndView mav = new ModelAndView();
		List<BoardListDomain> items = uploadService.boardList();
		System.out.println("items ==>" + items);
		mav.addObject("items",items);
		mav.setViewName("board/boardList.html");
		return mav;
	}
	
	//대시보드 리스트 보여주기
	@GetMapping("mbList")
	public ModelAndView mbList(HttpServletRequest request) {
		
		ModelAndView mav = new ModelAndView();
		HttpSession session = request.getSession();
		String page = (String) session.getAttribute("page");
		if(page == null)page = "1";
		
		//클릭페이지 세션에 담아줌
		session.setAttribute("page", page);
		
		//페이지네이션
		mav = mbListCall(request);
		
		mav.setViewName("admin/adminList.html");
		return mav;
	}
	
	//페이징으로 리스트 가져오기
	public ModelAndView mbListCall(HttpServletRequest request) {
		ModelAndView mav = new ModelAndView();
		
		//페이지네이션 쿼리 참고
		// SELECT * FROM jsp.member order by mb_update_at limit 1, 5; {offset}{limit}
		
		//전체갯수
		int totalcount = userService.mbGetAll();
		int contentnum = 10;
		
		//데이터 유무 분기때 사용
		boolean itemsNotEmpty;
		
		if(totalcount > 0) {
			
			
			//itemsNotEmpty true일떄만, 리스트 & 페이징 보여주기
			itemsNotEmpty = true;
			//페이지 표현 데이터 가져오기
			Map<String, Object> pagination = Pagination.pagination(totalcount, request);
			
			
			Map map = new HashMap<String, Integer>();
			    map.put("offset", pagination.get("offset"));
			    map.put("contentnum", contentnum);
			    
			    //페이지별 데이터 가져오기
			List<LoginDomain> loginDomain = userService.mbAllList(map);
			
			//모델 객체 넣어주기
			mav.addObject("itemsNotEmpty", itemsNotEmpty);
			mav.addObject("items", loginDomain);
			mav.addObject("rowNum", pagination.get("rowNum"));
			mav.addObject("pageNum", pagination.get("pageNum"));
			mav.addObject("startpage", pagination.get("startpage"));
			mav.addObject("endpage", pagination.get("endpage"));
			
			
		} else {
			itemsNotEmpty = false;
		}
		return mav;
	};
	
	//수정페이지 이동
	@GetMapping("/modify/{mbseq}")
	public ModelAndView mbModify(@PathVariable("mbSeq") String mbSeq, RedirectAttributes re) throws IOException{
		ModelAndView mav = new ModelAndView();
		re.addAttribute("mbSeq", mbSeq);
		mav.setViewName("redirect:/mbEditList");
		return mav;
	};
	
	//대시보드 리스트 보여주기
	@GetMapping("mbEditList")
	public ModelAndView mbListEdit(@RequestParam("mbSeq") String mbSeq, HttpServletRequest request) {
		
		ModelAndView mav = new ModelAndView();
		//해당리스트 가져옴
		mav = mbListCall(request);
		Map map = new HashMap<String, String>();
		map.put("mbSeq", mbSeq);
		LoginDomain loginDomain = userService.mbSelectList(map);
		mav.addObject("item",loginDomain);
		mav.setViewName("admin/adminEditList.html");
		return mav;
	};
	
	//수정 업데이트
	
	@RequestMapping("/update")
	public ModelAndView mbModify(LoginVO loginVO, HttpServletRequest request,RedirectAttributes re) throws IOException{
		
		
		ModelAndView mav = new ModelAndView();
		
		//page초기화
		HttpSession session = request.getSession();
		
		String page = "1";
		
		//db업데이트
		LoginDomain loginDomain = null;
		String IP = CommonUtils.getClientIP(request);
		loginDomain = LoginDomain.builder()
			.mbSeq(Integer.parseInt(loginVO.getSeq()))
			.mbId(loginVO.getId())
			.mbPw(loginVO.getPw())
			.mbLevel(loginVO.getLevel())
			.mbIp(IP)
			.mbUse("Y")
			.build();
		userService.mbUpdate(loginDomain);
		
	//첫 페이지로 이동
	re.addAttribute("page",page);
	mav.setViewName("redirect:/mbList");
	return mav;
	};
	//삭제
	@GetMapping("/remove/{mbSeq}")
	public ModelAndView mbRemove(@PathVariable("mbSeq") String mbSeq, RedirectAttributes re, HttpServletRequest request) throws IOException{
		ModelAndView mav = new ModelAndView();
		
		//db삭제
		Map map = new HashMap<String, String>();
		map.put("mbSeq", mbSeq);
		userService.mbRemove(map);
		
		//page초기화
		HttpSession session = request.getSession();
		
		//보고 있던 현재 페이지로 이동
		re.addAttribute("page", session.getAttribute("page"));
		mav.setViewName("redirct:/mbList");
		return mav;
	};
	
	//어드민의 멤버추가 &회원가입
	@PostMapping("create")
	public ModelAndView create(LoginVO loginVO, HttpServletRequest request, HttpServletResponse response) throws IOException{
		
		
		ModelAndView mav = new ModelAndView();
		
		//session 처리
		HttpSession session = request.getSession();
		
		//페이지 초기화
		String page = (String) session.getAttribute("page");
		if(page == null) page = "1";
		
		//중복체크
		Map<String, String> map = new HashMap<>();
		map.put("mbId", loginVO.getId());
		map.put("mbPw", loginVO.getPw());
		
		//중복체크
		int dupleCheck = userService.mbDuplicationCheck(map);
		System.out.println(dupleCheck);
		
		if(dupleCheck > 0) {
			String alertText = "중복이거나 유효하지 않은 접근입니다.";
			String redirectPath = "/main";
			System.out.println(loginVO.getAdmin());
			if(loginVO.getAdmin() != null) {
				redirectPath = "/main/mbList?page"+page;
			}
			CommonUtils.redirect(alertText, redirectPath, response);
		}else {
			
			//현재 아이피 추출
			String IP = CommonUtils.getClientIP(request);
		
		
		//전체 갯수
		int totalcount = userService.mbGetAll();
		
		//db insert준비
		LoginDomain loginDomain = LoginDomain.builder()
			.mbId(loginVO.getId())
			.mbPw(loginVO.getPw())
			.mbLevel((totalcount == 0)? "3" : "2")
			.mbIp(IP)
			.mbUse("Y")
			.build();
		
		//저장
		userService.mbCreate(loginDomain);
		
		if(loginVO.getAdmin() == null) {
		//session 저장
		session.setAttribute("ip", IP);
		session.setAttribute("id", loginDomain.getMbId());;
		session.setAttribute("mbLevel", (totalcount == 0)? "3" : "2");
		mav.setViewName("redirct:/mbList?page=1");
		 }else {
			 mav.setViewName("redirect:/mbList?page=1");
		 }
		}
		return mav;
	}
	//회원가입 화면
	@GetMapping("signin")
	public ModelAndView signIn() throws IOException {
		ModelAndView mav = new ModelAndView();
			mav.setViewName("signin/signin.html");
			return mav;
	}
	//로그아웃
	@RequestMapping("logout")
	public ModelAndView logout(HttpServletRequest request) {
		ModelAndView mav = new ModelAndView();
		HttpSession session = request.getSession();
		session.invalidate();//전체 삭제
		mav.setViewName("index.html");
		return mav;
	}
}
