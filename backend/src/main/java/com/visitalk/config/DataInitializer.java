package com.visitalk.config;

import com.visitalk.model.PictogramCard;
import com.visitalk.model.User;
import com.visitalk.repository.CardRepository;
import com.visitalk.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final PasswordEncoder encoder;

    /**
     * Default PECS card library — 7 categories × ~6–8 cards = 52 cards total.
     * Format: {category, label, "emoji:<char>"}. Idempotent seeder upgrades old
     * placeholder imageUrls (e.g. "apple.png") to the emoji form on next start.
     */
    private static final String[][] DEFAULT_CARDS = {
        // People (8) — subjects + audience
        {"People", "Me", "emoji:🙋"},
        {"People", "Mom", "emoji:👩"},
        {"People", "Dad", "emoji:👨"},
        {"People", "Sister", "emoji:👧"},
        {"People", "Brother", "emoji:👦"},
        {"People", "Friend", "emoji:🧒"},
        {"People", "Teacher", "emoji:👩‍🏫"},
        {"People", "Doctor", "emoji:🧑‍⚕️"},

        // Action (8) — verbs for predicate
        {"Action", "Want", "emoji:🤲"},
        {"Action", "Like", "emoji:💖"},
        {"Action", "See", "emoji:👀"},
        {"Action", "Go", "emoji:🚶"},
        {"Action", "Come", "emoji:👈"},
        {"Action", "Give", "emoji:🤝"},
        {"Action", "Need", "emoji:🙏"},
        {"Action", "Stop", "emoji:✋"},

        // Time (6)
        {"Time", "Now", "emoji:⏰"},
        {"Time", "Later", "emoji:🔜"},
        {"Time", "Morning", "emoji:🌅"},
        {"Time", "Night", "emoji:🌙"},
        {"Time", "Today", "emoji:📅"},
        {"Time", "Tomorrow", "emoji:🗓️"},

        // Eat (8)
        {"Eat", "Apple", "emoji:🍎"},
        {"Eat", "Bread", "emoji:🍞"},
        {"Eat", "Rice", "emoji:🍚"},
        {"Eat", "Cookie", "emoji:🍪"},
        {"Eat", "Banana", "emoji:🍌"},
        {"Eat", "Pizza", "emoji:🍕"},
        {"Eat", "Egg", "emoji:🥚"},
        {"Eat", "Vegetable", "emoji:🥦"},

        // Drink (6)
        {"Drink", "Water", "emoji:💧"},
        {"Drink", "Milk", "emoji:🥛"},
        {"Drink", "Juice", "emoji:🧃"},
        {"Drink", "Tea", "emoji:🍵"},
        {"Drink", "Soup", "emoji:🍲"},
        {"Drink", "Smoothie", "emoji:🥤"},

        // Play (8)
        {"Play", "Ball", "emoji:⚽"},
        {"Play", "Blocks", "emoji:🧱"},
        {"Play", "Puzzle", "emoji:🧩"},
        {"Play", "Drawing", "emoji:🎨"},
        {"Play", "Book", "emoji:📚"},
        {"Play", "Music", "emoji:🎵"},
        {"Play", "Run", "emoji:🏃"},
        {"Play", "Outside", "emoji:🌳"},

        // Feel (8)
        {"Feel", "Happy", "emoji:😊"},
        {"Feel", "Sad", "emoji:😢"},
        {"Feel", "Angry", "emoji:😠"},
        {"Feel", "Scared", "emoji:😨"},
        {"Feel", "Tired", "emoji:😴"},
        {"Feel", "Excited", "emoji:🤩"},
        {"Feel", "Calm", "emoji:😌"},
        {"Feel", "Confused", "emoji:😕"},
    };

    private static final String FAMILY = "FAM001";

    public DataInitializer(UserRepository userRepository, CardRepository cardRepository, PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.cardRepository = cardRepository;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            User parent = new User();
            parent.setEmail("parent@test.com");
            parent.setPasswordHash(encoder.encode("password123"));
            parent.setRole("parent");
            parent.setFamilyId(FAMILY);
            parent.setPinHash(encoder.encode("1234"));
            userRepository.save(parent);

            User child = new User();
            child.setEmail("child@test.com");
            child.setPasswordHash(encoder.encode("password123"));
            child.setRole("child");
            child.setFamilyId(FAMILY);
            userRepository.save(child);
        }

        // Idempotent card seeder: for each default card, upsert by (family, category, label, is_custom=false).
        // Existing rows with stale imageUrl (e.g. "apple.png") get upgraded to "emoji:<char>".
        for (int i = 0; i < DEFAULT_CARDS.length; i++) {
            String[] def = DEFAULT_CARDS[i];
            String cat = def[0], label = def[1], url = def[2];
            int order = i;

            var existing = cardRepository.findFirstByFamilyIdAndCategoryAndLabelI18nAndIsCustom(
                FAMILY, cat, label, false);
            if (existing.isPresent()) {
                PictogramCard c = existing.get();
                boolean needsUpdate = false;
                if (c.getImageUrl() == null || !c.getImageUrl().equals(url)) {
                    c.setImageUrl(url);
                    needsUpdate = true;
                }
                // Don't touch sort_order if user has rearranged: only set when still at default 0.
                if (c.getSortOrder() == 0 && order != 0) {
                    c.setSortOrder(order);
                    needsUpdate = true;
                }
                if (needsUpdate) cardRepository.save(c);
            } else {
                PictogramCard card = new PictogramCard();
                card.setFamilyId(FAMILY);
                card.setCategory(cat);
                card.setLabelI18n(label);
                card.setImageUrl(url);
                card.setCustom(false);
                card.setSortOrder(order);
                cardRepository.save(card);
            }
        }
    }
}
