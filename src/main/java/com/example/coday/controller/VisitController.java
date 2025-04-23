package com.example.coday.controller;

import com.example.coday.model.User;
import com.example.coday.model.Visit;
import com.example.coday.repository.UserRepo;
import com.example.coday.repository.VisitRepo;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Controller
@RequestMapping("/visits")
public class VisitController {

    private final VisitRepo visitRepo;
    private final UserRepo userRepo;

    public VisitController(VisitRepo visitRepo, UserRepo userRepo) {
        this.visitRepo = visitRepo;
        this.userRepo = userRepo;
    }

    @PostMapping("/present")
    public String markPresent(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        boolean alreadyCheckedIn = visitRepo.existsByUserAndCheckOutTimeIsNull(user);
        if (!alreadyCheckedIn) {
            Visit visit = new Visit();
            visit.setUser(user);
            visit.setCheckInTime(LocalDateTime.now());
            visitRepo.save(visit);
        }

        return "redirect:/dashboard/" + user.getId();
    }

    @PostMapping("/away")
    public String markAway(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

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

        return "redirect:/dashboard/" + user.getId();
    }

    @GetMapping("/today/{userId}")
    @ResponseBody
    public ResponseEntity<?> getTodayVisit(@PathVariable Long userId) {
        Optional<User> userOpt = userRepo.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Användare hittades inte.");
        }

        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        Optional<Visit> visitOpt = visitRepo.findByUserIdAndCheckInTimeBetween(userId, startOfDay, endOfDay);

        return visitOpt.<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.ok("Ingen registrerad incheckning idag."));
    }
}

