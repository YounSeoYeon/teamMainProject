package com.boot.teamMainProject.service;

import java.util.ArrayList;

import com.boot.teamMainProject.model.GatheringVO;

public interface IGatheringService {

	ArrayList<GatheringVO> showlist(String hobbyNo);
	ArrayList<GatheringVO> detailgat(int gatNo);
	
	
	
	
	//ldh
	void insertGathering(GatheringVO gath);
}
