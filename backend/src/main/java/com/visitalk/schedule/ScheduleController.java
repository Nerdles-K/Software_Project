package com.visitalk.schedule;

import com.visitalk.model.PictogramCard;
import com.visitalk.model.ScheduleInstance;
import com.visitalk.model.ScheduleTemplate;
import com.visitalk.repository.CardRepository;
import com.visitalk.repository.ScheduleInstanceRepository;
import com.visitalk.repository.ScheduleTemplateRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    private static final int MAX_STEPS = 10;

    private final ScheduleTemplateRepository templates;
    private final ScheduleInstanceRepository instances;
    private final CardRepository cards;

    public ScheduleController(ScheduleTemplateRepository templates,
                               ScheduleInstanceRepository instances,
                               CardRepository cards) {
        this.templates = templates;
        this.instances = instances;
        this.cards = cards;
    }

    // ============ Templates (B-1, B-4) ============

    @GetMapping("/templates")
    public ResponseEntity<?> listTemplates(HttpServletRequest req) {
        String familyId = (String) req.getAttribute("familyId");
        return ResponseEntity.ok(templates.findByFamilyId(familyId));
    }

    @PostMapping("/templates")
    public ResponseEntity<?> createTemplate(HttpServletRequest req,
                                             @RequestBody Map<String, Object> body) {
        if (!"parent".equals(req.getAttribute("role"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Only parents may create schedules"));
        }
        String familyId = (String) req.getAttribute("familyId");

        String name = asString(body.get("name"));
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "name is required"));
        }
        Long[] steps = parseSteps(body.get("steps"));
        if (steps == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "steps must be an array of card ids"));
        }
        if (steps.length == 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "schedule needs at least 1 step"));
        }
        if (steps.length > MAX_STEPS) {
            return ResponseEntity.badRequest().body(Map.of("error", "schedule supports at most " + MAX_STEPS + " steps"));
        }

        ScheduleTemplate t = new ScheduleTemplate();
        t.setFamilyId(familyId);
        t.setName(name.trim());
        t.setSteps(steps);
        return ResponseEntity.ok(templates.insert(t));
    }

    @PutMapping("/templates/{id}")
    public ResponseEntity<?> updateTemplate(HttpServletRequest req,
                                             @PathVariable Long id,
                                             @RequestBody Map<String, Object> body) {
        if (!"parent".equals(req.getAttribute("role"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "forbidden"));
        }
        String familyId = (String) req.getAttribute("familyId");
        Optional<ScheduleTemplate> existing = templates.findById(id);
        if (existing.isEmpty() || !familyId.equals(existing.get().getFamilyId())) {
            return ResponseEntity.notFound().build();
        }
        String name = asString(body.get("name"));
        Long[] steps = parseSteps(body.get("steps"));
        if (name == null || name.isBlank() || steps == null || steps.length == 0 || steps.length > MAX_STEPS) {
            return ResponseEntity.badRequest().body(Map.of("error", "invalid name/steps"));
        }
        templates.update(id, name.trim(), steps);
        return ResponseEntity.ok(templates.findById(id).orElseThrow());
    }

    @DeleteMapping("/templates/{id}")
    public ResponseEntity<?> deleteTemplate(HttpServletRequest req, @PathVariable Long id) {
        if (!"parent".equals(req.getAttribute("role"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "forbidden"));
        }
        String familyId = (String) req.getAttribute("familyId");
        Optional<ScheduleTemplate> existing = templates.findById(id);
        if (existing.isEmpty() || !familyId.equals(existing.get().getFamilyId())) {
            return ResponseEntity.notFound().build();
        }
        templates.delete(id);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    // ============ Instance / today's run (B-2, B-3) ============

    /**
     * Returns the child-facing "today" view for a given template:
     *   - the instance (auto-created if missing for today)
     *   - the template
     *   - the cards in step order so the frontend can render labels + icons
     * Both roles can call this.
     */
    @GetMapping("/today")
    public ResponseEntity<?> today(HttpServletRequest req, @RequestParam Long templateId) {
        String familyId = (String) req.getAttribute("familyId");
        Optional<ScheduleTemplate> tOpt = templates.findById(templateId);
        if (tOpt.isEmpty() || !familyId.equals(tOpt.get().getFamilyId())) {
            return ResponseEntity.notFound().build();
        }
        ScheduleTemplate t = tOpt.get();
        LocalDate today = LocalDate.now();
        ScheduleInstance inst = instances.findByTemplateAndDate(t.getId(), today)
            .orElseGet(() -> {
                ScheduleInstance s = new ScheduleInstance();
                s.setTemplateId(t.getId());
                s.setDate(today);
                s.setCompletedStepIndices(new Long[0]);
                return instances.insert(s);
            });
        List<PictogramCard> ordered = orderedCards(t.getSteps());
        return ResponseEntity.ok(Map.of(
            "template", t,
            "instance", inst,
            "cards", ordered
        ));
    }

    /**
     * Today's completion status for every template in the family — read-only,
     * does NOT auto-create instances. Lets the parent dashboard show which
     * schedules the child has finished today. Both roles can call this.
     */
    @GetMapping("/status")
    public ResponseEntity<?> todayStatus(HttpServletRequest req) {
        String familyId = (String) req.getAttribute("familyId");
        LocalDate today = LocalDate.now();
        List<Map<String, Object>> out = new ArrayList<>();
        for (ScheduleTemplate t : templates.findByFamilyId(familyId)) {
            int total = t.getSteps() == null ? 0 : t.getSteps().length;
            int completedCount = instances.findByTemplateAndDate(t.getId(), today)
                .map(inst -> (int) Arrays.stream(inst.getCompletedStepIndices())
                    .filter(i -> i != null && i >= 0 && i < total)
                    .distinct().count())
                .orElse(0);
            Map<String, Object> row = new HashMap<>();
            row.put("templateId", t.getId());
            row.put("totalSteps", total);
            row.put("completedCount", completedCount);
            row.put("completed", total > 0 && completedCount >= total);
            out.add(row);
        }
        return ResponseEntity.ok(out);
    }

    /** B-3: toggle a step's completion (0-based index). Children may call this. */
    @PutMapping("/instances/{id}/step")
    public ResponseEntity<?> toggleStep(@PathVariable Long id,
                                         @RequestBody Map<String, Object> body) {
        Number rawIdx = (Number) body.get("stepIndex");
        Boolean completed = (Boolean) body.get("completed");
        if (rawIdx == null || completed == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "stepIndex and completed are required"));
        }
        long idx = rawIdx.longValue();

        Optional<ScheduleInstance> instOpt = instances.findById(id);
        if (instOpt.isEmpty()) return ResponseEntity.notFound().build();
        ScheduleInstance inst = instOpt.get();

        Set<Long> set = new TreeSet<>(Arrays.asList(inst.getCompletedStepIndices()));
        if (completed) set.add(idx); else set.remove(idx);
        Long[] next = set.toArray(new Long[0]);
        instances.updateCompleted(id, next);
        inst.setCompletedStepIndices(next);
        return ResponseEntity.ok(inst);
    }

    // ---- helpers ----

    private static String asString(Object o) { return o == null ? null : o.toString(); }

    private static Long[] parseSteps(Object raw) {
        if (!(raw instanceof List<?> list)) return null;
        Long[] out = new Long[list.size()];
        for (int i = 0; i < list.size(); i++) {
            Object v = list.get(i);
            if (!(v instanceof Number n)) return null;
            out[i] = n.longValue();
        }
        return out;
    }

    private List<PictogramCard> orderedCards(Long[] steps) {
        if (steps == null || steps.length == 0) return List.of();
        Set<Long> uniq = new HashSet<>(Arrays.asList(steps));
        Map<Long, PictogramCard> byId = cards.findAllById(uniq).stream()
            .collect(Collectors.toMap(PictogramCard::getId, c -> c));
        return Arrays.stream(steps).map(byId::get).filter(Objects::nonNull).toList();
    }
}
