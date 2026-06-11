package com.visitalk.service;

import com.visitalk.model.PictogramCard;
import com.visitalk.repository.CardRepository;
import org.springframework.stereotype.Component;

/**
 * Seeds a family's default PECS card library. Shared by {@code DataInitializer}
 * (demo family) and {@code AuthService} (every newly registered family) so that
 * any account starts with a rich, daily-life vocabulary rather than a blank board.
 *
 * <p>The library is geared towards helping autistic children navigate everyday
 * routines: functional requests (Need), self-care steps (Daily), places (Place),
 * plus food/drink/play/feelings and the People/Action/Time conversation pieces.
 * Every card carries an English text label; the {@code emoji:<char>} icon is a
 * visual support for that word.
 */
@Component
public class CardSeeder {

    /** {category, label, "emoji:<char>"} — grouped by category, in display order. */
    public static final String[][] DEFAULT_CARDS = {
        // People (10) — subjects / who
        {"People", "Me", "emoji:🙋"},
        {"People", "Mom", "emoji:👩"},
        {"People", "Dad", "emoji:👨"},
        {"People", "Grandma", "emoji:👵"},
        {"People", "Grandpa", "emoji:👴"},
        {"People", "Sister", "emoji:👧"},
        {"People", "Brother", "emoji:👦"},
        {"People", "Friend", "emoji:🧒"},
        {"People", "Teacher", "emoji:👩‍🏫"},
        {"People", "Doctor", "emoji:🧑‍⚕️"},

        // Action (10) — verbs / what to do
        {"Action", "Want", "emoji:🤲"},
        {"Action", "Like", "emoji:💖"},
        {"Action", "Don't like", "emoji:👎"},
        {"Action", "Go", "emoji:🚶"},
        {"Action", "Stop", "emoji:✋"},
        {"Action", "Open", "emoji:🔓"},
        {"Action", "Give", "emoji:🤝"},
        {"Action", "Make", "emoji:🛠️"},
        {"Action", "Look", "emoji:👀"},
        {"Action", "Listen", "emoji:👂"},

        // Time (8) — when / sequencing
        {"Time", "Now", "emoji:⏰"},
        {"Time", "Later", "emoji:🔜"},
        {"Time", "First", "emoji:1️⃣"},
        {"Time", "Then", "emoji:2️⃣"},
        {"Time", "Last", "emoji:🔚"},
        {"Time", "Morning", "emoji:🌅"},
        {"Time", "Night", "emoji:🌙"},
        {"Time", "Today", "emoji:📅"},

        // Need (12) — functional requests; the most important words for FC
        {"Need", "Help", "emoji:🆘"},
        {"Need", "More", "emoji:➕"},
        {"Need", "All done", "emoji:✅"},
        {"Need", "Yes", "emoji:👍"},
        {"Need", "No", "emoji:🙅"},
        {"Need", "Please", "emoji:🥺"},
        {"Need", "Thank you", "emoji:🙏"},
        {"Need", "Wait", "emoji:⏳"},
        {"Need", "My turn", "emoji:🙋"},
        {"Need", "Toilet", "emoji:🚽"},
        {"Need", "Hurt", "emoji:🤕"},
        {"Need", "Break", "emoji:🧘"},

        // Daily (12) — self-care routine steps
        {"Daily", "Wake up", "emoji:🌅"},
        {"Daily", "Brush teeth", "emoji:🪥"},
        {"Daily", "Wash hands", "emoji:🧼"},
        {"Daily", "Get dressed", "emoji:👕"},
        {"Daily", "Shoes on", "emoji:👟"},
        {"Daily", "Comb hair", "emoji:💇"},
        {"Daily", "Eat meal", "emoji:🍽️"},
        {"Daily", "Medicine", "emoji:💊"},
        {"Daily", "Clean up", "emoji:🧹"},
        {"Daily", "Bath", "emoji:🛁"},
        {"Daily", "Sleep", "emoji:😴"},
        {"Daily", "Backpack", "emoji:🎒"},

        // Place (10) — where
        {"Place", "Home", "emoji:🏠"},
        {"Place", "School", "emoji:🏫"},
        {"Place", "Park", "emoji:🏞️"},
        {"Place", "Store", "emoji:🏪"},
        {"Place", "Doctor", "emoji:🏥"},
        {"Place", "Bedroom", "emoji:🛏️"},
        {"Place", "Bathroom", "emoji:🚻"},
        {"Place", "Car", "emoji:🚗"},
        {"Place", "Playground", "emoji:🛝"},
        {"Place", "Outside", "emoji:🌳"},

        // Eat (12)
        {"Eat", "Apple", "emoji:🍎"},
        {"Eat", "Banana", "emoji:🍌"},
        {"Eat", "Bread", "emoji:🍞"},
        {"Eat", "Rice", "emoji:🍚"},
        {"Eat", "Noodles", "emoji:🍜"},
        {"Eat", "Egg", "emoji:🥚"},
        {"Eat", "Chicken", "emoji:🍗"},
        {"Eat", "Pizza", "emoji:🍕"},
        {"Eat", "Cookie", "emoji:🍪"},
        {"Eat", "Fruit", "emoji:🍓"},
        {"Eat", "Vegetable", "emoji:🥦"},
        {"Eat", "Cereal", "emoji:🥣"},

        // Drink (8)
        {"Drink", "Water", "emoji:💧"},
        {"Drink", "Milk", "emoji:🥛"},
        {"Drink", "Juice", "emoji:🧃"},
        {"Drink", "Tea", "emoji:🍵"},
        {"Drink", "Soup", "emoji:🍲"},
        {"Drink", "Smoothie", "emoji:🥤"},
        {"Drink", "Hot cocoa", "emoji:☕"},
        {"Drink", "Lemonade", "emoji:🍋"},

        // Play (12)
        {"Play", "Ball", "emoji:⚽"},
        {"Play", "Blocks", "emoji:🧱"},
        {"Play", "Puzzle", "emoji:🧩"},
        {"Play", "Drawing", "emoji:🎨"},
        {"Play", "Book", "emoji:📚"},
        {"Play", "Music", "emoji:🎵"},
        {"Play", "Run", "emoji:🏃"},
        {"Play", "Bike", "emoji:🚲"},
        {"Play", "Toy", "emoji:🧸"},
        {"Play", "TV", "emoji:📺"},
        {"Play", "Game", "emoji:🎮"},
        {"Play", "Bubbles", "emoji:🫧"},

        // Feel (12)
        {"Feel", "Happy", "emoji:😊"},
        {"Feel", "Sad", "emoji:😢"},
        {"Feel", "Angry", "emoji:😠"},
        {"Feel", "Scared", "emoji:😨"},
        {"Feel", "Tired", "emoji:😴"},
        {"Feel", "Excited", "emoji:🤩"},
        {"Feel", "Calm", "emoji:😌"},
        {"Feel", "Confused", "emoji:😕"},
        {"Feel", "Sick", "emoji:🤒"},
        {"Feel", "Shy", "emoji:😳"},
        {"Feel", "Surprised", "emoji:😲"},
        {"Feel", "Love", "emoji:🥰"},
    };

    private final CardRepository cardRepository;

    public CardSeeder(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    /**
     * Idempotently ensures the family has the full default library. Upserts by
     * (family, category, label, is_custom=false): existing rows get their icon /
     * default sort order refreshed; missing ones are inserted. Never touches a
     * parent's custom cards or a sort order the user has already rearranged.
     */
    public void seedDefaultCards(String familyId) {
        for (int i = 0; i < DEFAULT_CARDS.length; i++) {
            String[] def = DEFAULT_CARDS[i];
            String cat = def[0], label = def[1], url = def[2];
            int order = i;

            var existing = cardRepository.findFirstByFamilyIdAndCategoryAndLabelI18nAndIsCustom(
                familyId, cat, label, false);
            if (existing.isPresent()) {
                PictogramCard c = existing.get();
                boolean needsUpdate = false;
                if (c.getImageUrl() == null || !c.getImageUrl().equals(url)) {
                    c.setImageUrl(url);
                    needsUpdate = true;
                }
                if (c.getSortOrder() == 0 && order != 0) {
                    c.setSortOrder(order);
                    needsUpdate = true;
                }
                if (needsUpdate) cardRepository.save(c);
            } else {
                PictogramCard card = new PictogramCard();
                card.setFamilyId(familyId);
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
