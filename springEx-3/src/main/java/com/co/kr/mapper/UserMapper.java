package com.co.kr.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.co.kr.domain.LoginDomain;

@Mapper
public interface UserMapper {
	public LoginDomain mbSelectList(Map<String,String> map);
	
	public void mbCreate(LoginDomain loginDomain);
	
	public List<LoginDomain> mbAllList(Map<String, Integer> map);
	
	public int mbGetAll();
	
	public LoginDomain mbGetId(Map<String, String> map);
	
	public int mbDuplicationCheck(Map<String, String> map);
	
	public void mbUpdate(LoginDomain loginDomain);
	
	public void mbRemove(Map<String, String> map);
	
}
