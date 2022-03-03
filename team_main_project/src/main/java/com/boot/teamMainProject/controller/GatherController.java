package com.boot.teamMainProject.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.boot.teamMainProject.model.*;
import com.boot.teamMainProject.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.boot.teamMainProject.service.GatherScheduleService;
import com.boot.teamMainProject.service.GatheringService;
import com.boot.teamMainProject.service.SpaceReservationService;

@Controller
public class GatherController {
    @Autowired
    GatherScheduleService service;
    @Autowired
    SpaceReservationService reservationService;
    @Autowired
    GatherScheduleService scheduleService;
    @Autowired
    GatheringService service3;
    @Autowired
    MemberService memberService;

    // 모임 일정 신청 페이지
    @RequestMapping("GatherSchedule")
    public String GatherSchedule(HttpSession session, Model model) {
        String sid = (String) session.getAttribute("sid");
        MemberVO mem = memberService.detailViewMember(sid);

        model.addAttribute("mem", mem);
        return "bch/gatherSchedule";
    }

    // 모임 일정 신청 POST 공간 빌림.
    @ResponseBody
    @RequestMapping("WriteGatherSchedule")
    public void WriteGatherSchedule(@RequestParam("memNick") String memNick,
                                      @RequestParam("gatNo") int gatNo,
                                      @RequestParam("gatScheTitle") String gatScheTitle,
                                      @RequestParam("gatScheDate") String gatScheDate,
                                      @RequestParam("gatScheTime") String gatScheTime,
                                      @RequestParam("gatScheMax") int gatScheMax,
                                      @RequestParam("scheduleAddress") String scheduleAddress,
                                      @RequestParam("scheduleSpace") String scheduleSpace,
                                      @RequestParam("gatScheSpace") String gatScheSpace,
                                      @RequestParam("gatScheInfo") String gatScheInfo,
                                      @RequestParam("spaceNo") int spaceNo,
                                      @RequestParam("date") String date,
                                      @RequestParam("time") String time,
                                      @RequestParam("time2") String time2,
                                      @RequestParam("spacePrice") int spacePrice) throws ParseException {

        SimpleDateFormat Timeformatter = new SimpleDateFormat("HH:mm");
        Date StartTime = Timeformatter.parse(time);
        Date EndTime = Timeformatter.parse(time2);
        long diffMin = (EndTime.getTime() - StartTime.getTime()) / 60000; // 분 차이 계산

        spacePrice = Integer.parseInt(String.valueOf(diffMin*spacePrice));

        service.MakeGatherSchedule(memNick, gatNo, gatScheTitle, gatScheDate, gatScheTime, gatScheMax, scheduleAddress, scheduleSpace, gatScheSpace, gatScheInfo); // 모임 만들기
        reservationService.ReservationCompGather(gatNo, memNick, spaceNo, date, time, time2, spacePrice); // 공간 예약

    }

    // 모임 일정 신청 POST 공간 빌리지 않음.
    @ResponseBody
    @RequestMapping("WriteGatherScheduleWithoutSpaceReser")
    public void WriteGatherScheduleWithoutSpaceReser(@RequestParam("memNick") String memNick,
                                                       @RequestParam("gatNo") int gatNo,
                                                       @RequestParam("gatScheTitle") String gatScheTitle,
                                                       @RequestParam("gatScheDate") String gatScheDate,
                                                       @RequestParam("gatScheTime") String gatScheTime,
                                                       @RequestParam("gatScheMax") int gatScheMax,
                                                       @RequestParam("scheduleAddress") String scheduleAddress,
                                                       @RequestParam("scheduleSpace") String scheduleSpace,
                                                       @RequestParam("gatScheSpace") String gatScheSpace,
                                                       @RequestParam("gatScheInfo") String gatScheInfo) {
        service.MakeGatherSchedule(memNick, gatNo, gatScheTitle, gatScheDate, gatScheTime, gatScheMax, scheduleAddress, scheduleSpace, gatScheSpace, gatScheInfo);
    }

    // 모임 일정 공지
    @RequestMapping("ScheduleNotice/{gatNo}/{gatScheNo}")
    public String ScheduleNotice(@PathVariable int gatNo, @PathVariable int gatScheNo, Model model) {
//        ArrayList<GatherScheduleVO> gatherSchedule = scheduleService.LoadGather_Sche(gatNo); // bch
        GatheringVO gath = service3.detailViewSomoim(gatNo);
        GatherScheduleVO gatherSchedule = scheduleService.LocdGather_Schedule(gatScheNo);
        ArrayList<Gather_Sche_PerVO> GatherJoinPerson = scheduleService.GatherJoinPerson(gatScheNo);
        model.addAttribute("gath", gath);
        model.addAttribute("gatherSchedule", gatherSchedule);
        model.addAttribute("GatherJoinPerson", GatherJoinPerson);
        scheduleService.updateViewCount(gatScheNo);
        return "bch/scheduleNotice";
    }

    // 모임 참가 ajax
    @ResponseBody
    @RequestMapping("JoinGatherPlan")
    public void JoinGatherPlan(@RequestParam("ajaxMemNick") String ajaxMemNick,
                               @RequestParam("ajaxGatScheNo") int ajaxGatScheNo,
                               @RequestParam("ajaxGatNo") int ajaxGatNo,
                               HttpServletResponse response) throws IOException {
        int ScheduleMaxPerson = scheduleService.CheckMaxPerson(ajaxGatScheNo);
        int SchedulePersonNow = scheduleService.GatherJoinNow(ajaxGatScheNo);
        System.out.println("모임 최대 인원 : " + ScheduleMaxPerson);
        System.out.println("현재 인원 : " + SchedulePersonNow);
        if(SchedulePersonNow >= ScheduleMaxPerson) {
            response.setContentType("text/html; charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.println("인원이 초과되었습니다!");
            out.flush();
        }
        else {
            scheduleService.JoinGather(ajaxGatScheNo, ajaxGatNo, ajaxMemNick);
            response.setContentType("text/html; charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.println("신청 완료!");
            out.flush();

        }
    }
}
