package com.neo.rental.controller;

import com.neo.rental.dto.MemberDTO;
import com.neo.rental.service.MemberService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/rental/save") // localhost:8080/member/save 접속 시 saveForm 메소드 실행
    public String saveForm() {
        return "save";
    }

    // 회원가입 처리 메서드
    @PostMapping("/rental/save")
    public String save(@ModelAttribute MemberDTO memberDTO) {
        System.out.println("MemberController.save");
        System.out.println("memberDTO = " + memberDTO);
        memberService.save(memberDTO);
        return "index"; // 회원가입 후 기본 화면으로 이동
    }

    @GetMapping("/rental/login")
    public String loginForm() {
        return "login"; // templates/login.html 출력
    }

    // 로그인 처리 메서드
    @PostMapping("/rental/login")
    public String login(@ModelAttribute MemberDTO memberDTO, HttpSession session) {
        MemberDTO loginResult = memberService.login(memberDTO);

        if (loginResult != null) {
            // 로그인 성공 logic
            // 세션에 로그인 정보를 담는다. (키: "loginEmail", 값: 이메일)
            session.setAttribute("loginEmail", loginResult.getMemberEmail());
            return "main"; // 메인 페이지로 이동
        } else {
            // 로그인 실패 logic
            return "login"; // 다시 로그인 페이지로
        }

    }

    @GetMapping("/rental/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // 세션 무효화
        return "logout"; // logout.html 출력
    }
}
