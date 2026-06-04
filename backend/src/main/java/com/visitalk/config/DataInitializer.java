package com.visitalk.config;

import com.visitalk.model.User;
import com.visitalk.pecs.model.PictogramCard;
import com.visitalk.pecs.repository.PictogramCardRepository;
import com.visitalk.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final String DEMO_FAMILY = "FAM001";

    /** Dev pictograms: image_url holds emoji until real assets (A-5). */
    private static final String[][] DEMO_CARDS = {
        {"core", "🙋", "child.pecs.card_want", "0"},
        {"core", "🆘", "child.pecs.card_need", "1"},
        {"core", "👍", "child.pecs.card_like", "2"},
        {"core", "👎", "child.pecs.card_dont", "3"},
        {"core", "➕", "child.pecs.card_more", "4"},
        {"eat", "🍎", "child.pecs.card_apple", "0"},
        {"eat", "🍞", "child.pecs.card_bread", "1"},
        {"eat", "🍚", "child.pecs.card_rice", "2"},
        {"eat", "🍌", "child.pecs.card_banana", "3"},
        {"eat", "🥚", "child.pecs.card_egg", "4"},
        {"eat", "🍪", "child.pecs.card_cookie", "5"},
        {"drink", "🥛", "child.pecs.card_milk", "0"},
        {"drink", "💧", "child.pecs.card_water", "1"},
        {"drink", "🧃", "child.pecs.card_juice", "2"},
        {"drink", "🍵", "child.pecs.card_tea", "3"},
        {"drink", "☕", "child.pecs.card_coffee", "4"},
        {"play", "🧸", "child.pecs.card_teddy", "0"},
        {"play", "⚽", "child.pecs.card_ball", "1"},
        {"play", "🎨", "child.pecs.card_art", "2"},
        {"play", "🧩", "child.pecs.card_puzzle", "3"},
        {"play", "🎵", "child.pecs.card_music", "4"},
        {"play", "📚", "child.pecs.card_book", "5"},
        {"feel", "😊", "child.pecs.card_happy", "0"},
        {"feel", "😢", "child.pecs.card_sad", "1"},
        {"feel", "😠", "child.pecs.card_angry", "2"},
        {"feel", "😨", "child.pecs.card_scared", "3"},
        {"feel", "😴", "child.pecs.card_tired", "4"},
        {"feel", "🤒", "child.pecs.card_sick", "5"},
    };

    private final UserRepository userRepository;
    private final PictogramCardRepository cardRepository;
    private final PasswordEncoder encoder;

    public DataInitializer(
        UserRepository userRepository,
        PictogramCardRepository cardRepository,
        PasswordEncoder encoder
    ) {
        this.userRepository = userRepository;
        this.cardRepository = cardRepository;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            seedUsers();
        }
        seedPecsCardsIfMissing();
    }

    private void seedUsers() {
        User parent = new User();
        parent.setEmail("parent@test.com");
        parent.setPasswordHash(encoder.encode("password123"));
        parent.setRole("parent");
        parent.setFamilyId(DEMO_FAMILY);
        parent.setPinHash(encoder.encode("1234"));
        userRepository.save(parent);

        User child = new User();
        child.setEmail("child@test.com");
        child.setPasswordHash(encoder.encode("password123"));
        child.setRole("child");
        child.setFamilyId(DEMO_FAMILY);
        userRepository.save(child);
    }

    /** Inserts demo cards that are not already present (safe on restart / existing DB). */
    private void seedPecsCardsIfMissing() {
        for (String[] row : DEMO_CARDS) {
            if (cardRepository.existsByFamilyIdAndLabelI18n(DEMO_FAMILY, row[2])) {
                continue;
            }
            PictogramCard card = new PictogramCard();
            card.setFamilyId(DEMO_FAMILY);
            card.setCategory(row[0]);
            card.setImageUrl(row[1]);
            card.setLabelI18n(row[2]);
            card.setIsCustom(false);
            card.setSortOrder(Integer.parseInt(row[3]));
            cardRepository.save(card);
        }
    }
}
