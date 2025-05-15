package com.example.coday.controller;

import com.example.coday.model.User;
import com.example.coday.model.Visit;
import com.example.coday.repository.UserRepo;
import com.example.coday.repository.VisitRepo;
import com.example.coday.security.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Controller
@RequestMapping("/visit")
public class VisitController {

    private final VisitRepo visitRepo;
    private final UserRepo userRepo;

    public VisitController(VisitRepo visitRepo, UserRepo userRepo) {
        this.visitRepo = visitRepo;
        this.userRepo = userRepo;
    }

    @PostMapping("/checkin")
    public String checkIn(@AuthenticationPrincipal CustomUserDetails currentUser) {
        User user = currentUser.getUser();

        boolean alreadyCheckedIn = visitRepo.existsByUserAndCheckOutTimeIsNull(user);
        if (!alreadyCheckedIn) {
            Visit visit = new Visit();
            visit.setUser(user);
            visit.setCheckInTime(LocalDateTime.now());
            visitRepo.save(visit);
        }

        return "redirect:/user/dashboard";
    }

    @PostMapping("/checkout")
    public String checkOut(@AuthenticationPrincipal CustomUserDetails currentUser) {
        User user = currentUser.getUser();

        Optional<Visit> visitOpt = visitRepo.findFirstByUserAndCheckOutTimeIsNullOrderByCheckInTimeDesc(user);
        if (visitOpt.isPresent()) {
            Visit visit = visitOpt.get();
            visit.setCheckOutTime(LocalDateTime.now());

            Duration duration = Duration.between(visit.getCheckInTime(), visit.getCheckOutTime());
            long minutes = duration.toMinutes();

            if (minutes >= 1) {
                user.setPoints(user.getPoints() + (int) minutes);
                userRepo.save(user);
            }

            visitRepo.save(visit);
        }

        return "redirect:/user/dashboard";
    }

    @GetMapping("/today")
    @ResponseBody
    public ResponseEntity<?> getTodayVisit(@AuthenticationPrincipal CustomUserDetails currentUser) {
        User user = currentUser.getUser();

        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        Optional<Visit> visitOpt = visitRepo.findByUserIdAndCheckInTimeBetween(user.getId(), startOfDay, endOfDay);

        return visitOpt.<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.ok("Ingen registrerad incheckning idag."));
    }
}
